package se.feraswilson.taskservice.dto;

import java.util.Map;

public class ActionDTO {

    private Map<String, String> fields;
    private String actionType;

    public Map<String, String> getFields() {
        return fields;
    }

    public void setFields(Map<String, String> fields) {
        this.fields = fields;
    }

    public String getActionType() {
        return actionType;
    }

    public void setActionType(String actionType) {
        this.actionType = actionType;
    }
}
