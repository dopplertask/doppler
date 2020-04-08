package com.dopplertask.doppler.domain.action;

import com.dopplertask.doppler.domain.ActionResult;
import com.dopplertask.doppler.domain.OutputType;
import com.dopplertask.doppler.domain.StatusCode;
import com.dopplertask.doppler.domain.TaskExecution;
import com.dopplertask.doppler.service.TaskService;
import com.dopplertask.doppler.service.VariableExtractorUtil;

import java.io.IOException;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.Table;

/**
 * This action defines the start of a task.
 */
@Entity
@Table(name = "StartAction")
@DiscriminatorValue("start_action")
public class StartAction extends Action {

    public StartAction() {
    }

    @Override
    public ActionResult run(TaskService taskService, TaskExecution execution, VariableExtractorUtil variableExtractorUtil) throws IOException {
        ActionResult result = new ActionResult();
        result.setOutput("--- Task execution started ---");
        result.setStatusCode(StatusCode.SUCCESS);
        result.setOutputType(OutputType.STRING);
        return result;
    }

    @Override
    public String getDescription() {
        return "This is the start of every task.";
    }
}
