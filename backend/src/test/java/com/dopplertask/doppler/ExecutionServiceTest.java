package com.dopplertask.doppler;

import com.dopplertask.doppler.dao.TaskDao;
import com.dopplertask.doppler.dao.TaskExecutionDao;
import com.dopplertask.doppler.domain.Task;
import com.dopplertask.doppler.domain.TaskExecution;
import com.dopplertask.doppler.domain.action.common.SetVariable;
import com.dopplertask.doppler.domain.action.common.SetVariableAction;
import com.dopplertask.doppler.service.ExecutionServiceImpl;
import com.dopplertask.doppler.service.TaskExecutionRequest;
import com.dopplertask.doppler.service.TaskServiceImpl;
import com.dopplertask.doppler.service.VariableExtractorUtil;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
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
    private VariableExtractorUtil variableExtractorUtil;

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
        TaskExecutionRequest request = createTaskRequest("ExampleTask", null, 10L);

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

        TaskExecution resultExecution = executionService.processActions(exampleTask.getId(), execution.getId(), null);

        // Check that we have everything correct
        Assert.assertEquals(10L, resultExecution.getId().longValue());
        Assert.assertEquals(exampleTask, resultExecution.getTask());
    }

    @Test
    public void testProcessActionsShouldReturnExecutionWithNewParams() throws IOException {
        // Prepare Task
        Task exampleTask = new Task();
        exampleTask.setId(20L);
        exampleTask.setName("ExampleTask");
        SetVariableAction setVariableAction = new SetVariableAction();
        SetVariable setVariable = new SetVariable();
        setVariable.setName("testVar");
        setVariable.setValue("testValue One two three $executionId");
        setVariableAction.setSetVariableList(List.of(setVariable));
        exampleTask.getActionList().add(setVariableAction);
        Optional<Task> exampleTaskOptional = Optional.of(exampleTask);

        // Prepare Execution
        TaskExecution execution = new TaskExecution();
        execution.setId(10L);
        execution.setTask(exampleTask);
        Optional<TaskExecution> executionOptional = Optional.of(execution);

        // Assign values
        when(taskDao.findById(eq(20L))).thenReturn(exampleTaskOptional);
        when(taskExecutionDao.findById(eq(10L))).thenReturn(executionOptional);
        when(variableExtractorUtil.extract(eq("testValue One two three $executionId"), eq(execution), eq(setVariableAction.getScriptLanguage()))).thenReturn("testValue One two three 10");
        TaskExecution resultExecution = executionService.processActions(exampleTask.getId(), execution.getId(), null);

        // Check that we have everything correct
        Assert.assertEquals(10L, resultExecution.getId().longValue());
        Assert.assertEquals(exampleTask, resultExecution.getTask());
        Assert.assertEquals(1, resultExecution.getParameters().size());
        Assert.assertEquals("testValue One two three 10", resultExecution.getParameters().get("testVar"));
    }


    @Test
    public void testStartExecutionByChecksumAndTaskNameShouldReturnExecution() {
        // Prepare Task
        Task exampleTask = new Task();
        exampleTask.setName("ExampleTask");
        exampleTask.setChecksum("123123");
        Optional<Task> exampleTaskOptional = Optional.of(exampleTask);

        // Prepare Execution
        TaskExecution execution = new TaskExecution();
        execution.setId(10L);
        Optional<TaskExecution> executionOptional = Optional.of(execution);

        // Assign values
        when(taskDao.findFirstByNameOrderByCreatedDesc(eq("ExampleTask"))).thenReturn(exampleTaskOptional);
        when(taskDao.findFirstByChecksumStartingWith(eq("123123"))).thenReturn(exampleTaskOptional);
        when(taskExecutionDao.findById(eq(10L))).thenReturn(executionOptional);

        // Run test with data
        TaskExecutionRequest request = createTaskRequest("ExampleTask", "123123", 10L);

        TaskExecution resultExecution = executionService.startExecution(request, taskService);

        // Check that we have everything correct
        Assert.assertEquals(10L, resultExecution.getId().longValue());
        Assert.assertEquals(exampleTask, resultExecution.getTask());
    }

    @Test
    public void testStartExecutionByChecksumShouldReturnExecution() {
        // Prepare Task
        Task exampleTask = new Task();
        exampleTask.setName("TaskName");
        exampleTask.setChecksum("123123");
        Optional<Task> exampleTaskOptional = Optional.of(exampleTask);

        // Prepare Execution
        TaskExecution execution = new TaskExecution();
        execution.setId(10L);
        Optional<TaskExecution> executionOptional = Optional.of(execution);

        // Assign values
        when(taskDao.findFirstByNameOrderByCreatedDesc(eq("ExampleTask"))).thenReturn(exampleTaskOptional);
        when(taskDao.findFirstByChecksumStartingWith(eq("123123"))).thenReturn(exampleTaskOptional);
        when(taskExecutionDao.findById(eq(10L))).thenReturn(executionOptional);

        // Run test with data
        TaskExecutionRequest request = createTaskRequest("123123", "123123", 10L);

        TaskExecution resultExecution = executionService.startExecution(request, taskService);

        // Check that we have everything correct
        Assert.assertEquals(10L, resultExecution.getId().longValue());
        Assert.assertEquals(exampleTask, resultExecution.getTask());
    }

    @Test
    public void testStartExecutionByTaskNameShouldReturnExecution() {
        // Prepare Task
        Task exampleTask = new Task();
        exampleTask.setName("ExampleTask");
        exampleTask.setChecksum("123123");
        Optional<Task> exampleTaskOptional = Optional.of(exampleTask);

        // Prepare Execution
        TaskExecution execution = new TaskExecution();
        execution.setId(10L);
        Optional<TaskExecution> executionOptional = Optional.of(execution);

        // Assign values
        when(taskDao.findFirstByNameOrderByCreatedDesc(eq("ExampleTask"))).thenReturn(exampleTaskOptional);
        when(taskDao.findFirstByChecksumStartingWith(eq("123123"))).thenReturn(exampleTaskOptional);
        when(taskExecutionDao.findById(eq(10L))).thenReturn(executionOptional);

        // Run test with data
        TaskExecutionRequest request = createTaskRequest("ExampleTask", "ExampleTask", 10L);

        TaskExecution resultExecution = executionService.startExecution(request, taskService);

        // Check that we have everything correct
        Assert.assertEquals(10L, resultExecution.getId().longValue());
        Assert.assertEquals(exampleTask, resultExecution.getTask());
    }

    @Test
    public void testPullNonExistentTaskShouldReturnEmptyOptional() {
        Optional<Task> task = executionService.pullTask("Test", taskService);

        Assert.assertEquals(true, task.isEmpty());
    }

    @Test
    public void testPullNonExistentOnlineTaskShouldReturnLocalExistingTask() {
        Task exampleTask = new Task();
        exampleTask.setName("ExampleTask");
        exampleTask.setChecksum("123123");
        Optional<Task> exampleTaskOptional = Optional.of(exampleTask);
        when(taskDao.findFirstByNameOrderByCreatedDesc(eq("ExampleTask"))).thenReturn(exampleTaskOptional);

        Optional<Task> task = executionService.pullTask("ExampleTask", taskService);

        Assert.assertEquals(true, task.isPresent());
        Assert.assertEquals("123123", task.get().getChecksum());
    }

    private TaskExecutionRequest createTaskRequest(String taskName, String checksum, long executionId) {
        TaskExecutionRequest request = new TaskExecutionRequest();
        request.setTaskName(taskName);
        request.setChecksum(checksum);
        request.setExecutionId(executionId);
        request.setParameters(new HashMap<>());

        return request;
    }
}
