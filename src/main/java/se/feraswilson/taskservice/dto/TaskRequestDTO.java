package se.feraswilson.taskservice.dto;

import java.util.Map;

public class TaskRequestDTO {

    private Long automationId;
    private Map<String, String> parameters;

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
}
