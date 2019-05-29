package com.dopplertask.doppler.service;

import com.dopplertask.doppler.domain.Task;
import com.dopplertask.doppler.domain.TaskExecution;
import com.dopplertask.doppler.domain.action.Action;

import java.util.List;

import javax.transaction.Transactional;

/**
 * This service represents the scheduling and execution of tasks
 */
public interface TaskService {

    /**
     * Schedule a task
     *
     * @param request containing the information about what to execute.
     */
    void delegate(TaskRequest request);

    /**
     * Consumes scheduled tasks.
     *
     * @param taskRequest containing the information about what to execute.
     */
    void handleAutomationRequest(TaskRequest taskRequest);

    @Transactional
    TaskExecution runRequest(TaskRequest automationRequest);

    /**
     * Get all tasks
     *
     * @return all tasks from database.
     */
    List<Task> getAllTasks();

    Long createTask(String name, List<Action> actions);
}
