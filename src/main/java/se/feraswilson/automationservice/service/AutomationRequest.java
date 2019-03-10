package se.feraswilson.automationservice.service;

import java.util.Map;

public class AutomationRequest {
    private Long automationId;
    private Map<String, String> parameters;

    public AutomationRequest(Long automationId, Map<String, String> parameters) {
        this.automationId = automationId;
        this.parameters = parameters;
    }

    public AutomationRequest() {
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
        return "AutomationRequest{" +
                "automationId=" + automationId +
                ", parameters=" + parameters +
                '}';
    }
}
