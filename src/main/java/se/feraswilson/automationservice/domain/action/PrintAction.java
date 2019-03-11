package se.feraswilson.automationservice.domain.action;

import se.feraswilson.automationservice.domain.ActionResult;
import se.feraswilson.automationservice.domain.StatusCode;
import se.feraswilson.automationservice.domain.TaskExecution;
import se.feraswilson.automationservice.service.VariableExtractorUtil;

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
        String messageVar = message;
        String messageVariable = VariableExtractorUtil.extract(messageVar);
        if (!messageVariable.equals(message)) {
            messageVar = execution.getParameters().get(messageVariable);
        }

        ActionResult actionResult = new ActionResult();

        if (messageVar != null && !messageVar.isEmpty()) {
            actionResult.setOutput(messageVar);
            actionResult.setStatusCode(StatusCode.SUCCESS);
        } else {
            actionResult.setErrorMsg("No output");
            actionResult.setStatusCode(StatusCode.FAILURE);
        }
        return actionResult;
    }
}
