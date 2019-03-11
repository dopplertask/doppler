package se.feraswilson.automationservice.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import se.feraswilson.automationservice.service.AutomationRequest;
import se.feraswilson.automationservice.service.AutomationService;

import java.util.HashMap;
import java.util.Map;

@RestController
public class TaskController {

    @Autowired
    private AutomationService automationService;

    @RequestMapping("/task")
    public String createTask(@RequestParam(value = "name", defaultValue = "World") String name) {

        Map<String, String> parameters = new HashMap<>();
        parameters.put("testmsg", "Hello my friend");
        AutomationRequest request = new AutomationRequest(2L, parameters);
        automationService.delegate(request);


        return "{\"success\": 1}";
    }
}
