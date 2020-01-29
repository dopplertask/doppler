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
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.*;

@Service
public class ExecutionServiceImpl implements ExecutionService {


    private static final Logger LOG = LoggerFactory.getLogger(ExecutionServiceImpl.class);
    private static final String DOPPLERTASK_WORKFLOW_DOWNLOAD = "https://www.dopplertask.com/getworkflow.php";

    private JmsTemplate jmsTemplate;
    private TaskDao taskDao;
    private VariableExtractorUtil variableExtractorUtil;
    private TaskExecutionDao taskExecutionDao;
    private Executor executor;

    @Autowired
    public ExecutionServiceImpl(JmsTemplate jmsTemplate, TaskDao taskDao, VariableExtractorUtil variableExtractorUtil, TaskExecutionDao taskExecutionDao) {
        this.jmsTemplate = jmsTemplate;
        this.taskDao = taskDao;
        this.variableExtractorUtil = variableExtractorUtil;
        this.taskExecutionDao = taskExecutionDao;
        this.executor = Executors.newSingleThreadExecutor();
    }

    private static String bytesToHex(byte[] hash) {
        StringBuilder hexString = new StringBuilder();
        for (int i = 0; i < hash.length; i++) {
            String hex = Integer.toHexString(0xff & hash[i]);
            if (hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }
        return hexString.toString();
    }

    @Transactional
    public TaskExecution startExecution(TaskExecutionRequest taskExecutionRequest, TaskService taskService) {

        // Look up by checksum first
        Optional<Task> taskRequest = Optional.empty();
        if (taskExecutionRequest.getChecksum() != null && !taskExecutionRequest.getChecksum().isEmpty()) {
            taskRequest = findOrDownloadByChecksum(taskExecutionRequest.getChecksum(), taskService);

            // Search in the local database by taskName
            if (!taskRequest.isPresent() && taskExecutionRequest.getTaskName() != null && !taskExecutionRequest.getTaskName().isEmpty()) {
                taskRequest = findOrDownloadByName(taskExecutionRequest.getTaskName(), taskService);
            }
        } else if (taskExecutionRequest.getTaskName() != null && !taskExecutionRequest.getTaskName().isEmpty()) {
            taskRequest = findOrDownloadByName(taskExecutionRequest.getTaskName(), taskService);
        }

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

            if (executionReq.isPresent()) {
                TaskExecution taskExecution = executionReq.get();
                taskExecution.setId(executionReq.get().getId());
                taskExecution.setSuccess(false);
                TaskExecutionLog noTaskLog = new TaskExecutionLog();
                noTaskLog.setOutput("Task could not be found [taskId=" + taskExecutionRequest.getTaskName() + "]");
                noTaskLog.setTaskExecution(taskExecution);
                broadcastResults(noTaskLog, true);
            }
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
                    String sha256 = bytesToHex(encodedhash);

                    //TODO: check that there is no other checksum with the same value in the DB
                    Long onlineTaskId = taskService.createTask(taskCreationDTO.getName(), taskCreationDTO.getActions(), taskCreationDTO.getDescription(), sha256);

                    return taskDao.findById(onlineTaskId);
                }
            } catch (IOException | InterruptedException | NoSuchAlgorithmException e) {
                LOG.error("Exception: {}", e);
            }

            return Optional.empty();
        }

    }

    @Override
    public Optional<TaskExecution> getExecution(long id) {
        return taskExecutionDao.findById(id);
    }

    @Override
    @Transactional
    public void deleteExecution(long id) {
        Optional<TaskExecution> execution = taskExecutionDao.findById(id);
        if (execution.isPresent()) {
            taskExecutionDao.delete(execution.get());
            return;
        }

        throw new ExecutionNotFoundException("Execution could not be found.");
    }

    @Override
    public Optional<Task> pullTask(String taskName, TaskService taskService) {
        // Try to download it
        LOG.info("Pulling task with name: {}", taskName);

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
                String sha256 = bytesToHex(encodedhash);

                // Check current database for existing task with checksum.
                Optional<Task> existingTask = taskDao.findFirstByChecksumStartingWith(sha256);
                if (!existingTask.isPresent()) {
                    Long onlineTaskId = taskService.createTask(taskCreationDTO.getName(), taskCreationDTO.getActions(), taskCreationDTO.getDescription(), sha256, false);
                    return taskDao.findById(onlineTaskId);
                } else {
                    return existingTask;
                }
            }
        } catch (IOException | InterruptedException | NoSuchAlgorithmException e) {
            LOG.error("Exception: {}", e);
        }


        return taskDao.findFirstByNameOrderByCreatedDesc(taskName);
    }

    private Optional<Task> findOrDownloadByChecksum(String checksum, TaskService taskService) {
        Optional<Task> task = taskDao.findFirstByChecksumStartingWith(checksum);

        if (task.isPresent()) {
            LOG.info("Found task with checksum: {}", checksum);
            return task;
        } else if (!checksum.isEmpty() && checksum.length() == 255) {
            // Try to download it
            LOG.info("Trying to download task with checksum: {}", checksum);

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
                    String sha256 = bytesToHex(encodedhash);

                    Long onlineTaskId = taskService.createTask(taskCreationDTO.getName(), taskCreationDTO.getActions(), taskCreationDTO.getDescription(), sha256);

                    return taskDao.findById(onlineTaskId);
                }
            } catch (IOException | InterruptedException | NoSuchAlgorithmException e) {
                LOG.error("Exception: {}", e);
            }

            return Optional.empty();
        }
        return Optional.empty();
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
                int tries = 0;
                do {
                    try {
                        actionResult = currentAction.run(taskService, execution, variableExtractorUtil);

                        // Handle failOn
                        if (currentAction.getFailOn() != null && !currentAction.getFailOn().isEmpty()) {
                            String failOn = variableExtractorUtil.extract(currentAction.getFailOn(), execution);
                            if (failOn != null && !failOn.isEmpty()) {
                                actionResult.setErrorMsg("Failed on: " + failOn);
                                actionResult.setStatusCode(StatusCode.FAILURE);
                            }
                        }
                    } catch (Exception e) {
                        LOG.error("Exception occurred: {}", e);
                        actionResult.setErrorMsg(e.toString());
                        actionResult.setStatusCode(StatusCode.FAILURE);
                    }

                    tries++;
                } while (actionResult.getStatusCode() == StatusCode.FAILURE && currentAction.getRetries() >= tries && !currentAction.isContinueOnFailure());

                TaskExecutionLog log = new TaskExecutionLog();
                log.setTaskExecution(execution);

                LOG.info("Ran current action: {} with status code: {} and with result: {}", currentAction.getClass().getSimpleName(), actionResult.getStatusCode(), actionResult.getOutput() != null && !actionResult.getOutput().isEmpty() ? actionResult.getOutput() : actionResult.getErrorMsg());

                log.setOutput(actionResult.getOutput());
                log.setOutputType(actionResult.getOutputType());

                // If action did not go well
                if (actionResult.getStatusCode() == StatusCode.FAILURE && !currentAction.isContinueOnFailure()) {
                    log.setOutput(actionResult.getErrorMsg());
                    log.setOutputType(actionResult.getOutputType());
                    execution.setSuccess(false);
                    execution.addLog(log);
                    broadcastResults(log);
                    break;
                }

                // Add log to the execution
                execution.addLog(log);

                // Send message to MQ
                broadcastResults(log);
            }

            TaskExecutionLog executionCompleted = new TaskExecutionLog();
            executionCompleted.setTaskExecution(execution);
            executionCompleted.setOutput("Task execution completed [taskId=" + task.getId() + ", executionId=" + execution.getId() + ", success=" + execution.isSuccess() + "]");
            execution.addLog(executionCompleted);
            broadcastResults(executionCompleted, true);

            LOG.info("Task execution completed [taskId={}, executionId={}]", task.getId(), execution.getId());

            execution.setEnddate(new Date());
            execution.setStatus(execution.isSuccess() ? TaskExecutionStatus.FINISHED : TaskExecutionStatus.FAILED);


            return execution;
        }
        return null;
    }

    private void broadcastResults(TaskExecutionLog taskExecutionLog) {
        broadcastResults(taskExecutionLog, false);
    }

    private void broadcastResults(TaskExecutionLog taskExecutionLog, boolean lastMessage) {
        BroadcastResult result = new BroadcastResult(taskExecutionLog.getOutput(), taskExecutionLog.getOutputType());

        executor.execute(() -> jmsTemplate.convertAndSend("taskexecution_destination", result, message -> {
            message.setLongProperty("executionId", taskExecutionLog.getTaskExecution().getId());
            message.setBooleanProperty("lastMessage", lastMessage);
            return message;
        }));
    }

}
