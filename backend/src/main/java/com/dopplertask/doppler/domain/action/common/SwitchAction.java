package com.dopplertask.doppler.domain.action.common;

import com.dopplertask.doppler.domain.ActionResult;
import com.dopplertask.doppler.domain.StatusCode;
import com.dopplertask.doppler.domain.TaskExecution;
import com.dopplertask.doppler.domain.action.Action;
import com.dopplertask.doppler.service.TaskService;
import com.dopplertask.doppler.service.VariableExtractorUtil;

import javax.persistence.CascadeType;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "SwitchAction")
@DiscriminatorValue("switch_action")
public class SwitchAction extends Action {

    private String value;

    @OneToMany(mappedBy = "switchAction", cascade = CascadeType.ALL)
    private List<SwitchCase> switchCases = new ArrayList<>();


    public SwitchAction() {
        // Init
    }

    @Override
    public ActionResult run(TaskService taskService, TaskExecution execution, VariableExtractorUtil variableExtractorUtil) throws IOException {

        ActionResult actionResult = new ActionResult();
        String localCondition;
        List<String> evaluatedCases = new ArrayList<>();

        if (value != null && !value.isEmpty()) {
            StringBuilder statement = new StringBuilder();
            int i = 0;
            switch (getScriptLanguage()) {
                case VELOCITY:
                    for (SwitchCase switchCase : switchCases) {
                        String evaluatedCase = variableExtractorUtil.extract("\"" + switchCase.getCurrentCase() + "\"", execution, getScriptLanguage());
                        evaluatedCases.add(evaluatedCase);
                        if (i == 0) {
                            statement.append("#if(\"" + value + "\" == \"" + evaluatedCase + "\")" +
                                    "0");
                        } else {
                            statement.append("#elseif(\"" + value + "\" == \"" + evaluatedCase + "\")" +
                                    "" + i);
                        }
                        i++;
                    }
                    statement.append("#end");
                    break;
                case JAVASCRIPT:
                    statement.append("var outputPort = 0;");
                    for (SwitchCase switchCase : switchCases) {
                        String evaluatedCase = variableExtractorUtil.extract("\"" + switchCase.getCurrentCase() + "\"", execution, getScriptLanguage());
                        evaluatedCases.add(evaluatedCase);
                        if (i == 0) {
                            statement.append("if(\"" + value + "\" == \"" + evaluatedCase + "\") {" +
                                    "outputPort = 0; }");
                        } else {
                            statement.append("\nelse if(\"" + value + "\" == \"" + evaluatedCase + "\") {" +
                                    "outputPort = " + i + ";}");
                        }
                        i++;
                    }

                    statement.append("outputPort;");
                    break;
                default:
                    throw new IllegalStateException("Unexpected script engine");
            }

            localCondition = variableExtractorUtil.extract(statement.toString(), execution, getScriptLanguage());


            actionResult.setOutput("Switch evaluated to port nr: " + localCondition);
            execution.setCurrentAction(getOutputPorts().get(Integer.parseInt(localCondition)).getConnectionSource().getTarget().getAction());

        } else {
            actionResult.setStatusCode(StatusCode.FAILURE);
            actionResult.setErrorMsg("Please enter a condition.");
        }
        return actionResult;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    @Override
    public List<PropertyInformation> getActionInfo() {
        List<PropertyInformation> actionInfo = super.getActionInfo();

        actionInfo.add(new PropertyInformation("value", "Value", PropertyInformation.PropertyInformationType.STRING, "", "Value to compare"));
        actionInfo.add(new PropertyInformation("switchCases", "Cases", PropertyInformation.PropertyInformationType.MAP, "", "Cases to match the value", List.of(
                new PropertyInformation("currentCase", "Case")
        )));
        return actionInfo;
    }

    public List<SwitchCase> getSwitchCases() {
        return switchCases;
    }

    public void setSwitchCases(List<SwitchCase> switchCases) {
        switchCases.forEach(switchCase -> switchCase.setSwitchAction(this));
        this.switchCases = switchCases;
    }
}
