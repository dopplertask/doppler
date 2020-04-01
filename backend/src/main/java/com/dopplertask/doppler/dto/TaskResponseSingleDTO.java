package com.dopplertask.doppler.dto;

import com.dopplertask.doppler.domain.Connection;
import com.dopplertask.doppler.domain.TaskParameter;
import com.dopplertask.doppler.domain.action.Action;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class TaskResponseSingleDTO {

    private String name;
    private String checksum;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Date created;
    private List<TaskParameter> parameters;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String description;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private List<Action> actions;
    private List<Connection> connections;

    public TaskResponseSingleDTO() {
        parameters = new ArrayList<>();
        connections = new ArrayList<>();
    }

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

    public List<Connection> getConnections() {
        return connections;
    }

    public void setConnections(List<Connection> connections) {
        this.connections = connections;
    }

    public List<TaskParameter> getParameters() {
        return parameters;
    }

    public void setParameters(List<TaskParameter> parameters) {
        this.parameters = parameters;
    }
}
