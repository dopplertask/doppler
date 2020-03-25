package com.dopplertask.doppler.dto;

import com.dopplertask.doppler.domain.Connection;
import com.dopplertask.doppler.domain.TaskParameter;
import com.dopplertask.doppler.domain.action.Action;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.Date;
import java.util.List;

public class TaskResponseSingleDTO {

    private String checksum;
    private List<TaskParameter> taskParameters;
    private String name;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String description;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private List<Action> actions;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Date created;

    private List<Connection> connections;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Action> getActions() {
        return actions;
    }

    public void setActions(List<Action> actions) {
        this.actions = actions;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getChecksum() {
        return checksum;
    }

    public void setChecksum(String checksum) {
        this.checksum = checksum;
    }

    public Date getCreated() {
        return created;
    }

    public void setCreated(Date created) {
        this.created = created;
    }

    public List<TaskParameter> getTaskParameters() {
        return taskParameters;
    }

    public void setTaskParameters(List<TaskParameter> taskParameters) {
        this.taskParameters = taskParameters;
    }

    public List<Connection> getConnections() {
        return connections;
    }

    public void setConnections(List<Connection> connections) {
        this.connections = connections;
    }
}
