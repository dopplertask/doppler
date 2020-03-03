package com.dopplertask.doppler.domain.action;

import com.dopplertask.doppler.domain.ActionResult;
import com.dopplertask.doppler.domain.Task;
import com.dopplertask.doppler.domain.TaskExecution;
import com.dopplertask.doppler.domain.action.common.ExecuteCommandAction;
import com.dopplertask.doppler.domain.action.common.IfAction;
import com.dopplertask.doppler.domain.action.common.LinkedTaskAction;
import com.dopplertask.doppler.domain.action.common.PrintAction;
import com.dopplertask.doppler.domain.action.common.ScriptAction;
import com.dopplertask.doppler.domain.action.common.ScriptLanguage;
import com.dopplertask.doppler.domain.action.common.SetVariableAction;
import com.dopplertask.doppler.domain.action.common.TimedWait;
import com.dopplertask.doppler.domain.action.connection.HttpAction;
import com.dopplertask.doppler.domain.action.connection.MySQLAction;
import com.dopplertask.doppler.domain.action.connection.SSHAction;
import com.dopplertask.doppler.domain.action.connection.SecureCopyAction;
import com.dopplertask.doppler.domain.action.io.ReadFileAction;
import com.dopplertask.doppler.domain.action.ui.BrowseWebAction;
import com.dopplertask.doppler.domain.action.ui.MouseAction;
import com.dopplertask.doppler.service.TaskService;
import com.dopplertask.doppler.service.VariableExtractorUtil;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import javax.persistence.Column;
import javax.persistence.DiscriminatorColumn;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import java.io.IOException;

import static com.fasterxml.jackson.annotation.JsonTypeInfo.As.PROPERTY;
import static com.fasterxml.jackson.annotation.JsonTypeInfo.Id.NAME;

@Entity
@Inheritance(strategy = InheritanceType.JOINED)
@DiscriminatorColumn(name = "action_type", length = 255)
@DiscriminatorValue("noop")
@JsonTypeInfo(use = NAME, include = PROPERTY)
@JsonSubTypes({
        @JsonSubTypes.Type(value = BrowseWebAction.class, name = "BrowseWebAction"),
        @JsonSubTypes.Type(value = HttpAction.class, name = "HttpAction"),
        @JsonSubTypes.Type(value = LinkedTaskAction.class, name = "LinkedTaskAction"),
        @JsonSubTypes.Type(value = MySQLAction.class, name = "MySQLAction"),
        @JsonSubTypes.Type(value = PrintAction.class, name = "PrintAction"),
        @JsonSubTypes.Type(value = ReadFileAction.class, name = "ReadFileAction"),
        @JsonSubTypes.Type(value = SecureCopyAction.class, name = "SecureCopyAction"),
        @JsonSubTypes.Type(value = SSHAction.class, name = "SSHAction"),
        @JsonSubTypes.Type(value = TimedWait.class, name = "TimedWait"),
        @JsonSubTypes.Type(value = ExecuteCommandAction.class, name = "ExecuteCommandAction"),
        @JsonSubTypes.Type(value = SetVariableAction.class, name = "SetVariableAction"),
        @JsonSubTypes.Type(value = ScriptAction.class, name = "ScriptAction"),
        @JsonSubTypes.Type(value = IfAction.class, name = "IfAction"),
        @JsonSubTypes.Type(value = MouseAction.class, name = "MouseAction")
})
@JsonIgnoreProperties(ignoreUnknown = true)
public class Action {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", updatable = false, nullable = false)
    @JsonIgnore
    private Long id;

    @ManyToOne
    @JoinColumn
    @JsonIgnore
    private Task task;

    @Column
    @JsonIgnore
    private Integer orderPosition;

    @Column(nullable = false, columnDefinition = "TINYINT(1)")
    private boolean continueOnFailure = false;

    @Column(length = 4096)
    private String failOn;

    private Integer retries = 0;

    /**
     * All action values are evaluated with VELOCITY as standard, but can be changed to other languages.
     */
    @Enumerated(EnumType.STRING)
    @Column
    private ScriptLanguage scriptLanguage = ScriptLanguage.VELOCITY;

    /**
     * This path describes the path for this particular action. This is used in the executionimpl to choose the correct actions.
     */
    @Column
    private String path;


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Task getTask() {
        return task;
    }

    public void setTask(Task task) {
        this.task = task;
    }

    /**
     * Executes an action.
     *
     * @param taskService           which handles task execution.
     * @param execution             of the current task.
     * @param variableExtractorUtil utility to evaluate velocity code.
     * @return an action result which represents the outcome of the executed action.
     */
    public ActionResult run(TaskService taskService, TaskExecution execution, VariableExtractorUtil variableExtractorUtil) throws IOException {
        return new ActionResult();
    }

    public Integer getOrderPosition() {
        return orderPosition;
    }

    public void setOrderPosition(Integer orderPosition) {
        this.orderPosition = orderPosition;
    }

    public boolean isContinueOnFailure() {
        return continueOnFailure;
    }

    public void setContinueOnFailure(boolean continueOnFailure) {
        this.continueOnFailure = continueOnFailure;
    }

    public Integer getRetries() {
        return retries;
    }

    public void setRetries(Integer retries) {
        this.retries = retries;
    }

    public String getFailOn() {
        return failOn;
    }

    public void setFailOn(String failOn) {
        this.failOn = failOn;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public ScriptLanguage getScriptLanguage() {
        return scriptLanguage;
    }

    public void setScriptLanguage(ScriptLanguage scriptLanguage) {
        this.scriptLanguage = scriptLanguage;
    }
}
