package com.dopplertask.doppler.service;

import com.dopplertask.doppler.domain.Task;
import com.dopplertask.doppler.domain.TaskExecution;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Service;

import com.dopplertask.doppler.dao.TaskDao;
import com.dopplertask.doppler.dao.TaskExecutionDao;
import com.dopplertask.doppler.domain.ActionResult;
import com.dopplertask.doppler.domain.StatusCode;
import com.dopplertask.doppler.domain.TaskExecutionLog;
import com.dopplertask.doppler.domain.action.Action;

import java.util.Date;
import java.util.List;
import java.util.Optional;

import javax.transaction.Transactional;

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

                ActionResult actionResult = currentAction.run(this, execution);
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
