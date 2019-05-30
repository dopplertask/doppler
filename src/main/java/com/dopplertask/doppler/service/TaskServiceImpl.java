package com.dopplertask.doppler.service;

import com.dopplertask.doppler.dao.TaskDao;
import com.dopplertask.doppler.dao.TaskExecutionDao;
import com.dopplertask.doppler.domain.ActionResult;
import com.dopplertask.doppler.domain.StatusCode;
import com.dopplertask.doppler.domain.Task;
import com.dopplertask.doppler.domain.TaskExecution;
import com.dopplertask.doppler.domain.TaskExecutionLog;
import com.dopplertask.doppler.domain.action.Action;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    @Transactional
    public TaskExecution delegate(TaskRequest request) {
        LOG.info("Delegating action");

        TaskExecution execution = new TaskExecution();
        // Create execution record
        taskExecutionDao.save(execution);

        TaskExecutionRequest taskExecutionRequest = new TaskExecutionRequest();
        taskExecutionRequest.setAutomationId(request.getAutomationId());
        taskExecutionRequest.setParameters(request.getParameters());
        taskExecutionRequest.setExecutionId(execution.getId());

        jmsTemplate.convertAndSend("automation_destination", taskExecutionRequest);

        return execution;
    }

    @Override
    @JmsListener(destination = "automation_destination", containerFactory = "jmsFactory")
    @Transactional
    public void handleAutomationRequest(TaskExecutionRequest automationRequest) {
        runRequest(automationRequest);
    }

    @Transactional
    @Override
    public TaskExecution runRequest(TaskExecutionRequest automationRequest) {
        Optional<Task> taskRequest = taskDao.findById(automationRequest.getAutomationId());
        Optional<TaskExecution> executionReq = taskExecutionDao.findById(automationRequest.getExecutionId());
        if (taskRequest.isPresent() && executionReq.isPresent()) {
            Task task = taskRequest.get();
            TaskExecution execution = executionReq.get();

            // Assign task to execution
            execution.setTask(task);

            // Populate variables
            execution.getParameters().putAll(automationRequest.getParameters());

            execution.setStartdate(new Date());

            TaskExecutionLog executionStarted = new TaskExecutionLog();
            executionStarted.setTaskExecution(execution);
            executionStarted.setOutput("Task execution started [taskId=" + task.getId() + ", executionId=" + execution.getId() + "]");
            execution.addLog(executionStarted);
            broadcastResults(executionStarted);

            LOG.info("Task execution started [taskId={}, executionId={}]", task.getId(), execution.getId());

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


            TaskExecutionLog executionCompleted = new TaskExecutionLog();
            executionCompleted.setTaskExecution(execution);
            executionCompleted.setOutput("Task execution completed [taskId=" + task.getId() + ", executionId=" + execution.getId() + "]");
            execution.addLog(executionCompleted);
            broadcastResults(executionCompleted);


            LOG.info("Task execution completed [taskId={}, executionId={}]", task.getId(), execution.getId());

            execution.setEnddate(new Date());
            return execution;

        } else {
            LOG.warn("Task could not be found [taskId={}]", automationRequest.getAutomationId());

            TaskExecution taskExecution = new TaskExecution();
            taskExecution.setId(0L);
            TaskExecutionLog noTaskLog = new TaskExecutionLog();
            noTaskLog.setOutput("Task could not be found [taskId=" + automationRequest.getAutomationId() + "]");
            broadcastResults(noTaskLog);
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

    @Override
    public TaskExecution runRequest(TaskRequest request) {
        TaskExecution execution = new TaskExecution();
        // Create execution record
        taskExecutionDao.save(execution);

        TaskExecutionRequest taskExecutionRequest = new TaskExecutionRequest();
        taskExecutionRequest.setAutomationId(request.getAutomationId());
        taskExecutionRequest.setParameters(request.getParameters());
        taskExecutionRequest.setExecutionId(execution.getId());

        return this.runRequest(taskExecutionRequest);
    }
}
