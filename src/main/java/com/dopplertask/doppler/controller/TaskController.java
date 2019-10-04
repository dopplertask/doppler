package com.dopplertask.doppler.controller;

import com.dopplertask.doppler.domain.Task;
import com.dopplertask.doppler.domain.TaskExecution;
import com.dopplertask.doppler.domain.TaskExecutionLog;
import com.dopplertask.doppler.domain.action.Action;
import com.dopplertask.doppler.dto.SimpleChecksumResponseDto;
import com.dopplertask.doppler.dto.SimpleIdResponseDto;
import com.dopplertask.doppler.dto.TaskCreationDTO;
import com.dopplertask.doppler.dto.TaskExecutionDTO;
import com.dopplertask.doppler.dto.TaskExecutionListDTO;
import com.dopplertask.doppler.dto.TaskExecutionLogResponseDTO;
import com.dopplertask.doppler.dto.TaskRequestDTO;
import com.dopplertask.doppler.dto.TaskResponseDTO;
import com.dopplertask.doppler.service.TaskRequest;
import com.dopplertask.doppler.service.TaskService;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

@RestController
public class TaskController {

    @Autowired
    private TaskService taskService;

    private static String bytesToHex(byte[] hash) {
        StringBuffer hexString = new StringBuffer();
        for (int i = 0; i < hash.length; i++) {
            String hex = Integer.toHexString(0xff & hash[i]);
            if (hex.length() == 1) hexString.append('0');
            hexString.append(hex);
        }
        return hexString.toString();
    }

    @PostMapping(path = "/schedule/task")
    public ResponseEntity<SimpleIdResponseDto> scheduleTask(@RequestBody TaskRequestDTO taskRequestDTO) {
        TaskRequest request = new TaskRequest(taskRequestDTO.getTaskName(), taskRequestDTO.getParameters());
        request.setChecksum(taskRequestDTO.getTaskName());
        TaskExecution taskExecution = taskService.delegate(request);

        if (taskExecution != null) {
            SimpleIdResponseDto idResponseDto = new SimpleIdResponseDto();
            idResponseDto.setId(String.valueOf(taskExecution.getId()));
            return new ResponseEntity<>(idResponseDto, HttpStatus.OK);
        }

        return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
    }

    @PostMapping(path = "/schedule/directtask")
    public ResponseEntity<TaskExecutionLogResponseDTO> runTask(@RequestBody TaskRequestDTO taskRequestDTO) {
        TaskRequest request = new TaskRequest(taskRequestDTO.getTaskName(), taskRequestDTO.getParameters());
        request.setChecksum(taskRequestDTO.getTaskName());
        TaskExecution execution = taskService.runRequest(request);

        TaskExecutionLogResponseDTO responseDTO = new TaskExecutionLogResponseDTO();
        if (execution != null) {
            for (TaskExecutionLog log : execution.getLogs()) {
                responseDTO.getOutput().add(log.getOutput());
            }

            return new ResponseEntity<>(responseDTO, HttpStatus.OK);
        }

        return new ResponseEntity<>(HttpStatus.BAD_REQUEST);

    }

    @PostMapping(path = "/task")
    public ResponseEntity<SimpleChecksumResponseDto> createTask(@RequestBody String body) throws IOException, NoSuchAlgorithmException {

        // Translate JSON to object
        ObjectMapper mapper = new ObjectMapper();
        mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        TaskCreationDTO taskCreationDTO = mapper.readValue(body, TaskCreationDTO.class);

        // Generate compact JSON
        String compactJSON = mapper.writeValueAsString(taskCreationDTO);

        // Create checksum
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] encodedhash = digest.digest(compactJSON.getBytes(StandardCharsets.UTF_8));
        String sha3_256hex = bytesToHex(encodedhash);

        List<Action> actions = taskCreationDTO.getActions();
        
        Long id = taskService.createTask(taskCreationDTO.getName(), actions, sha3_256hex);

        if (id != null) {
            SimpleChecksumResponseDto checksumResponseDto = new SimpleChecksumResponseDto();
            checksumResponseDto.setChecksum(sha3_256hex);
            return new ResponseEntity<>(checksumResponseDto, HttpStatus.OK);
        }

        return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
    }

    @GetMapping("/task")
    public ResponseEntity<List<TaskResponseDTO>> getTasks() {
        List<Task> tasks = taskService.getAllTasks();
        List<TaskResponseDTO> taskResponseDTOList = new ArrayList<>();

        for (Task task : tasks) {
            TaskResponseDTO taskDto = new TaskResponseDTO();
            taskDto.setId(task.getId());
            taskDto.setName(task.getName());
            taskDto.setCreated(task.getCreated());

            taskResponseDTOList.add(taskDto);
        }

        return new ResponseEntity<>(taskResponseDTOList, HttpStatus.OK);
    }

    @GetMapping("/task/{id}")
    public ResponseEntity<TaskResponseDTO> getTask(@PathVariable("id") long id) {
        Task task = taskService.getTask(id);
        if (task != null) {
            TaskResponseDTO taskDto = new TaskResponseDTO();
            taskDto.setId(task.getId());
            taskDto.setName(task.getName());
            taskDto.setCreated(task.getCreated());
            taskDto.setActions(task.getActionList());

            return new ResponseEntity<>(taskDto, HttpStatus.OK);
        }

        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    @GetMapping("/executions")
    public ResponseEntity<TaskExecutionListDTO> getExecutions() {
        List<TaskExecution> executions = taskService.getExecutions();
        List<TaskExecutionDTO> executionDTOList = new ArrayList<>();
        executions.forEach(item -> {
            TaskExecutionDTO dto = new TaskExecutionDTO();
            dto.setStatus(item.getStatus().name());
            dto.setStartDate(item.getStartdate());
            dto.setEndDate(item.getEnddate());
            dto.setExecutionId(Long.toString(item.getId()));
            dto.setTaskName(item.getTask().getName() != null ? item.getTask().getName() : Long.toString(item.getTask().getId()));
            executionDTOList.add(dto);
        });

        TaskExecutionListDTO executionListDTO = new TaskExecutionListDTO();
        executionListDTO.setExecutions(executionDTOList);

        return new ResponseEntity<>(executionListDTO, HttpStatus.OK);
    }

}
