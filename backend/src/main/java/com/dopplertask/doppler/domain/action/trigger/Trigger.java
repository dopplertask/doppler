package com.dopplertask.doppler.domain.action.trigger;

import com.dopplertask.doppler.domain.action.Action;
import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.List;
import java.util.Map;

import javax.persistence.Column;
import javax.persistence.MappedSuperclass;
import javax.persistence.Transient;

@MappedSuperclass
public abstract class Trigger extends Action {

    @Column
    private String path = "";

    @Column
    private String triggerSuffix = "";

    @JsonIgnore
    @Transient
    private Map<String, String> parameters;

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }


    public Map<String, String> getParameters() {
        return parameters;
    }

    public void setParameters(Map<String, String> parameters) {
        this.parameters = parameters;
    }

    public String getTriggerSuffix() {
        return triggerSuffix;
    }

    public void setTriggerSuffix(String triggerSuffix) {
        this.triggerSuffix = triggerSuffix;
    }

    @Override
    public List<PropertyInformation> getActionInfo() {
        List<PropertyInformation> actionInfo = super.getActionInfo();
        actionInfo.add(new PropertyInformation("path", "Path", PropertyInformation.PropertyInformationType.STRING));
        return actionInfo;
    }
}
