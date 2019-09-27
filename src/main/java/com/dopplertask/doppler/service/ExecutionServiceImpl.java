package com.dopplertask.doppler.service;

import com.dopplertask.doppler.dao.TaskDao;
import com.dopplertask.doppler.dao.TaskExecutionDao;
import com.dopplertask.doppler.domain.ActionResult;
import com.dopplertask.doppler.domain.StatusCode;
import com.dopplertask.doppler.domain.Task;
import com.dopplertask.doppler.domain.TaskExecution;
import com.dopplertask.doppler.domain.TaskExecutionLog;
import com.dopplertask.doppler.domain.TaskExecutionStatus;
import com.dopplertask.doppler.domain.action.Action;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.Optional;

@Service
public class ExecutionServiceImpl implements ExecutionService {


    private static final Logger LOG = LoggerFactory.getLogger(ExecutionServiceImpl.class);

    @Autowired
    private JmsTemplate jmsTemplate;

    @Autowired
    private TaskDao taskDao;

    @Autowired
    private TaskExecutionDao taskExecutionDao;

    @Transactional
    public TaskExecution startExecution(TaskExecutionRequest taskExecutionRequest) {
        Optional<Task> taskRequest = taskDao.findById(taskExecutionRequest.getAutomationId());
        Optional<TaskExecution> executionReq = taskExecutionDao.findById(taskExecutionRequest.getExecutionId());
        if (taskRequest.isPresent() && executionReq.isPresent()) {
            Task task = taskRequest.get();
            TaskExecution execution = executionReq.get();

            // Assign task to execution
            execution.setTask(task);

            // Populate variables
            execution.getParameters().putAll(taskExecutionRequest.getParameters());

            execution.setStartdate(new Date());
            execution.setStatus(TaskExecutionStatus.STARTED);

            TaskExecutionLog executionStarted = new TaskExecutionLog();
            executionStarted.setTaskExecution(execution);
            executionStarted.setOutput("Task execution started [taskId=" + task.getId() + ", executionId=" + execution.getId() + "]");
            execution.addLog(executionStarted);


            taskExecutionDao.save(execution);

            LOG.info("Task execution started [taskId={}, executionId={}]", task.getId(), execution.getId());


            broadcastResults(executionStarted);

            return execution;
        } else {
            LOG.warn("Task could not be found [taskId={}]", taskExecutionRequest.getAutomationId());

            TaskExecution taskExecution = new TaskExecution();
            taskExecution.setId(0L);
            TaskExecutionLog noTaskLog = new TaskExecutionLog();
            noTaskLog.setOutput("Task could not be found [taskId=" + taskExecutionRequest.getAutomationId() + "]");
            broadcastResults(noTaskLog);
            return null;
        }
    }

    @Override
    @Transactional
    public TaskExecution processActions(Long taskId, Long executionId, TaskService taskService) {
        Optional<Task> taskRequest = taskDao.findById(taskId);
        Optional<TaskExecution> executionReq = taskExecutionDao.findById(executionId);
        if (taskRequest.isPresent() && executionReq.isPresent()) {
            Task task = taskRequest.get();
            TaskExecution execution = executionReq.get();

            // Start processing task
            for (Action currentAction : task.getActionList()) {

                ActionResult actionResult = new ActionResult();
                try {
                    actionResult = currentAction.run(taskService, execution);
                } catch (Exception e) {
                    LOG.error("Exception occured: {}", e);
                }

                TaskExecutionLog log = new TaskExecutionLog();
                log.setTaskExecution(execution);

                LOG.info("Ran current action: {} with status code: {} and with result: {}", currentAction.getClass().getSimpleName(), actionResult.getStatusCode(), actionResult.getOutput());

                log.setOutput(actionResult.getOutput());
                log.setOutputType(actionResult.getOutputType());

                // If action did not go well
                if (actionResult.getStatusCode() == StatusCode.FAILURE) {
                    log.setOutput(actionResult.getErrorMsg());
                    log.setOutputType(actionResult.getOutputType());
                    execution.setSuccess(false);
                    execution.addLog(log);
                    broadcastResults(log);
                    break;
                }

                // Add log to the execution
                execution.addLog(log);
                broadcastResults(log);

                TaskExecutionLog executionCompleted = new TaskExecutionLog();
                executionCompleted.setTaskExecution(execution);
                executionCompleted.setOutput("Task execution completed [taskId=" + task.getId() + ", executionId=" + execution.getId() + "]");
                execution.addLog(executionCompleted);
                broadcastResults(executionCompleted);

                LOG.info("Task execution completed [taskId={}, executionId={}]", task.getId(), execution.getId());

                execution.setEnddate(new Date());
                execution.setStatus(execution.isSuccess() ? TaskExecutionStatus.FINISHED : TaskExecutionStatus.FAILED);
            }

            return execution;
        }
        return null;
    }

    private void broadcastResults(TaskExecutionLog taskExecutionLog) {
        BroadcastResult result = new BroadcastResult(taskExecutionLog.getOutput(), taskExecutionLog.getOutputType());

        jmsTemplate.convertAndSend("taskexecution_destination", result, message -> {
            message.setLongProperty("executionId", taskExecutionLog.getTaskExecution().getId());
            return message;
        });
    }

}
