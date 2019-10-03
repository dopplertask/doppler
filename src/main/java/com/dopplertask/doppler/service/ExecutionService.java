package com.dopplertask.doppler.service;

import com.dopplertask.doppler.domain.Task;
import com.dopplertask.doppler.domain.TaskExecution;

import java.util.Map;
import java.util.Optional;

public interface ExecutionService {
    /**
     * Initiates execution based on a request.
     *
     * @param taskExecutionRequest with information about what task to run and with what parameters.
     * @return task execution containing task info and initial logs.
     */
    TaskExecution startExecution(TaskExecutionRequest taskExecutionRequest, TaskService taskService);

    /**
     * Runs all actions in an execution.
     *
     * @param taskId      of the requested task.
     * @param executionId of the execution.
     * @param taskParameters from the task request
     * @param taskService to be provided to actions.
     * @return task execution with logs.
     */
    TaskExecution processActions(Long taskId, Long executionId, TaskService taskService);

    Optional<Task> findOrDownloadByName(String taskName, TaskService taskService);
}
