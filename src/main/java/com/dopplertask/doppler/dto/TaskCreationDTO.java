package com.dopplertask.doppler.dto;

import com.dopplertask.doppler.domain.action.Action;

import java.util.List;

public class TaskCreationDTO {
    private String name;
    private List<Action> actions;

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
}
