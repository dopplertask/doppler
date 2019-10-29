package com.dopplertask.doppler.dto;

import java.util.Map;

public class TaskRequestDTO {

    private String taskName;
    private Map<String, String> parameters;

    public String getTaskName() {
        return taskName;
    }

    public void setTaskName(String taskName) {
        this.taskName = taskName;
    }

    public Map<String, String> getParameters() {
        return parameters;
    }

    public void setParameters(Map<String, String> parameters) {
        this.parameters = parameters;
    }
}
