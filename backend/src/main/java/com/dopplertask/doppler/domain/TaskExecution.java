package com.dopplertask.doppler.domain;

import com.dopplertask.doppler.domain.action.Action;

import javax.persistence.CascadeType;
import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.MapKeyColumn;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Transient;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Entity
@Table(name = "TaskExecution")
public class TaskExecution {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ElementCollection
    @MapKeyColumn(name = "paramName")
    @Column(name = "paramValue")
    @CollectionTable(name = "execution_parameters", joinColumns = @JoinColumn(name = "execution_id"))
    private Map<String, String> parameters = new HashMap<String, String>();

    @ManyToOne
    @JoinColumn
    private Task task;

    @OneToMany(mappedBy = "taskExecution", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<TaskExecutionLog> logs = new ArrayList<>();

    private Date startdate;
    private Date enddate;

    // Useful for linked tasks to know how deep in the linking it has reached to avoid stack overflow.
    private Integer depth = 0;

    @Enumerated(EnumType.STRING)
    private TaskExecutionStatus status = TaskExecutionStatus.CREATED;

    @Transient
    private Action currentAction;

    private boolean success = true;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Map<String, String> getParameters() {
        return parameters;
    }

    public void setParameters(Map<String, String> parameters) {
        this.parameters = parameters;
    }

    public Task getTask() {
        return task;
    }

    public void setTask(Task task) {
        this.task = task;
    }

    public Date getStartdate() {
        return startdate;
    }

    public void setStartdate(Date startdate) {
        this.startdate = startdate;
    }

    public Date getEnddate() {
        return enddate;
    }

    public void setEnddate(Date enddate) {
        this.enddate = enddate;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public void addLog(TaskExecutionLog executionLog) {
        logs.add(executionLog);
    }

    public List<TaskExecutionLog> getLogs() {
        return logs;
    }

    public TaskExecutionStatus getStatus() {
        return status;
    }

    public void setStatus(TaskExecutionStatus status) {
        this.status = status;
    }

    public Integer getDepth() {
        return depth;
    }

    public void setDepth(Integer depth) {
        this.depth = depth;
    }

    public Action getCurrentAction() {
        return currentAction;
    }

    public void setCurrentAction(Action currentAction) {
        this.currentAction = currentAction;
    }
}
