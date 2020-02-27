package com.dopplertask.doppler.domain;


import com.dopplertask.doppler.domain.action.Action;
import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

@Entity
@Table(name = "Task")
public class Task {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column
    private String name;

    @Column
    private String description;

    @OneToMany(mappedBy = "task", cascade = CascadeType.ALL)
    private List<TaskParameter> taskParameterList = new ArrayList<>();

    @OneToMany(mappedBy = "task", cascade = CascadeType.ALL)
    @OrderBy("orderPosition ASC")
    private List<Action> actionList = new ArrayList<>();

    @OneToMany(mappedBy = "task", cascade = CascadeType.ALL)
    @JsonIgnore
    private List<TaskExecution> executions = new ArrayList<>();

    @Basic
    @Temporal(TemporalType.TIMESTAMP)
    private Date created;

    @Column(unique = true)
    @JsonIgnore
    private String checksum;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public List<Action> getActionList() {
        return actionList;
    }

    public void setActionList(List<Action> actionList) {
        this.actionList = actionList;
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

    public String getChecksum() {
        return checksum;
    }

    public void setChecksum(String checksum) {
        this.checksum = checksum;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<TaskExecution> getExecutions() {
        return executions;
    }

    public void setExecutions(List<TaskExecution> executions) {
        this.executions = executions;
    }

    public List<TaskParameter> getTaskParameterList() {
        return taskParameterList;
    }

    public void setTaskParameterList(List<TaskParameter> taskParameterList) {
        this.taskParameterList = taskParameterList;
    }
}
