package se.feraswilson.taskservice.service;

import se.feraswilson.taskservice.domain.Task;
import se.feraswilson.taskservice.domain.TaskExecution;
import se.feraswilson.taskservice.domain.action.Action;

import javax.transaction.Transactional;
import java.util.List;

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
