package com.dopplertask.doppler.domain.action.common;

import com.dopplertask.doppler.domain.ActionResult;
import com.dopplertask.doppler.domain.TaskExecution;
import com.dopplertask.doppler.domain.action.Action;
import com.dopplertask.doppler.service.TaskService;
import com.dopplertask.doppler.service.VariableExtractorUtil;

import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Lob;
import javax.persistence.Table;
import javax.script.ScriptException;

@Entity
@Table(name = "ScriptAction")
@DiscriminatorValue("script_action")
public class ScriptAction extends Action {

    @Lob
    @Column(columnDefinition = "TEXT")
    private String script;

    @Enumerated(EnumType.STRING)
    @Column
    private ScriptType type = ScriptType.VELOCITY;

    public ScriptAction() {
    }

    @Override
    public ActionResult run(TaskService taskService, TaskExecution execution, VariableExtractorUtil variableExtractorUtil) {

        ActionResult actionResult = new ActionResult();
        String localScript;
        try {
            // Check action for null to default to velocity.
            localScript = type == ScriptType.VELOCITY || type == null ? variableExtractorUtil.extract(script, execution) : variableExtractorUtil.extractJavascript(script, execution);
        } catch (ScriptException e) {
            throw new RuntimeException(e);
        }
        actionResult.setOutput(localScript);

        return actionResult;
    }


    public String getScript() {
        return script;
    }

    public void setScript(String script) {
        this.script = script;
    }

    public ScriptType getType() {
        return type;
    }

    public void setType(ScriptType type) {
        this.type = type;
    }
}
