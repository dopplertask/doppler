package com.dopplertask.doppler.dto;

import com.dopplertask.doppler.domain.Connection;
import com.dopplertask.doppler.domain.TaskParameter;
import com.dopplertask.doppler.domain.action.Action;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class TaskCreationDTO {
    private String name;
    private List<TaskParameter> parameters;
    private List<Action> actions;
    private String description;
    private List<Connection> connections;

    @JsonCreator
    public TaskCreationDTO(@JsonProperty(value = "name", required = true) String name, @JsonProperty(value = "parameters") List<TaskParameter> parameters, @JsonProperty(value = "actions", required = true) List<Action> actions, @JsonProperty(value = "description", required = true) String description, @JsonProperty(value = "connections", required = true) List<Connection> connections) {
        this.name = name;
        this.parameters = parameters;
        this.actions = actions;
        this.description = description;
        this.connections = connections;
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

    public List<TaskParameter> getParameters() {
        return parameters;
    }

    public void setParameters(List<TaskParameter> parameters) {
        this.parameters = parameters;
    }

    public List<Connection> getConnections() {
        return connections;
    }

    public void setConnections(List<Connection> connections) {
        this.connections = connections;
    }
}
