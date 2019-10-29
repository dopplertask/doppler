package com.dopplertask.doppler.service;

import java.util.Map;

public class TaskRequest {
    private String taskName;
    private String checksum;
    private Map<String, String> parameters;
    private int depth = 0;

    public TaskRequest(String taskName, Map<String, String> parameters) {
        this.taskName = taskName;
        this.parameters = parameters;
    }

    public TaskRequest() {
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

    public void setDepth(int depth) {
        this.depth = depth;
    }

    public int getDepth() {
        return depth;
    }
}
