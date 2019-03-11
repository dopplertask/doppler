package se.feraswilson.automationservice.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Service;
import se.feraswilson.automationservice.dao.TaskDao;
import se.feraswilson.automationservice.dao.TaskExecutionDao;
import se.feraswilson.automationservice.domain.ActionResult;
import se.feraswilson.automationservice.domain.StatusCode;
import se.feraswilson.automationservice.domain.Task;
import se.feraswilson.automationservice.domain.TaskExecution;
import se.feraswilson.automationservice.domain.TaskExecutionLog;
import se.feraswilson.automationservice.domain.action.Action;

import javax.transaction.Transactional;
import java.util.Date;
import java.util.Optional;

@Service
public class AutomationServiceImpl implements AutomationService {

    private static final Logger LOG = LoggerFactory.getLogger(AutomationServiceImpl.class);

    @Autowired
    private JmsTemplate jmsTemplate;

    @Autowired
    private TaskDao taskDao;

    @Autowired
    private TaskExecutionDao taskExecutionDao;

    @Override
    public void delegate(AutomationRequest request) {
        LOG.info("Delegating action");

        jmsTemplate.convertAndSend("automation_destination", request);

    }

    @Override
    @JmsListener(destination = "automation_destination", containerFactory = "jmsFactory")
    @Transactional
    public void handleAutomationRequest(AutomationRequest automationRequest) {
        Optional<Task> taskRequest = taskDao.findById(automationRequest.getAutomationId());

        if (taskRequest.isPresent()) {
            Task task = taskRequest.get();

            TaskExecution execution = new TaskExecution();
            execution.setTask(task);

            // Create execution record
            taskExecutionDao.save(execution);

            // Populate variables
            execution.getParameters().putAll(automationRequest.getParameters());

            execution.setStartdate(new Date());

            // Start processing task
            for (Action currentAction : task.getActionList()) {

                ActionResult actionResult = currentAction.run(execution);
                TaskExecutionLog log = new TaskExecutionLog();
                log.setTaskExecution(execution);

                LOG.info("Ran current action: {} with result: {}", actionResult.getStatusCode(), actionResult.getErrorMsg());

                log.setOutput(actionResult.getOutput());

                // If action did not go well
                if (actionResult.getStatusCode() == StatusCode.FAILURE) {
                    log.setOutput(actionResult.getErrorMsg());
                    execution.setSuccess(false);
                    execution.addLog(log);
                    break;
                }

                // Add log to the execution
                execution.addLog(log);
            }

            execution.setEnddate(new Date());


        } else {
            LOG.warn("Task could not be found [taskId={}]", automationRequest.getAutomationId());
        }


    }
}
