package se.feraswilson.taskservice.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import se.feraswilson.taskservice.domain.Task;
import se.feraswilson.taskservice.domain.TaskExecution;
import se.feraswilson.taskservice.domain.TaskExecutionLog;
import se.feraswilson.taskservice.domain.action.Action;
import se.feraswilson.taskservice.dto.ActionDTO;
import se.feraswilson.taskservice.dto.TaskCreationDTO;
import se.feraswilson.taskservice.dto.TaskExecutionLogResponseDTO;
import se.feraswilson.taskservice.dto.TaskRequestDTO;
import se.feraswilson.taskservice.dto.TaskResponseDTO;
import se.feraswilson.taskservice.service.TaskRequest;
import se.feraswilson.taskservice.service.TaskService;

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
    public String scheduleTask(@RequestBody TaskRequestDTO taskRequestDTO) {
        TaskRequest request = new TaskRequest(taskRequestDTO.getAutomationId(), taskRequestDTO.getParameters());
        taskService.delegate(request);

        return "{\"success\": 1}";
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
        for (ActionDTO action : taskCreationDTO.getActions()) {
            // Determine what actions the user wants to add
            try {
                Class<?> cls = Class.forName("se.feraswilson.taskservice.domain.action." + action.getActionType());
                Action clsInstance = (Action) cls.getDeclaredConstructor().newInstance();


                for (Map.Entry<String, String> entry : action.getFields().entrySet()) {

                    try {
                        Field field = cls.getDeclaredField(entry.getKey());
                        field.setAccessible(true);
                        try {
                            // Try to set it as a long
                            field.set(clsInstance, Long.parseLong(entry.getValue()));
                        } catch (NumberFormatException e) {
                            field.set(clsInstance, entry.getValue());
                        }
                        logger.debug("Variable set in action type [key={}]", entry.getKey());
                    } catch (NoSuchFieldException | IllegalAccessException e) {
                        logger.warn("Could not set variable for action [key={}, action={}]", entry.getKey(), action.getActionType());
                    }

                }

                actions.add(clsInstance);

            } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
                logger.warn("Action type {} could not be found", action.getActionType());
                throw new RuntimeException("Action type " + action.getActionType() + " could not be found");
            }
        }
        return actions;
    }


    @RequestMapping(path = "/task", method = RequestMethod.GET)
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

}
