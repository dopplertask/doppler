package com.dopplertask.doppler.domain.action.io;

import com.dopplertask.doppler.domain.ActionResult;
import com.dopplertask.doppler.domain.OutputType;
import com.dopplertask.doppler.domain.StatusCode;
import com.dopplertask.doppler.domain.TaskExecution;
import com.dopplertask.doppler.domain.action.Action;
import com.dopplertask.doppler.service.BroadcastListener;
import com.dopplertask.doppler.service.TaskService;
import com.dopplertask.doppler.service.VariableExtractorUtil;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name = "ReadFileAction")
@DiscriminatorValue("readfile_action")
public class ReadFileAction extends Action {

    @Column
    private String filename;

    @Column
    private String parameterName;

    public ReadFileAction() {
    }

    @Override
    public ActionResult run(TaskService taskService, TaskExecution execution, VariableExtractorUtil variableExtractorUtil, BroadcastListener broadcastListener) throws IOException {
        String filenameVariable = variableExtractorUtil.extract(filename, execution, getScriptLanguage());
        String parameterNameVariable = variableExtractorUtil.extract(parameterName, execution, getScriptLanguage());

        try {
            // Support shell ~ for home directory
            if (filenameVariable.contains("~/") && filenameVariable.startsWith("~")) {
                filenameVariable = filenameVariable.replace("~/", System.getProperty("user.home") + "/");
            }

            String fileContents = Files.readString(Paths.get(filenameVariable), StandardCharsets.UTF_8);

            execution.getParameters().put(parameterNameVariable, fileContents);

            ActionResult actionResult = new ActionResult();
            actionResult.setOutput(fileContents);
            actionResult.setOutputType(OutputType.STRING);
            actionResult.setStatusCode(StatusCode.SUCCESS);

            return actionResult;
        } catch (IOException e) {
            ActionResult actionResult = new ActionResult();
            actionResult.setOutput("File could not be read [filename=" + filenameVariable + "]");
            actionResult.setOutputType(OutputType.STRING);
            actionResult.setStatusCode(StatusCode.FAILURE);
            return actionResult;
        }
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public String getParameterName() {
        return parameterName;
    }

    public void setParameterName(String parameterName) {
        this.parameterName = parameterName;
    }

    @Override
    public List<PropertyInformation> getActionInfo() {
        List<PropertyInformation> actionInfo = super.getActionInfo();

        actionInfo.add(new PropertyInformation("filename", "File location", PropertyInformation.PropertyInformationType.STRING, "", "File path. eg. /home/user/file.txt"));
        actionInfo.add(new PropertyInformation("parameterName", "Parameter Name", PropertyInformation.PropertyInformationType.STRING, "", "Parameter name to store contents."));
        return actionInfo;
    }

    @Override
    public String getDescription() {
        return "Reads a file from disk";
    }
}
