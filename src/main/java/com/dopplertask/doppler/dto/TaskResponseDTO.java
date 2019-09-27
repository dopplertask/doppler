package com.dopplertask.doppler.dto;

import com.dopplertask.doppler.domain.action.Action;

import java.util.Date;
import java.util.List;

public class TaskResponseDTO {

    private Long id;
    private String name;
    private Date created;
    private List<Action> actions;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Date getCreated() {
        return created;
    }

    public void setCreated(Date created) {
        this.created = created;
    }

    public List<Action> getActions() {
        return actions;
    }

    public void setActions(List<Action> actions) {
        this.actions = actions;
    }
}
