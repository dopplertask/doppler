package com.dopplertask.doppler.service;

import com.dopplertask.doppler.dao.TaskDao;
import com.dopplertask.doppler.dao.TaskExecutionDao;
import com.dopplertask.doppler.domain.ActionPort;
import com.dopplertask.doppler.domain.ActionPortType;
import com.dopplertask.doppler.domain.Connection;
import com.dopplertask.doppler.domain.Task;
import com.dopplertask.doppler.domain.TaskExecution;
import com.dopplertask.doppler.domain.TaskExecutionStatus;
import com.dopplertask.doppler.domain.TaskParameter;
import com.dopplertask.doppler.domain.action.Action;
import com.dopplertask.doppler.domain.action.common.LinkedTaskAction;
import com.dopplertask.doppler.dto.TaskCreationDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.hibernate.Hibernate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class TaskServiceImpl implements TaskService {

    private static final Logger LOG = LoggerFactory.getLogger(TaskServiceImpl.class);
    private static final String DOPPLERTASK_WORKFLOW_UPLOAD = "https://www.dopplertask.com/submitworkflow.php";
    private static final String DOPPLERTASK_LOGIN = "https://www.dopplertask.com/logincheck.php";

    @Autowired
    private JmsTemplate jmsTemplate;

    @Autowired
    private TaskDao taskDao;

    @Autowired
    private TaskExecutionDao taskExecutionDao;

    @Autowired
    private ExecutionService executionService;


    @Override
    public TaskExecution delegate(TaskRequest request) {
        LOG.info("Delegating action");

        TaskExecution execution = new TaskExecution();
        // Create execution record
        taskExecutionDao.save(execution);

        TaskExecutionRequest taskExecutionRequest = new TaskExecutionRequest();
        taskExecutionRequest.setTaskName(request.getTaskName());
        taskExecutionRequest.setParameters(request.getParameters());
        taskExecutionRequest.setExecutionId(execution.getId());
        taskExecutionRequest.setChecksum(request.getChecksum());

        jmsTemplate.convertAndSend("automation_destination", taskExecutionRequest);

        return execution;
    }

    @Override
    @JmsListener(destination = "automation_destination", containerFactory = "jmsFactory")
    public void handleAutomationRequest(TaskExecutionRequest automationRequest) {
        runRequest(automationRequest);
    }

    @Override
    public TaskExecution runRequest(TaskExecutionRequest taskExecutionRequest) {
        TaskExecution execution = executionService.startExecution(taskExecutionRequest, this);

        if (execution != null) {
            if (execution.getStatus() == TaskExecutionStatus.FAILED) {
                return execution;
            } else {
                return executionService.processActions(execution.getTask().getId(), execution.getId(), this);
            }
        }
        return null;
    }

    @Override
    @Transactional
    public List<Task> getAllTasks() {
        List<Task> tasks = taskDao.findAll();
        tasks.forEach(task -> Hibernate.initialize(task.getActionList()));
        return tasks;
    }

    @Override
    @Transactional
    public Long createTask(String name, List<TaskParameter> taskParameters, List<Action> actions, String description, List<Connection> connections, String checksum) {
        return createTask(name, taskParameters, actions, description, connections, checksum, true);
    }

    @Override
    @Transactional
    public Long createTask(String name, List<TaskParameter> taskParameters, List<Action> actions, String description, List<Connection> connections, String checksum, boolean buildTask) {

        if (name.contains(" ")) {
            throw new WhiteSpaceInNameException("Could not create task. Task name contains whitespace.");
        }

        // If we try to add the same task again, we just return the current id.
        Optional<Task> existingTask = taskDao.findByChecksum(checksum);
        if (existingTask.isPresent()) {
            return existingTask.get().getId();
        }

        Task task = new Task();

        task.setName(name);

        Map<String, ActionPort> portMap = new HashMap<>();
        actions.forEach(action -> {
            action.setTask(task);

            // Find task or download it if necessary and assign checksum
            if (action instanceof LinkedTaskAction) {
                if (((LinkedTaskAction) action).getName() != null && !((LinkedTaskAction) action).getName().isEmpty()) {
                    Optional<Task> linkedTask = executionService.findOrDownloadByName(((LinkedTaskAction) action).getName(), this);

                    if (linkedTask.isPresent()) {
                        ((LinkedTaskAction) action).setChecksum(linkedTask.get().getChecksum());
                    } else {
                        throw new LinkedTaskNotFoundException("Linked Task could not be found locally or in the public hub [name=" + ((LinkedTaskAction) action).getName() + "]");
                    }
                } else {
                    throw new LinkedTaskNotFoundException("Linked Task does not have any task name associated");
                }
            }


            for (ActionPort actionPort : action.getPorts()) {
                actionPort.setAction(action);
                portMap.put(actionPort.getExternalId(), actionPort);
            }

        });

        if (taskParameters != null) {
            taskParameters.forEach(taskParameter -> taskParameter.setTask(task));
            task.setTaskParameterList(taskParameters);
        }
        task.setActionList(actions);

        // Check if action is available
        if (task.getStartAction() == null) {
            throw new NoStartActionFoundException("No start action is found in the task. Please create one.");
        }

        task.setCreated(new Date());
        task.setChecksum(checksum);
        task.setDescription(description);

        // Prepare connections
        connections.forEach(connection -> {
            connection.setTask(task);
            if (connection.getSource() != null && connection.getTarget() != null && connection.getSource().getExternalId() != null && connection.getTarget().getExternalId() != null) {
                ActionPort sourcePort = portMap.get(connection.getSource().getExternalId());
                ActionPort targetPort = portMap.get(connection.getTarget().getExternalId());
                if (sourcePort.getPortType() == ActionPortType.OUTPUT && targetPort.getPortType() == ActionPortType.INPUT) {
                    connection.setSource(sourcePort);
                    connection.setTarget(targetPort);
                } else {
                    throw new IncompleteConnectionException("Source port is not an output port or the target port is not an input port.");
                }
            } else {
                throw new IncompleteConnectionException("The connection lacks a source or target port.");
            }
        });

        task.setConnections(connections);

        // Save the new task
        taskDao.save(task);




/*
        actionPortDao.findByExternalIdAndTask()*/

        return task.getId();
    }

    @Override
    public List<TaskExecution> getExecutions() {
        return taskExecutionDao.findAllByTaskNotNull();
    }

    @Override
    @Transactional
    public Task getTask(long id) {
        Optional<Task> taskOptional = taskDao.findById(id);
        if (taskOptional.isPresent()) {
            Task task = taskOptional.get();
            Hibernate.initialize(task.getActionList());
            return task;
        }
        return null;
    }

    @Override
    @Transactional
    public boolean loginUser(String username, String password) {
        try {

            String base64credentials = Base64.getEncoder().encodeToString((username + ":" + password).getBytes());
            HttpRequest.Builder builder = HttpRequest.newBuilder()
                    .uri(URI.create(DOPPLERTASK_LOGIN))
                    .timeout(Duration.ofMinutes(1)).setHeader("Authorization", "Basic " + base64credentials);
            builder = builder.GET();
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = builder.build();
            // Get JSON from Hub
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200 && response.body().equals("{\"message\": \"Logged in\"}")) {
                // Update the credentials file
                PrintWriter writer = new PrintWriter(System.getProperty("user.home") + "/.dopplercreds", "UTF-8");
                writer.print(base64credentials);
                writer.close();

                return true;
            }

        } catch (IOException | InterruptedException e) {
            return false;
        }

        return false;
    }

    @Override
    @Transactional
    public boolean pushTask(String taskName) {
        Optional<Task> taskOptional = taskDao.findFirstByNameOrderByCreatedDesc(taskName);
        if (taskOptional.isPresent()) {
            Task task = taskOptional.get();

            TaskCreationDTO dto = new TaskCreationDTO(task.getName(), task.getTaskParameterList(), task.getActionList(), task.getDescription(), task.getConnections());

            ObjectMapper mapper = new ObjectMapper();
            try {
                String compactJSON = mapper.writeValueAsString(dto);

                // Read credentials
                String credentials = Files.readString(Paths.get(System.getProperty("user.home") + "/.dopplercreds")).replace("\n", "");

                HttpRequest.Builder builder = HttpRequest.newBuilder()
                        .uri(URI.create(DOPPLERTASK_WORKFLOW_UPLOAD))
                        // TODO: Make this authentication based on input from the CLI.
                        .timeout(Duration.ofMinutes(1)).setHeader("Authorization", "Basic " + credentials);
                builder = builder.POST(HttpRequest.BodyPublishers.ofString(compactJSON));
                HttpClient client = HttpClient.newHttpClient();
                HttpRequest request = builder.build();
                // Get JSON from Hub
                HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

                if (response.statusCode() == 200 && response.body().equals("{\"message\": \"Successfully uploaded workflow.\"}")) {
                    // Successful upload
                    return true;
                } else if (response.body().equals("{\"message\": \"Could not authenticate user.\"}")) {
                    throw new AuthenticationException("Task could not be uploaded. You've provided wrong credentials.");
                } else if (response.body().equals("{\"message\": \"A workflow with the same checksum exists. Aborting.\"}")) {
                    throw new TaskAlreadyUploadedException("This task is already uploaded.");
                }

            } catch (IOException | InterruptedException e) {
                throw new UploadNotSuccessfulException("Task could not be uploaded. Check that you've logged in, or that you have an internet connection.");
            }
        }
        throw new TaskNotFoundException("Task could not be found");
    }

    @Override
    @Transactional
    public Task deleteTask(String taskNameOrChecksum) {
        // Find task by checksum if input is longer than 1 characters.
        if (taskNameOrChecksum.length() > 1) {
            Optional<Task> taskByChecksum = taskDao.findFirstByChecksumStartingWith(taskNameOrChecksum);
            if (taskByChecksum.isPresent()) {
                taskDao.delete(taskByChecksum.get());
                return taskByChecksum.get();
            }
        }

        // Find task by name
        Optional<Task> taskByName = taskDao.findFirstByNameOrderByCreatedDesc(taskNameOrChecksum);
        if (taskByName.isPresent()) {
            taskDao.delete(taskByName.get());
            return taskByName.get();
        }

        throw new TaskNotFoundException("Task could not be found.");
    }

    @Override
    public TaskExecution runRequest(TaskRequest request) {
        TaskExecution execution = new TaskExecution();
        execution.setDepth(request.getDepth());
        // Create execution record
        // Always save to allow consumer to get a message
        taskExecutionDao.save(execution);

        TaskExecutionRequest taskExecutionRequest = new TaskExecutionRequest();
        taskExecutionRequest.setTaskName(request.getTaskName());
        taskExecutionRequest.setParameters(request.getParameters());
        taskExecutionRequest.setExecutionId(execution.getId());
        taskExecutionRequest.setDepth(request.getDepth());
        taskExecutionRequest.setChecksum(request.getChecksum());

        return this.runRequest(taskExecutionRequest);
    }
}
