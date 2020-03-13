package com.dopplertask.doppler.domain.action.common;

import com.dopplertask.doppler.domain.ActionResult;
import com.dopplertask.doppler.domain.TaskExecution;
import com.dopplertask.doppler.domain.action.Action;
import com.dopplertask.doppler.service.TaskService;
import com.dopplertask.doppler.service.VariableExtractorUtil;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.Table;
import java.io.IOException;
import java.util.List;

@Entity
@Table(name = "IfAction")
@DiscriminatorValue("if_action")
public class IfAction extends Action {

    private String condition;


    public IfAction() {
        // Init
    }

    @Override
    public ActionResult run(TaskService taskService, TaskExecution execution, VariableExtractorUtil variableExtractorUtil) throws IOException {

        ActionResult actionResult = new ActionResult();
        String localCondition;
        switch (getScriptLanguage()) {
            case VELOCITY:
                localCondition = variableExtractorUtil.extract("#if(" + condition + ")\ntrue#else\nfalse#end", execution, getScriptLanguage());
                break;
            case JAVASCRIPT:
                localCondition = variableExtractorUtil.extract("if(" + condition + ") {\n\"true\"; } else {\n\"false\";}", execution, ScriptLanguage.JAVASCRIPT);
                break;
            default:
                throw new IllegalStateException("Unexpected script engine");
        }

        if ("true".equals(localCondition)) {
            actionResult.setOutput("If evaluated to true. Next actions path: " + localCondition);
            execution.setCurrentAction(getOutputPorts().get(0).getConnectionSource().getTarget().getAction());
        } else {
            actionResult.setOutput("If evaluated to false. Next actions path: " + localCondition);
            execution.setCurrentAction(getOutputPorts().get(1).getConnectionSource().getTarget().getAction());
        }

        return actionResult;
    }


    public String getCondition() {
        return condition;
    }

    public void setCondition(String condition) {
        this.condition = condition;
    }

    @Override
    public List<PropertyInformation> getActionInfo() {
        List<PropertyInformation> actionInfo = super.getActionInfo();

        actionInfo.add(new PropertyInformation("condition", "Condition", PropertyInformation.PropertyInformationType.STRING, "", "Condition to evaluate."));
        return actionInfo;
    }

}
