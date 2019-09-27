package com.dopplertask.doppler.service;

import com.dopplertask.doppler.domain.TaskExecution;

public interface ExecutionService {
    /**
     * Initiates execution based on a request.
     *
     * @param taskExecutionRequest with information about what task to run and with what parameters.
     * @return task execution containing task info and initial logs.
     */
    TaskExecution startExecution(TaskExecutionRequest taskExecutionRequest);

    /**
     * Runs all actions in an execution.
     *
     * @param taskId      of the requested task.
     * @param executionId of the execution.
     * @param taskService to be provided to actions.
     * @return task execution with logs.
     */
    TaskExecution processActions(Long taskId, Long executionId, TaskService taskService);
}
