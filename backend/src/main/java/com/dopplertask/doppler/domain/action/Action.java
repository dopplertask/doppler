package com.dopplertask.doppler.domain.action;

import com.dopplertask.doppler.domain.ActionPort;
import com.dopplertask.doppler.domain.ActionPortType;
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
import com.dopplertask.doppler.domain.action.common.SwitchAction;
import com.dopplertask.doppler.domain.action.common.TimedWait;
import com.dopplertask.doppler.domain.action.connection.HttpAction;
import com.dopplertask.doppler.domain.action.connection.MySQLAction;
import com.dopplertask.doppler.domain.action.connection.SSHAction;
import com.dopplertask.doppler.domain.action.connection.SecureCopyAction;
import com.dopplertask.doppler.domain.action.io.ReadFileAction;
import com.dopplertask.doppler.domain.action.io.WriteFileAction;
import com.dopplertask.doppler.domain.action.ui.BrowseWebAction;
import com.dopplertask.doppler.domain.action.ui.MouseAction;
import com.dopplertask.doppler.service.TaskService;
import com.dopplertask.doppler.service.VariableExtractorUtil;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import javax.persistence.CascadeType;
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
import javax.persistence.OneToMany;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

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
        @JsonSubTypes.Type(value = MouseAction.class, name = "MouseAction"),
        @JsonSubTypes.Type(value = StartAction.class, name = "StartAction"),
        @JsonSubTypes.Type(value = WriteFileAction.class, name = "WriteFileAction"),
        @JsonSubTypes.Type(value = SwitchAction.class, name = "SwitchAction")
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

    private Integer guiXPos;
    private Integer guiYPos;

    /**
     * All action values are evaluated with VELOCITY as standard, but can be changed to other languages.
     */
    @Enumerated(EnumType.STRING)
    @Column
    private ScriptLanguage scriptLanguage = ScriptLanguage.VELOCITY;

    @OneToMany(mappedBy = "action", cascade = CascadeType.ALL)
    @Fetch(value = FetchMode.JOIN)
    private List<ActionPort> ports = new ArrayList<>();

    @JsonIgnore
    public List<ActionPort> getOutputPorts() {
        if (ports != null) {
            return ports.stream().filter(actionPort -> actionPort.getPortType() == ActionPortType.OUTPUT).collect(Collectors.toList());
        }
        return Collections.emptyList();
    }

    @JsonIgnore
    public List<ActionPort> getInputPorts() {
        if (ports != null) {
            return ports.stream().filter(actionPort -> actionPort.getPortType() == ActionPortType.INPUT).collect(Collectors.toList());
        }
        return Collections.emptyList();
    }

    public Action() {
    }


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

    public ScriptLanguage getScriptLanguage() {
        return scriptLanguage;
    }

    public void setScriptLanguage(ScriptLanguage scriptLanguage) {
        this.scriptLanguage = scriptLanguage;
    }

    @JsonIgnore
    public List<PropertyInformation> getActionInfo() {
        return new ArrayList<>(List.of(
                new PropertyInformation("continueOnFailure", "Continue on failure", PropertyInformation.PropertyInformationType.BOOLEAN, "false", "true or false. Lets the action continue on failure, ignoring any retry."),
                new PropertyInformation("scriptLanguage", "Script Language", PropertyInformation.PropertyInformationType.DROPDOWN, "VELOCITY", "VELOCITY (default), JAVASCRIPT.",
                        List.of(new PropertyInformation("VELOCITY", "Velocity"), new PropertyInformation("JAVASCRIPT", "Javascript"))
                ),
                new PropertyInformation("retries", "Retries", PropertyInformation.PropertyInformationType.NUMBER, "0", "Amount of retries."),
                new PropertyInformation("failOn", "Fail on", PropertyInformation.PropertyInformationType.STRING, "", "The current action will fail if this evaluates to anything."))
        );
    }

    public List<ActionPort> getPorts() {
        return ports;
    }

    public void setPorts(List<ActionPort> ports) {
        this.ports = ports;
    }

    public Integer getGuiXPos() {
        return guiXPos;
    }

    public void setGuiXPos(Integer guiXPos) {
        this.guiXPos = guiXPos;
    }

    public Integer getGuiYPos() {
        return guiYPos;
    }

    public void setGuiYPos(Integer guiYPos) {
        this.guiYPos = guiYPos;
    }

    public static class PropertyInformation {
        private String name;
        private String displayName;
        private PropertyInformationType type;
        private String defaultValue;
        private String description;
        private List<PropertyInformation> options;

        /**
         * Initialize a property info with name and displayName. Type is set to a String.
         *
         * @param name
         * @param displayName
         */
        public PropertyInformation(String name, String displayName) {
            this(name, displayName, PropertyInformationType.STRING, "", "");
        }

        public PropertyInformation(String name, String displayName, PropertyInformationType type) {
            this(name, displayName, type, "", "");
        }

        public PropertyInformation(String name, String displayName, PropertyInformationType type, String defaultValue, String description) {
            this(name, displayName, type, defaultValue, description, List.of());
        }

        public PropertyInformation(String name, String displayName, PropertyInformationType type, String defaultValue, String description, List<PropertyInformation> options) {
            this.name = name;
            this.displayName = displayName;
            this.type = type;
            this.defaultValue = defaultValue;
            this.description = description;
            this.options = options;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getDisplayName() {
            return displayName;
        }

        public void setDisplayName(String displayName) {
            this.displayName = displayName;
        }

        public PropertyInformationType getType() {
            return type;
        }

        public void setType(PropertyInformationType type) {
            this.type = type;
        }

        public String getDefaultValue() {
            return defaultValue;
        }

        public void setDefaultValue(String defaultValue) {
            this.defaultValue = defaultValue;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public List<PropertyInformation> getOptions() {
            return options;
        }

        public void setOptions(List<PropertyInformation> options) {
            this.options = options;
        }

        public enum PropertyInformationType {
            STRING, MULTILINE, BOOLEAN, NUMBER, DROPDOWN, MAP
        }
    }
}
