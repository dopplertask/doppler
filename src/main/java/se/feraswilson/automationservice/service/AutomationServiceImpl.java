package se.feraswilson.automationservice.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Service;
import se.feraswilson.automationservice.dao.TaskDao;
import se.feraswilson.automationservice.domain.ActionResult;
import se.feraswilson.automationservice.domain.Task;
import se.feraswilson.automationservice.domain.action.Action;

import javax.transaction.Transactional;
import java.util.Optional;

@Service
public class AutomationServiceImpl implements AutomationService {

    private static final Logger LOG = LoggerFactory.getLogger(AutomationServiceImpl.class);

    @Autowired
    private JmsTemplate jmsTemplate;

    @Autowired
    private TaskDao taskDao;

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

            // Populate variables
            task.getParameters().putAll(automationRequest.getParameters());

            // Start processing task
            for (Action currentAction : task.getActionList()) {

                ActionResult actionResult = currentAction.run();
                LOG.info("Ran current action: {} with result: {}", actionResult.getStatusCode(), actionResult.getErrorMsg());
            }
        } else {
            LOG.warn("Task could not be found [taskId={}]", automationRequest.getAutomationId());
        }


    }
}
