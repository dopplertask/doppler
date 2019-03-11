package se.feraswilson.taskservice.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import se.feraswilson.taskservice.service.TaskRequest;
import se.feraswilson.taskservice.service.TaskService;

import java.util.HashMap;
import java.util.Map;

@RestController
public class TaskController {

    @Autowired
    private TaskService taskService;

    @RequestMapping("/task")
    public String createTask(@RequestParam(value = "name", defaultValue = "World") String name) {

        Map<String, String> parameters = new HashMap<>();
        parameters.put("testmsg", "Hello my friend");
        TaskRequest request = new TaskRequest(2L, parameters);
        taskService.delegate(request);


        return "{\"success\": 1}";
    }
}
