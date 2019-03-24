package se.feraswilson.taskservice.domain.action;

import se.feraswilson.taskservice.domain.ActionResult;
import se.feraswilson.taskservice.domain.StatusCode;
import se.feraswilson.taskservice.domain.TaskExecution;
import se.feraswilson.taskservice.service.VariableExtractorUtil;

import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.Table;

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
    public ActionResult run(TaskExecution execution) {
        String messageVariable = VariableExtractorUtil.extract(message, execution);

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

    public void setMessage(String message) {
        this.message = message;
    }
}

