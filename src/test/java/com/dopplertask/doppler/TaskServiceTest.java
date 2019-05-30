package com.dopplertask.doppler;

import com.dopplertask.doppler.dao.TaskDao;
import com.dopplertask.doppler.dao.TaskExecutionDao;
import com.dopplertask.doppler.domain.Task;
import com.dopplertask.doppler.domain.TaskExecution;
import com.dopplertask.doppler.domain.action.Action;
import com.dopplertask.doppler.domain.action.LinkedTaskAction;
import com.dopplertask.doppler.domain.action.PrintAction;
import com.dopplertask.doppler.service.TaskExecutionRequest;
import com.dopplertask.doppler.service.TaskServiceImpl;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.HashMap;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@RunWith(SpringRunner.class)
public class TaskServiceTest {

    @InjectMocks
    private TaskServiceImpl taskService;

    @Mock
    private TaskDao taskDao;

    @Mock
    private TaskExecutionDao taskExecutionDao;

    @Mock
    private JmsTemplate jmsTemplate;

    @Test
    public void runSimpleTaskWithNoActionsShouldReturnOk() {
        // Setup data
        Task task = new Task();
        task.setId(1928L);

        TaskExecution taskExecution = new TaskExecution();
        taskExecution.setId(283471L);

        // Prepare behavior
        Optional<Task> taskOptional = Optional.of(task);
        when(taskDao.findById(eq(1928L))).thenReturn(taskOptional);

        Optional<TaskExecution> executionOptional = Optional.of(taskExecution);
        when(taskExecutionDao.findById(eq(283471L))).thenReturn(executionOptional);

        // Run test
        TaskExecutionRequest request = new TaskExecutionRequest();
        request.setExecutionId(283471L);
        request.setAutomationId(1928L);
        request.setParameters(new HashMap<>());
        TaskExecution taskExecutionReturned = taskService.runRequest(request);

        // Setup assert params
        Optional<Long> executionIdOptional = Optional.ofNullable(taskExecutionReturned.getId());
        long executionId = executionIdOptional.orElse(-1L);

        Optional<Long> taskIdOptional = Optional.ofNullable(taskExecutionReturned.getTask().getId());
        long taskId = taskIdOptional.orElse(-1L);

        Assert.assertNotNull(taskExecutionReturned);
        Assert.assertEquals(283471L, executionId);
        Assert.assertEquals(1928L, taskId);
        Assert.assertEquals(2, taskExecutionReturned.getLogs().size());
        Mockito.verify(jmsTemplate, Mockito.times(2)).convertAndSend(eq("taskexecution_destination"), Mockito.anyString(), Mockito.any());
    }

    @Test
    public void runSimpleTaskWithPrintActionShouldReturnOk() {
        // Setup data
        Action action = new PrintAction("Automation is the future");

        Task task = new Task();
        task.setId(1928L);
        task.getActionList().add(action);

        TaskExecution taskExecution = new TaskExecution();
        taskExecution.setId(283472L);

        // Prepare behavior
        Optional<Task> taskOptional = Optional.of(task);
        when(taskDao.findById(eq(1928L))).thenReturn(taskOptional);

        Optional<TaskExecution> executionOptional = Optional.of(taskExecution);
        when(taskExecutionDao.findById(eq(283472L))).thenReturn(executionOptional);

        // Run test
        TaskExecutionRequest request = new TaskExecutionRequest();
        request.setExecutionId(283472L);
        request.setAutomationId(1928L);
        request.setParameters(new HashMap<>());
        TaskExecution taskExecutionReturned = taskService.runRequest(request);

        // Setup assert params
        Optional<Long> executionIdOptional = Optional.ofNullable(taskExecutionReturned.getId());
        long executionId = executionIdOptional.orElse(-1L);

        Optional<Long> taskIdOptional = Optional.ofNullable(taskExecutionReturned.getTask().getId());
        long taskId = taskIdOptional.orElse(-1L);

        Assert.assertNotNull(taskExecutionReturned);
        Assert.assertEquals(283472L, executionId);
        Assert.assertEquals(1928L, taskId);
        Assert.assertEquals(3, taskExecutionReturned.getLogs().size());
        Mockito.verify(jmsTemplate, Mockito.times(3)).convertAndSend(eq("taskexecution_destination"), Mockito.anyString(), Mockito.any());
    }

    @Test
    public void runTaskWithLinkedTaskShouldReturnOk() {
        // Setup data
        Task linkedTask = new Task();
        linkedTask.setId(120L);
        linkedTask.getActionList().add(new PrintAction("Printing from a linked task"));

        TaskExecution linkedTaskExecution = new TaskExecution();
        linkedTaskExecution.setId(12001L);

        LinkedTaskAction action = new LinkedTaskAction();
        action.setLinkedTaskId(120L);

        Task task = new Task();
        task.setId(1928L);
        task.getActionList().add(action);

        TaskExecution taskExecution = new TaskExecution();
        taskExecution.setId(283472L);

        // Prepare behavior
        Optional<Task> linkedTaskOptional = Optional.of(linkedTask);
        when(taskDao.findById(eq(120L))).thenReturn(linkedTaskOptional);

        Optional<Task> taskOptional = Optional.of(task);
        when(taskDao.findById(eq(1928L))).thenReturn(taskOptional);

        Optional<TaskExecution> executionOptional = Optional.of(taskExecution);
        when(taskExecutionDao.findById(eq(283472L))).thenReturn(executionOptional);

        Optional<TaskExecution> linkedTaskExecutionOptional = Optional.of(linkedTaskExecution);
        when(taskExecutionDao.findById(eq(12001L))).thenReturn(linkedTaskExecutionOptional);

        when(taskExecutionDao.save(any())).thenAnswer(invocation -> {
            TaskExecution te = ((TaskExecution) invocation.getArguments()[0]);

            // Some different id for the task execution of the linked automation.
            te.setId(linkedTaskExecution.getId());
            return te;
        });

        // Run test
        TaskExecutionRequest request = new TaskExecutionRequest();
        request.setExecutionId(283472L);
        request.setAutomationId(1928L);
        request.setParameters(new HashMap<>());
        TaskExecution taskExecutionReturned = taskService.runRequest(request);

        // Setup assert params
        Optional<Long> executionIdOptional = Optional.ofNullable(taskExecutionReturned.getId());
        long executionId = executionIdOptional.orElse(-1L);

        Optional<Long> taskIdOptional = Optional.ofNullable(taskExecutionReturned.getTask().getId());
        long taskId = taskIdOptional.orElse(-1L);

        Assert.assertNotNull(taskExecutionReturned);
        Assert.assertEquals(283472L, executionId);
        Assert.assertEquals(1928L, taskId);
        Assert.assertEquals(3, taskExecutionReturned.getLogs().size());
        Assert.assertEquals("Successfully executed linked task [id=120]", taskExecutionReturned.getLogs().get(1).getOutput());
        Mockito.verify(jmsTemplate, Mockito.times(6)).convertAndSend(eq("taskexecution_destination"), Mockito.anyString(), Mockito.any());
    }
}
