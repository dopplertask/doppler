package com.dopplertask.doppler.domain.action;

import com.dopplertask.doppler.domain.ActionResult;
import com.dopplertask.doppler.domain.StatusCode;
import com.dopplertask.doppler.domain.TaskExecution;
import com.dopplertask.doppler.service.TaskRequest;
import com.dopplertask.doppler.service.TaskService;

import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name = "LinkedTaskAction")
@DiscriminatorValue("linkedtask_action")
public class LinkedTaskAction extends Action {

    @Column
    private Long linkedTaskId;

    @Override
    public ActionResult run(TaskService taskService, TaskExecution execution) {
        TaskRequest taskRequest = new TaskRequest();
        taskRequest.setAutomationId(linkedTaskId);
        taskRequest.setParameters(execution.getParameters());
        TaskExecution taskExecution = taskService.runRequest(taskRequest);
        if (taskExecution != null) {
            if (taskExecution.isSuccess()) {
                ActionResult actionResult = new ActionResult();
                actionResult.setStatusCode(StatusCode.SUCCESS);
                actionResult.setOutput("Successfully executed linked task [id=" + linkedTaskId + "]");
                return actionResult;
            } else {
                ActionResult actionResult = new ActionResult();
                actionResult.setStatusCode(StatusCode.FAILURE);
                actionResult.setOutput("Linked task execution failed [id=" + linkedTaskId + "]");
                return actionResult;
            }
        }

        ActionResult actionResult = new ActionResult();
        actionResult.setStatusCode(StatusCode.FAILURE);
        actionResult.setOutput("Could not find the requested linked task [id=" + linkedTaskId + "]");
        return actionResult;

    }


    public void setLinkedTaskId(Long linkedTaskId) {
        this.linkedTaskId = linkedTaskId;
    }
}
