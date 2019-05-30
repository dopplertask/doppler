package com.dopplertask.doppler.service;

import com.dopplertask.doppler.domain.Task;
import com.dopplertask.doppler.domain.TaskExecution;
import com.dopplertask.doppler.domain.action.Action;

import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * This service represents the scheduling and execution of tasks
 */
public interface TaskService {

    /**
     * Schedule a task
     *
     * @param request containing the information about what to execute.
     * @return
     */
    TaskExecution delegate(TaskRequest request);


    /**
     * Consumes scheduled tasks and executes them.
     *
     * @param taskRequest containing the information about what to execute.
     */
    void handleAutomationRequest(TaskExecutionRequest taskRequest);

    /**
     * Runs task with provided execution.
     *
     * @param taskRequest containing the information about what to execute.
     *                    @return execution containing the results.
     */
    @Transactional
    TaskExecution runRequest(TaskExecutionRequest automationRequest);

    /**
     * Creates execution and runs task.
     *
     * @param request containing the information about what to execute.
     * @return execution containing the results.
     */
    @Transactional
    TaskExecution runRequest(TaskRequest request);

    /**
     * Get all tasks
     *
     * @return all tasks from database.
     */
    List<Task> getAllTasks();

    Long createTask(String name, List<Action> actions);


}
