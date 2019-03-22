package se.feraswilson.taskservice.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import se.feraswilson.taskservice.domain.Task;
import se.feraswilson.taskservice.dto.TaskRequestDTO;
import se.feraswilson.taskservice.dto.TaskResponseDTO;
import se.feraswilson.taskservice.service.TaskRequest;
import se.feraswilson.taskservice.service.TaskService;

import java.util.ArrayList;
import java.util.List;

@RestController
public class TaskController {

    @Autowired
    private TaskService taskService;

    @RequestMapping(path = "/task", method = RequestMethod.POST)
    public String scheduleTask(@RequestBody TaskRequestDTO taskRequestDTO) {
        TaskRequest request = new TaskRequest(taskRequestDTO.getAutomationId(), taskRequestDTO.getParameters());
        taskService.delegate(request);

        return "{\"success\": 1}";
    }


/*
    @RequestMapping(path = "/task", method = RequestMethod.POST)
    public String createTask(@RequestBody TaskRequestDTO taskRequestDTO) {
        TaskRequest request = new TaskRequest(taskRequestDTO.getAutomationId(), taskRequestDTO.getParameters());
        taskService.delegate(request);

        return "{\"success\": 1}";
    }

*/

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
