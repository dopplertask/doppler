package com.dopplertask.doppler.service;

import java.util.Map;

public class TaskRequest {
    private String taskName;
    private String checksum;
    private Map<String, String> parameters;
    private int depth = 0;
    private boolean removeTaskAfterExecution;

    // If defined then automation starts from a trigger
    private String triggerName;
    private String triggerPath;

    public TaskRequest(String taskName, Map<String, String> parameters) {
        this(taskName, parameters, false);
    }

    public TaskRequest() {
        this.removeTaskAfterExecution = false;
    }

    public TaskRequest(String taskName, Map<String, String> parameters, boolean removeTaskAfterExecution) {
        this.taskName = taskName;
        this.parameters = parameters;
        this.removeTaskAfterExecution = removeTaskAfterExecution;
    }

    public String getTaskName() {
        return taskName;
    }

    public void setTaskName(String taskName) {
        this.taskName = taskName;
    }

    public Map<String, String> getParameters() {
        return parameters;
    }

    public void setParameters(Map<String, String> parameters) {
        this.parameters = parameters;
    }

    @Override
    public String toString() {
        return "TaskRequest{" +
                "taskName=" + taskName +
                ", parameters=" + parameters +
                '}';
    }

    public String getChecksum() {
        return checksum;
    }

    public void setChecksum(String checksum) {
        this.checksum = checksum;
    }

    public int getDepth() {
        return depth;
    }

    public void setDepth(int depth) {
        this.depth = depth;
    }

    public boolean isRemoveTaskAfterExecution() {
        return removeTaskAfterExecution;
    }

    public void setRemoveTaskAfterExecution(boolean removeTaskAfterExecution) {
        this.removeTaskAfterExecution = removeTaskAfterExecution;
    }

    public String getTriggerName() {
        return triggerName;
    }

    public void setTriggerName(String triggerName) {
        this.triggerName = triggerName;
    }

    public String getTriggerPath() {
        return triggerPath;
    }

    public void setTriggerPath(String triggerPath) {
        this.triggerPath = triggerPath;
    }
}
