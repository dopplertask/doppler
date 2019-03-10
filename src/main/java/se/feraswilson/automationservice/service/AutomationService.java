package se.feraswilson.automationservice.service;

public interface AutomationService {

    void delegate(AutomationRequest request);

    void handleAutomationRequest(AutomationRequest automationRequest);
}
