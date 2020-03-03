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

@Entity
@Table(name = "IfAction")
@DiscriminatorValue("if_action")
public class IfAction extends Action {

    private String condition;

    private String pathTrue;
    private String pathFalse;

    public IfAction() {
        // Init
    }

    @Override
    public ActionResult run(TaskService taskService, TaskExecution execution, VariableExtractorUtil variableExtractorUtil) throws IOException {

        ActionResult actionResult = new ActionResult();
        String localCondition;
        switch (getScriptLanguage()) {
            case VELOCITY:
                localCondition = variableExtractorUtil.extract("#if(" + condition + ")\n" + pathTrue + "#else\n" + pathFalse + "#end", execution, getScriptLanguage());
                break;
            case JAVASCRIPT:
                localCondition = variableExtractorUtil.extract("if(" + condition + ") {\n\"" + pathTrue + "\"; } else {\n\"" + pathFalse + "\";}", execution, ScriptLanguage.JAVASCRIPT);
                break;
            default:
                throw new IllegalStateException("Unexpected script engine");
        }

        if (pathTrue != null && pathTrue.equals(localCondition)) {
            actionResult.setOutput("If evaluated to true. Next actions path: " + localCondition);
            execution.setActivePath(pathTrue);
        } else {
            actionResult.setOutput("If evaluated to false. Next actions path: " + localCondition);
            execution.setActivePath(pathFalse);
        }

        return actionResult;
    }


    public String getCondition() {
        return condition;
    }

    public void setCondition(String condition) {
        this.condition = condition;
    }

    public String getPathTrue() {
        return pathTrue;
    }

    public void setPathTrue(String pathTrue) {
        this.pathTrue = pathTrue;
    }

    public String getPathFalse() {
        return pathFalse;
    }

    public void setPathFalse(String pathFalse) {
        this.pathFalse = pathFalse;
    }
}
