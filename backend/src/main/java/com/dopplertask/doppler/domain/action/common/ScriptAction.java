package com.dopplertask.doppler.domain.action.common;

import com.dopplertask.doppler.domain.ActionResult;
import com.dopplertask.doppler.domain.TaskExecution;
import com.dopplertask.doppler.domain.action.Action;
import com.dopplertask.doppler.service.TaskService;
import com.dopplertask.doppler.service.VariableExtractorUtil;

import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.Lob;
import javax.persistence.Table;
import java.io.IOException;

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
    public ActionResult run(TaskService taskService, TaskExecution execution, VariableExtractorUtil variableExtractorUtil) throws IOException {

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

}
