package com.dopplertask.doppler.domain.action.common;

import com.dopplertask.doppler.domain.ActionResult;
import com.dopplertask.doppler.domain.TaskExecution;
import com.dopplertask.doppler.domain.action.Action;
import com.dopplertask.doppler.service.BroadcastListener;
import com.dopplertask.doppler.service.TaskService;
import com.dopplertask.doppler.service.VariableExtractorUtil;

import java.io.IOException;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.Lob;
import javax.persistence.Table;

@Entity
@Table(name = "ScriptAction")
@DiscriminatorValue("script_action")
public class ScriptAction extends Action {

    @Lob
    @Column(columnDefinition = "TEXT")
    private String script;

    public ScriptAction() {
    }

    @Override
    public ActionResult run(TaskService taskService, TaskExecution execution, VariableExtractorUtil variableExtractorUtil, BroadcastListener broadcastListener) throws IOException {

        ActionResult actionResult = new ActionResult();
        actionResult.setOutput(variableExtractorUtil.extract(script, execution, getScriptLanguage()));

        return actionResult;
    }

    public String getScript() {
        return script;
    }

    public void setScript(String script) {
        this.script = script;
    }

    @Override
    public List<PropertyInformation> getActionInfo() {
        List<PropertyInformation> actionInfo = super.getActionInfo();

        actionInfo.add(new PropertyInformation("script", "Script", PropertyInformation.PropertyInformationType.MULTILINE, "", "Command to execute."));
        return actionInfo;
    }

    @Override
    public String getDescription() {
        return "Run a script written in Javascript or Velocity";
    }

}
