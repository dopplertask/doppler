package com.dopplertask.doppler.service;

import java.util.Map;

public class TaskRequest {
    private Long automationId;
    private Map<String, String> parameters;

    public TaskRequest(Long automationId, Map<String, String> parameters) {
        this.automationId = automationId;
        this.parameters = parameters;
    }

    public TaskRequest() {
    }

    public Long getAutomationId() {
        return automationId;
    }

    public void setAutomationId(Long automationId) {
        this.automationId = automationId;
    }

    public Map<String, String> getParameters() {
        return parameters;
    }

    public void setParameters(Map<String, String> parameters) {
        this.parameters = parameters;
    }

    @Override
    public String toString() {
        return "TaskRequest{" +
                "automationId=" + automationId +
                ", parameters=" + parameters +
                '}';
    }
}
