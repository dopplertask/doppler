package com.dopplertask.doppler;

import com.dopplertask.doppler.dao.TaskDao;
import com.dopplertask.doppler.dao.TaskExecutionDao;
import com.dopplertask.doppler.domain.Task;
import com.dopplertask.doppler.domain.TaskExecution;
import com.dopplertask.doppler.service.ExecutionServiceImpl;
import com.dopplertask.doppler.service.TaskExecutionRequest;
import com.dopplertask.doppler.service.TaskServiceImpl;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.HashMap;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@RunWith(SpringRunner.class)
public class ExecutionServiceTest {

    @InjectMocks
    private ExecutionServiceImpl executionService;

    @Mock
    private TaskServiceImpl taskService;

    @Mock
    private TaskDao taskDao;

    @Mock
    private TaskExecutionDao taskExecutionDao;

    @Mock
    private JmsTemplate jmsTemplate;

    @Test
    public void testStartExecutionShouldReturnExecution() {
        // Prepare Task
        Task exampleTask = new Task();
        exampleTask.setName("ExampleTask");
        Optional<Task> exampleTaskOptional = Optional.of(exampleTask);

        // Prepare Execution
        TaskExecution execution = new TaskExecution();
        execution.setId(10L);
        Optional<TaskExecution> executionOptional = Optional.of(execution);

        // Assign values
        when(taskDao.findFirstByNameOrderByCreatedDesc(eq("ExampleTask"))).thenReturn(exampleTaskOptional);
        when(taskExecutionDao.findById(eq(10L))).thenReturn(executionOptional);

        // Run test with data
        TaskExecutionRequest request = new TaskExecutionRequest();
        request.setTaskName("ExampleTask");
        request.setExecutionId(10L);
        request.setParameters(new HashMap<>());

        TaskExecution resultExecution = executionService.startExecution(request, taskService);

        // Check that we have everything correct
        Assert.assertEquals(10L, resultExecution.getId().longValue());
        Assert.assertEquals(exampleTask, resultExecution.getTask());
    }

    @Test
    public void testProcessActionsShouldReturnExecution() {
        // Prepare Task
        Task exampleTask = new Task();
        exampleTask.setId(20L);
        exampleTask.setName("ExampleTask");
        Optional<Task> exampleTaskOptional = Optional.of(exampleTask);

        // Prepare Execution
        TaskExecution execution = new TaskExecution();
        execution.setId(10L);
        execution.setTask(exampleTask);
        Optional<TaskExecution> executionOptional = Optional.of(execution);

        // Assign values
        when(taskDao.findById(eq(20L))).thenReturn(exampleTaskOptional);
        when(taskExecutionDao.findById(eq(10L))).thenReturn(executionOptional);

        // Run test with data
        TaskExecutionRequest request = new TaskExecutionRequest();
        request.setTaskName("ExampleTask");
        request.setExecutionId(10L);
        request.setParameters(new HashMap<>());

        TaskExecution resultExecution = executionService.processActions(exampleTask.getId(), execution.getId(), null);

        // Check that we have everything correct
        Assert.assertEquals(10L, resultExecution.getId().longValue());
        Assert.assertEquals(exampleTask, resultExecution.getTask());
    }
}
