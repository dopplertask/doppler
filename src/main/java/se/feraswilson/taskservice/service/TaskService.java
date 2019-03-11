package se.feraswilson.taskservice.service;

public interface TaskService {

    void delegate(TaskRequest request);

    void handleAutomationRequest(TaskRequest taskRequest);
}
