package com.dopplertask.doppler.dto;

import java.util.List;

public class TaskCreationDTO {
    private String name;
    private List<ActionDTO> actions;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<ActionDTO> getActions() {
        return actions;
    }

    public void setActions(List<ActionDTO> actions) {
        this.actions = actions;
    }
}
