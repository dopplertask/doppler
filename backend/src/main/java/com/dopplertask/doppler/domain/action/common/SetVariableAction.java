package com.dopplertask.doppler.domain.action.common;

import com.dopplertask.doppler.domain.ActionResult;
import com.dopplertask.doppler.domain.TaskExecution;
import com.dopplertask.doppler.domain.action.Action;
import com.dopplertask.doppler.service.BroadcastListener;
import com.dopplertask.doppler.service.TaskService;
import com.dopplertask.doppler.service.VariableExtractorUtil;

import java.io.IOException;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.OneToMany;
import javax.persistence.Table;

@Entity
@Table(name = "SetVariableAction")
@DiscriminatorValue("setvariable_action")
public class SetVariableAction extends Action {

    @OneToMany(mappedBy = "setVariableAction", cascade = CascadeType.ALL)
    private List<SetVariable> setVariableList;

    @Override
    public ActionResult run(TaskService taskService, TaskExecution execution, VariableExtractorUtil variableExtractorUtil, BroadcastListener broadcastListener) throws IOException {

        ActionResult actionResult = new ActionResult();
        StringBuilder builder = new StringBuilder();

        for (SetVariable setVariable : setVariableList) {
            if (setVariable.getValue() != null) {
                String evaluatedValue = variableExtractorUtil.extract(setVariable.getValue(), execution, getScriptLanguage());
                execution.getParameters().put(setVariable.getName(), evaluatedValue);

                builder.append("Setting variable [key=" + setVariable.getName() + ", value=" + evaluatedValue + "]\n");
            }
        }

        actionResult.setOutput(builder.toString());
        return actionResult;
    }


    public List<SetVariable> getSetVariableList() {
        return setVariableList;
    }

    public void setSetVariableList(List<SetVariable> setVariableList) {
        this.setVariableList = setVariableList;
        this.setVariableList.forEach(setVariable -> setVariable.setSetVariableAction(this));
    }

    @Override
    public List<PropertyInformation> getActionInfo() {
        List<PropertyInformation> actionInfo = super.actionInfo;

        actionInfo.add(new PropertyInformation("setVariableList", "Variables", PropertyInformation.PropertyInformationType.MAP, "", "", List.of(
                new PropertyInformation("name", "Name"),
                new PropertyInformation("value", "Value")
        )));

        return actionInfo;
    }

    @Override
    public String getDescription() {
        return "Set or modify a variable";
    }
}
