package se.feraswilson.taskservice.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Service;
import se.feraswilson.taskservice.dao.ActionDao;
import se.feraswilson.taskservice.dao.TaskDao;
import se.feraswilson.taskservice.dao.TaskExecutionDao;
import se.feraswilson.taskservice.domain.ActionResult;
import se.feraswilson.taskservice.domain.StatusCode;
import se.feraswilson.taskservice.domain.Task;
import se.feraswilson.taskservice.domain.TaskExecution;
import se.feraswilson.taskservice.domain.TaskExecutionLog;
import se.feraswilson.taskservice.domain.action.Action;

import javax.transaction.Transactional;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
public class TaskServiceImpl implements TaskService {

    private static final Logger LOG = LoggerFactory.getLogger(TaskServiceImpl.class);

    @Autowired
    private JmsTemplate jmsTemplate;

    @Autowired
    private TaskDao taskDao;

    @Autowired
    private TaskExecutionDao taskExecutionDao;

    @Override
    public void delegate(TaskRequest request) {
        LOG.info("Delegating action");

        jmsTemplate.convertAndSend("automation_destination", request);

    }

    @Override
    @JmsListener(destination = "automation_destination", containerFactory = "jmsFactory")
    @Transactional
    public void handleAutomationRequest(TaskRequest automationRequest) {
        runRequest(automationRequest);
    }

    @Transactional
    @Override
    public TaskExecution runRequest(TaskRequest automationRequest) {
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

                LOG.info("Ran current action: {} with status code: {} and with result: {}", currentAction.getClass().getSimpleName(), actionResult.getStatusCode(), actionResult.getOutput());

                log.setOutput(actionResult.getOutput());

                // If action did not go well
                if (actionResult.getStatusCode() == StatusCode.FAILURE) {
                    log.setOutput(actionResult.getErrorMsg());
                    execution.setSuccess(false);
                    execution.addLog(log);
                    broadcastResults(log);
                    break;
                }

                // Add log to the execution
                execution.addLog(log);
                broadcastResults(log);
            }

            execution.setEnddate(new Date());

            return execution;

        } else {
            LOG.warn("Task could not be found [taskId={}]", automationRequest.getAutomationId());
            return null;
        }
    }

    private void broadcastResults(TaskExecutionLog taskExecutionLog) {
        jmsTemplate.convertAndSend("taskexecution_destination", taskExecutionLog.getOutput(), message -> {
            message.setLongProperty("executionId", taskExecutionLog.getTaskExecution().getId());
            return message;
        });
    }

    @Override
    public List<Task> getAllTasks() {
        return taskDao.findAll();
    }

    @Override
    public Long createTask(String name, List<Action> actions) {

        Task task = new Task();
        task.setName(name);
        actions.forEach(action -> action.setTask(task));
        task.setActionList(actions);
        task.setCreated(new Date());
        taskDao.save(task);


        return task.getId();
    }
}
