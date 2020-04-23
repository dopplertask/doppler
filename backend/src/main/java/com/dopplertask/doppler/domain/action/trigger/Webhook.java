package com.dopplertask.doppler.domain.action.trigger;

import com.dopplertask.doppler.domain.ActionResult;
import com.dopplertask.doppler.domain.OutputType;
import com.dopplertask.doppler.domain.StatusCode;
import com.dopplertask.doppler.domain.TaskExecution;
import com.dopplertask.doppler.service.BroadcastListener;
import com.dopplertask.doppler.service.TaskService;
import com.dopplertask.doppler.service.VariableExtractorUtil;

import java.io.IOException;

import javax.persistence.Entity;

@Entity
public class Webhook extends Trigger {

    public Webhook() {
    }

    @Override
    public ActionResult run(TaskService taskService, TaskExecution execution, VariableExtractorUtil variableExtractorUtil, BroadcastListener broadcastListener) throws IOException {
        ActionResult result = new ActionResult();
        result.setOutput("Webhook triggered");
        result.setStatusCode(StatusCode.SUCCESS);
        result.setOutputType(OutputType.STRING);
        return result;
    }

    @Override
    public String getDescription() {
        return "Starts the workflow when the webhook URL is called.";
    }
}
