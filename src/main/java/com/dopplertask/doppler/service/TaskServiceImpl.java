package com.dopplertask.doppler.service;

import com.dopplertask.doppler.dao.TaskDao;
import com.dopplertask.doppler.dao.TaskExecutionDao;
import com.dopplertask.doppler.domain.Task;
import com.dopplertask.doppler.domain.TaskExecution;
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

    @Autowired
    private ExecutionService executionService;

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
    public void handleAutomationRequest(TaskExecutionRequest automationRequest) {
        runRequest(automationRequest);
    }

    @Override
    public TaskExecution runRequest(TaskExecutionRequest automationRequest) {
        TaskExecution execution = executionService.startExecution(automationRequest);

        if (execution != null) {
            return executionService.processActions(execution.getTask().getId(), execution.getId(), this);
        }
        return null;
    }

    @Override
    public List<Task> getAllTasks() {
        return taskDao.findAll();
    }

    @Override
    @Transactional
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
    public List<TaskExecution> getExecutions() {
        return taskExecutionDao.findAllByTaskNotNull();
    }

    @Override
    public Task getTask(long id) {
        Optional<Task> task = taskDao.findById(id);
        return task.isPresent() ? task.get() : null;
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
