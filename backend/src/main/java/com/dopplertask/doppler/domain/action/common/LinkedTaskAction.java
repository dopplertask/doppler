package com.dopplertask.doppler.domain.action.common;

import com.dopplertask.doppler.domain.ActionResult;
import com.dopplertask.doppler.domain.StatusCode;
import com.dopplertask.doppler.domain.TaskExecution;
import com.dopplertask.doppler.domain.action.Action;
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
    public ActionResult run(TaskService taskService, TaskExecution execution, VariableExtractorUtil variableExtractorUtil) {
        if (execution.getDepth() < MAX_LINKED_TASK_DEPTH) {
            TaskRequest taskRequest = new TaskRequest();

            taskRequest.setTaskName(name);
            taskRequest.setChecksum(checksum);

            // Increase depth by one
            taskRequest.setDepth(execution.getDepth() + 1);

            taskRequest.setParameters(execution.getParameters());
            TaskExecution taskExecution = taskService.runRequest(taskRequest);
            if (taskExecution != null) {
                if (taskExecution.isSuccess()) {
                    ActionResult actionResult = new ActionResult();
                    actionResult.setStatusCode(StatusCode.SUCCESS);
                    actionResult.setOutput("Successfully executed linked task [name=" + name + "]");
                    return actionResult;
                } else {
                    ActionResult actionResult = new ActionResult();
                    actionResult.setStatusCode(StatusCode.FAILURE);
                    actionResult.setOutput("Linked task execution failed [name=" + name + "]");
                    return actionResult;
                }
            }
        }

        ActionResult actionResult = new ActionResult();
        actionResult.setStatusCode(StatusCode.FAILURE);
        actionResult.setOutput("Could not find the requested linked task [name=" + name + "]");
        return actionResult;

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
        List<PropertyInformation> actionInfo = super.getActionInfo();

        actionInfo.add(new PropertyInformation("name", "Task name"));
        return actionInfo;
    }

    @Override
    public String getDescription() {
        return "Execute another task";
    }
}
