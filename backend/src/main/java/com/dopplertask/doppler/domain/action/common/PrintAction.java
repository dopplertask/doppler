package com.dopplertask.doppler.domain.action.common;

import com.dopplertask.doppler.domain.ActionResult;
import com.dopplertask.doppler.domain.StatusCode;
import com.dopplertask.doppler.domain.TaskExecution;
import com.dopplertask.doppler.domain.action.Action;
import com.dopplertask.doppler.service.TaskService;
import com.dopplertask.doppler.service.VariableExtractorUtil;

import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.Table;
import java.io.IOException;

@Entity
@Table(name = "PrintAction")
@DiscriminatorValue("print_action")
public class PrintAction extends Action {

    @Column
    private String message;

    public PrintAction() {
    }

    public PrintAction(String message) {
        this.message = message;
    }


    @Override
    public ActionResult run(TaskService taskService, TaskExecution execution, VariableExtractorUtil variableExtractorUtil) throws IOException {
        String messageVariable = variableExtractorUtil.extract(message, execution, getScriptLanguage());

        ActionResult actionResult = new ActionResult();

        if (messageVariable != null && !messageVariable.isEmpty()) {
            actionResult.setOutput(messageVariable);
            actionResult.setStatusCode(StatusCode.SUCCESS);
        } else {
            actionResult.setErrorMsg("No output");
            actionResult.setStatusCode(StatusCode.FAILURE);
        }
        return actionResult;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}

