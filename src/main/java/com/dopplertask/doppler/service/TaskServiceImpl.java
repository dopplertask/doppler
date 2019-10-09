package com.dopplertask.doppler.service;

import com.dopplertask.doppler.dao.TaskDao;
import com.dopplertask.doppler.dao.TaskExecutionDao;
import com.dopplertask.doppler.domain.Task;
import com.dopplertask.doppler.domain.TaskExecution;
import com.dopplertask.doppler.domain.action.Action;
import com.dopplertask.doppler.domain.action.LinkedTaskAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.HashMap;
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
        taskExecutionRequest.setTaskName(request.getTaskName());
        taskExecutionRequest.setParameters(request.getParameters());
        taskExecutionRequest.setExecutionId(execution.getId());
        taskExecutionRequest.setChecksum(request.getChecksum());

        jmsTemplate.convertAndSend("automation_destination", taskExecutionRequest);

        return execution;
    }

    @Override
    @JmsListener(destination = "automation_destination", containerFactory = "jmsFactory")
    public void handleAutomationRequest(TaskExecutionRequest automationRequest) {
        runRequest(automationRequest);
    }

    @Override
    public TaskExecution runRequest(TaskExecutionRequest taskExecutionRequest) {
        TaskExecution execution = executionService.startExecution(taskExecutionRequest, this);

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
    public Long createTask(String name, List<Action> actions, String checksum) {
        return createTask(name, actions, checksum, true);
    }

    @Override
    @Transactional
    public Long createTask(String name, List<Action> actions, String checksum, boolean buildTask) {

        if (name.contains(" ")) {
            throw new WhiteSpaceInNameException("Could not create task. Task name contains whitespace.");
        }

        // If we try to add the same task again, we just return the current id.
        Optional<Task> existingTask = taskDao.findByChecksum(checksum);
        if (existingTask.isPresent()) {
            return existingTask.get().getId();
        }

        Task task = new Task();

        task.setName(name);
        actions.forEach(action -> {
            action.setTask(task);

            // Find task or download it if necessary and assign checksum
            if (action instanceof LinkedTaskAction) {
                if (((LinkedTaskAction) action).getName() != null && !((LinkedTaskAction) action).getName().isEmpty()) {
                    Optional<Task> linkedTask = executionService.findOrDownloadByName(((LinkedTaskAction) action).getName(), this);

                    if (linkedTask.isPresent()) {
                        ((LinkedTaskAction) action).setChecksum(linkedTask.get().getChecksum());
                    } else {
                        throw new LinkedTaskNotFoundException("Linked Task could not be found locally or in the public hub [name=" + ((LinkedTaskAction) action).getName() + "]");
                    }
                } else {
                    throw new LinkedTaskNotFoundException("Linked Task does not have any task name associated");
                }
            }
        });

        task.setActionList(actions);
        task.setCreated(new Date());
        task.setChecksum(checksum);

        taskDao.save(task);

        // Run the execution
        if (buildTask) {
            TaskRequest taskRequest = new TaskRequest();
            taskRequest.setTaskName(task.getName());
            taskRequest.setParameters(new HashMap<>());

            TaskExecution execution = runRequest(taskRequest);
            if (!execution.isSuccess()) {
                throw new BuildNotSuccessfulException("Could not build task, execution was not successful.");
            }
        }

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
        execution.setDepth(request.getDepth());
        // Create execution record
        // Always save to allow consumer to get a message
        taskExecutionDao.save(execution);

        TaskExecutionRequest taskExecutionRequest = new TaskExecutionRequest();
        taskExecutionRequest.setTaskName(request.getTaskName());
        taskExecutionRequest.setParameters(request.getParameters());
        taskExecutionRequest.setExecutionId(execution.getId());
        taskExecutionRequest.setDepth(request.getDepth());
        taskExecutionRequest.setChecksum(request.getChecksum());

        return this.runRequest(taskExecutionRequest);
    }
}
