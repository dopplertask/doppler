package com.dopplertask.doppler.controller;

import com.dopplertask.doppler.domain.Task;
import com.dopplertask.doppler.domain.TaskExecution;
import com.dopplertask.doppler.domain.TaskExecutionLog;
import com.dopplertask.doppler.domain.action.Action;
import com.dopplertask.doppler.dto.ActionDTO;
import com.dopplertask.doppler.dto.SimpleIdResponseDto;
import com.dopplertask.doppler.dto.TaskCreationDTO;
import com.dopplertask.doppler.dto.TaskExecutionDTO;
import com.dopplertask.doppler.dto.TaskExecutionListDTO;
import com.dopplertask.doppler.dto.TaskExecutionLogResponseDTO;
import com.dopplertask.doppler.dto.TaskRequestDTO;
import com.dopplertask.doppler.dto.TaskResponseDTO;
import com.dopplertask.doppler.service.TaskRequest;
import com.dopplertask.doppler.service.TaskService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
public class TaskController {

    Logger logger = LoggerFactory.getLogger(TaskController.class);
    @Autowired
    private TaskService taskService;

    @RequestMapping(path = "/schedule/task", method = RequestMethod.POST)
    public ResponseEntity<SimpleIdResponseDto> scheduleTask(@RequestBody TaskRequestDTO taskRequestDTO) {
        TaskRequest request = new TaskRequest(taskRequestDTO.getAutomationId(), taskRequestDTO.getParameters());
        TaskExecution taskExecution = taskService.delegate(request);

        if (taskExecution != null) {
            SimpleIdResponseDto idResponseDto = new SimpleIdResponseDto();
            idResponseDto.setId(String.valueOf(taskExecution.getId()));
            return new ResponseEntity<>(idResponseDto, HttpStatus.OK);
        }

        return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
    }


    @RequestMapping(path = "/schedule/directtask", method = RequestMethod.POST)
    public ResponseEntity<TaskExecutionLogResponseDTO> runTask(@RequestBody TaskRequestDTO taskRequestDTO) {
        TaskRequest request = new TaskRequest(taskRequestDTO.getAutomationId(), taskRequestDTO.getParameters());
        TaskExecution execution = taskService.runRequest(request);

        TaskExecutionLogResponseDTO responseDTO = new TaskExecutionLogResponseDTO();
        for (TaskExecutionLog log : execution.getLogs()) {
            responseDTO.getOutput().add(log.getOutput());
        }

        return new ResponseEntity<>(responseDTO, HttpStatus.OK);
    }

    @RequestMapping(path = "/task", method = RequestMethod.POST)
    public ResponseEntity<SimpleIdResponseDto> createTask(@RequestBody TaskCreationDTO taskCreationDTO) {

        List<Action> actions = createActions(taskCreationDTO);

        Long id = taskService.createTask(taskCreationDTO.getName(), actions);

        SimpleIdResponseDto responseTaskId = new SimpleIdResponseDto();
        responseTaskId.setId(String.valueOf(id));

        return new ResponseEntity<>(responseTaskId, HttpStatus.OK);
    }

    private List<Action> createActions(@RequestBody TaskCreationDTO taskCreationDTO) {
        List<Action> actions = new ArrayList<>();
        for (ActionDTO Action : taskCreationDTO.getActions()) {
            // Determine what actions the user wants to add
            try {
                Class<?> cls = Class.forName("com.dopplertask.doppler.domain.action." + Action.getActionType());
                Action clsInstance = (Action) cls.getDeclaredConstructor().newInstance();


                for (Map.Entry<String, String> entry : Action.getFields().entrySet()) {

                    try {
                        Field field = cls.getDeclaredField(entry.getKey());
                        field.setAccessible(true);
                        try {
                            // Try to set it as a long
                            if (field.getType().getName().contains("Integer")) {
                                field.set(clsInstance, Integer.parseInt(entry.getValue()));
                            } else {
                                field.set(clsInstance, Long.parseLong(entry.getValue()));
                            }
                        } catch (NumberFormatException e) {
                            field.set(clsInstance, entry.getValue());
                        }
                        logger.debug("Variable set in action type [key={}]", entry.getKey());
                    } catch (NoSuchFieldException | IllegalAccessException e) {
                        logger.warn("Could not set variable for action [key={}, action={}]", entry.getKey(), Action.getActionType());
                    }

                }

                actions.add(clsInstance);

            } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
                logger.warn("Action type {} could not be found", Action.getActionType());
                throw new RuntimeException("Action type " + Action.getActionType() + " could not be found");
            }
        }
        return actions;
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
