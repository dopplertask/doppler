package com.dopplertask.doppler.domain.action.common;

import com.dopplertask.doppler.domain.ActionResult;
import com.dopplertask.doppler.domain.StatusCode;
import com.dopplertask.doppler.domain.TaskExecution;
import com.dopplertask.doppler.domain.TaskExecutionLog;
import com.dopplertask.doppler.domain.action.Action;
import com.dopplertask.doppler.service.BroadcastListener;
import com.dopplertask.doppler.service.TaskRequest;
import com.dopplertask.doppler.service.TaskService;
import com.dopplertask.doppler.service.VariableExtractorUtil;
import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.List;

import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name = "LinkedTaskAction")
@DiscriminatorValue("linkedtask_action")
public class LinkedTaskAction extends Action {

    private static final Integer MAX_LINKED_TASK_DEPTH = 100;
    @Column
    private String name;

    @Column
    @JsonIgnore
    private String checksum;

    @Override
    public ActionResult run(TaskService taskService, TaskExecution execution, VariableExtractorUtil variableExtractorUtil, BroadcastListener broadcastListener) {
        if (execution.getDepth() < MAX_LINKED_TASK_DEPTH) {
            TaskRequest taskRequest = new TaskRequest();

            taskRequest.setTaskName(name);
            taskRequest.setChecksum(checksum);

            // Increase depth by one
            taskRequest.setDepth(execution.getDepth() + 1);

            taskRequest.setParameters(execution.getParameters());
            TaskExecution taskExecution = taskService.runRequest(taskRequest);
            if (taskExecution != null) {
                StringBuilder standardOutput = new StringBuilder();
                if (taskExecution.isSuccess()) {
                    ActionResult actionResult = new ActionResult();
                    actionResult.setStatusCode(StatusCode.SUCCESS);


                    standardOutput = getExecutionLogsAsString(taskExecution);
                    standardOutput.append("Successfully executed linked task [name=" + name + "]");

                    actionResult.setOutput(standardOutput.toString());

                    return actionResult;
                } else {
                    ActionResult actionResult = new ActionResult();
                    actionResult.setStatusCode(StatusCode.FAILURE);


                    standardOutput.append("Linked task execution failed [name=" + name + "]");

                    standardOutput = getExecutionLogsAsString(taskExecution);

                    actionResult.setErrorMsg(standardOutput.toString());
                    return actionResult;
                }
            }
        }

        ActionResult actionResult = new ActionResult();
        actionResult.setStatusCode(StatusCode.FAILURE);
        actionResult.setOutput("Could not find the requested linked task [name=" + name + "]");
        return actionResult;

    }

    /**
     * Loop over all logs and compress into a string with new lines.
     *
     * @param taskExecution to extract logs from.
     * @return a string builder containing all log messages.
     */
    private StringBuilder getExecutionLogsAsString(TaskExecution taskExecution) {
        StringBuilder standardOutput = new StringBuilder();
        int i = 0;
        for (TaskExecutionLog log : taskExecution.getLogs()) {
            if (i == 0) {
                standardOutput.append("\n");
            }
            standardOutput.append("[" + name + "] " + log.getOutput() + "\n");
            i++;
        }
        return standardOutput;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getChecksum() {
        return checksum;
    }

    public void setChecksum(String checksum) {
        this.checksum = checksum;
    }

    @Override
    public List<PropertyInformation> getActionInfo() {
        List<PropertyInformation> actionInfo = super.actionInfo;

        actionInfo.add(new PropertyInformation("name", "Task name"));
        return actionInfo;
    }

    @Override
    public String getDescription() {
        return "Execute another task";
    }
}
