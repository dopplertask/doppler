package com.dopplertask.doppler.service;

import com.dopplertask.doppler.dao.TaskDao;
import com.dopplertask.doppler.dao.TaskExecutionDao;
import com.dopplertask.doppler.domain.ActionResult;
import com.dopplertask.doppler.domain.StatusCode;
import com.dopplertask.doppler.domain.Task;
import com.dopplertask.doppler.domain.TaskExecution;
import com.dopplertask.doppler.domain.TaskExecutionLog;
import com.dopplertask.doppler.domain.TaskExecutionStatus;
import com.dopplertask.doppler.domain.action.Action;
import com.dopplertask.doppler.dto.TaskCreationDTO;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.util.Date;
import java.util.Map;
import java.util.Optional;

@Service
public class ExecutionServiceImpl implements ExecutionService {


    private static final Logger LOG = LoggerFactory.getLogger(ExecutionServiceImpl.class);
    private static final String DOPPLERTASK_WORKFLOW_DOWNLOAD = "http://www.dopplertask.com/getworkflow.php";

    @Autowired
    private JmsTemplate jmsTemplate;

    @Autowired
    private TaskDao taskDao;


    @Autowired
    private TaskExecutionDao taskExecutionDao;

    @Transactional
    public TaskExecution startExecution(TaskExecutionRequest taskExecutionRequest, TaskService taskService) {
        Optional<Task> taskRequest = taskExecutionRequest.getChecksum() == null || taskExecutionRequest.getChecksum().isEmpty() ? findOrDownloadByName(taskExecutionRequest.getTaskName(), taskService) : findOrDownloadByChecksum(taskExecutionRequest.getChecksum(), taskService);
        Optional<TaskExecution> executionReq = taskExecutionDao.findById(taskExecutionRequest.getExecutionId());
        if (taskRequest.isPresent() && executionReq.isPresent()) {
            Task task = taskRequest.get();
            TaskExecution execution = executionReq.get();

            // Assign task to execution
            execution.setTask(task);

            // Populate variables
            execution.getParameters().putAll(taskExecutionRequest.getParameters());

            execution.setStartdate(new Date());
            execution.setStatus(TaskExecutionStatus.STARTED);

            TaskExecutionLog executionStarted = new TaskExecutionLog();
            executionStarted.setTaskExecution(execution);
            executionStarted.setOutput("Task execution started [taskId=" + task.getId() + ", executionId=" + execution.getId() + "]");
            execution.addLog(executionStarted);


            taskExecutionDao.save(execution);

            LOG.info("Task execution started [taskId={}, executionId={}]", task.getId(), execution.getId());


            broadcastResults(executionStarted);

            return execution;
        } else {
            LOG.warn("Task could not be found [taskId={}]", taskExecutionRequest.getTaskName());

            TaskExecution taskExecution = new TaskExecution();
            taskExecution.setId(0L);
            TaskExecutionLog noTaskLog = new TaskExecutionLog();
            noTaskLog.setOutput("Task could not be found [taskId=" + taskExecutionRequest.getTaskName() + "]");
            broadcastResults(noTaskLog);
            return null;
        }
    }

    public Optional<Task> findOrDownloadByName(String taskName, TaskService taskService) {
        Optional<Task> task = taskDao.findFirstByNameOrderByCreatedDesc(taskName);

        if (task.isPresent()) {
            LOG.info("Found task with with name: {}", taskName);
            return task;
        } else {
            // Try to download it
            LOG.info("Trying to download task with name: {}", taskName);

            // Persist with checksum
            HttpRequest.Builder builder = HttpRequest.newBuilder()
                    .uri(URI.create(DOPPLERTASK_WORKFLOW_DOWNLOAD + "?name=" + taskName))
                    .timeout(Duration.ofMinutes(1));
            builder = builder.GET();
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = builder.build();
            try {
                // Get JSON from Hub
                HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

                if (response.statusCode() == 200 && response.body() != null && !response.body().isEmpty() && taskService != null) {

                    // Translate JSON to object
                    ObjectMapper mapper = new ObjectMapper();
                    mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
                    TaskCreationDTO taskCreationDTO = mapper.readValue(response.body(), TaskCreationDTO.class);

                    // Create checksum
                    MessageDigest digest = MessageDigest.getInstance("SHA-256");
                    byte[] encodedhash = digest.digest(response.body().getBytes(StandardCharsets.UTF_8));
                    String sha3_256hex = bytesToHex(encodedhash);

                    Long onlineTaskId = taskService.createTask(taskCreationDTO.getName(), taskCreationDTO.getActions(), sha3_256hex);

                    return taskDao.findById(onlineTaskId);
                }
            } catch (IOException | InterruptedException | NoSuchAlgorithmException e) {
                LOG.error("Exception: {}", e);
            }

            return Optional.empty();
        }

    }

    private Optional<Task> findOrDownloadByChecksum(String checksum, TaskService taskService) {
        Optional<Task> task = taskDao.findByChecksum(checksum);

        if (task.isPresent()) {
            LOG.info("Found task with checksum: {}", checksum);
            return task;
        } else {
            // Try to download it
            LOG.info("Trying to download task with checksum: {}", checksum);

            //TODO: Persist with checksum
            // Persist with checksum
            HttpRequest.Builder builder = HttpRequest.newBuilder()
                    .uri(URI.create(DOPPLERTASK_WORKFLOW_DOWNLOAD + "?checksum=" + checksum))
                    .timeout(Duration.ofMinutes(1));
            builder = builder.GET();
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = builder.build();
            try {
                // Get JSON from Hub
                HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

                if (response.statusCode() == 200 && response.body() != null && !response.body().isEmpty() && taskService != null) {

                    // Translate JSON to object
                    ObjectMapper mapper = new ObjectMapper();
                    mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
                    TaskCreationDTO taskCreationDTO = mapper.readValue(response.body(), TaskCreationDTO.class);

                    // Create checksum
                    MessageDigest digest = MessageDigest.getInstance("SHA-256");
                    byte[] encodedhash = digest.digest(response.body().getBytes(StandardCharsets.UTF_8));
                    String sha3_256hex = bytesToHex(encodedhash);

                    Long onlineTaskId = taskService.createTask(taskCreationDTO.getName(), taskCreationDTO.getActions(), sha3_256hex);

                    return taskDao.findById(onlineTaskId);
                }
            } catch (IOException | InterruptedException | NoSuchAlgorithmException e) {
                LOG.error("Exception: {}", e);
            }

            return Optional.empty();
        }
    }


    private static String bytesToHex(byte[] hash) {
        StringBuffer hexString = new StringBuffer();
        for (int i = 0; i < hash.length; i++) {
            String hex = Integer.toHexString(0xff & hash[i]);
            if (hex.length() == 1) hexString.append('0');
            hexString.append(hex);
        }
        return hexString.toString();
    }

    @Override
    @Transactional
    public TaskExecution processActions(Long taskId, Long executionId, TaskService taskService) {
        Optional<Task> taskRequest = taskDao.findById(taskId);
        Optional<TaskExecution> executionReq = taskExecutionDao.findById(executionId);
        if (taskRequest.isPresent() && executionReq.isPresent()) {
            Task task = taskRequest.get();
            TaskExecution execution = executionReq.get();

            // Start processing task
            for (Action currentAction : task.getActionList()) {

                ActionResult actionResult = new ActionResult();
                try {
                    actionResult = currentAction.run(taskService, execution);
                } catch (Exception e) {
                    LOG.error("Exception occured: {}", e);
                }

                TaskExecutionLog log = new TaskExecutionLog();
                log.setTaskExecution(execution);

                LOG.info("Ran current action: {} with status code: {} and with result: {}", currentAction.getClass().getSimpleName(), actionResult.getStatusCode(), actionResult.getOutput());

                log.setOutput(actionResult.getOutput());
                log.setOutputType(actionResult.getOutputType());

                // If action did not go well
                if (actionResult.getStatusCode() == StatusCode.FAILURE) {
                    log.setOutput(actionResult.getErrorMsg());
                    log.setOutputType(actionResult.getOutputType());
                    execution.setSuccess(false);
                    execution.addLog(log);
                    broadcastResults(log);
                    break;
                }

                // Add log to the execution
                execution.addLog(log);
                broadcastResults(log);
            }

            TaskExecutionLog executionCompleted = new TaskExecutionLog();
            executionCompleted.setTaskExecution(execution);
            executionCompleted.setOutput("Task execution completed [taskId=" + task.getId() + ", executionId=" + execution.getId() + "]");
            execution.addLog(executionCompleted);
            broadcastResults(executionCompleted);

            LOG.info("Task execution completed [taskId={}, executionId={}]", task.getId(), execution.getId());

            execution.setEnddate(new Date());
            execution.setStatus(execution.isSuccess() ? TaskExecutionStatus.FINISHED : TaskExecutionStatus.FAILED);


            return execution;
        }
        return null;
    }

    private void broadcastResults(TaskExecutionLog taskExecutionLog) {
        BroadcastResult result = new BroadcastResult(taskExecutionLog.getOutput(), taskExecutionLog.getOutputType());

        jmsTemplate.convertAndSend("taskexecution_destination", result, message -> {
            message.setLongProperty("executionId", taskExecutionLog.getTaskExecution().getId());
            return message;
        });
    }

}
