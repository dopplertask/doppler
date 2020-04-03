package com.dopplertask.doppler.domain.action.io;

import com.dopplertask.doppler.domain.ActionResult;
import com.dopplertask.doppler.domain.OutputType;
import com.dopplertask.doppler.domain.StatusCode;
import com.dopplertask.doppler.domain.TaskExecution;
import com.dopplertask.doppler.domain.action.Action;
import com.dopplertask.doppler.service.TaskService;
import com.dopplertask.doppler.service.VariableExtractorUtil;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name = "WriteFileAction")
@DiscriminatorValue("writefile_action")
public class WriteFileAction extends Action {

    @Column
    private String filename;

    @Column
    private String contents;

    public WriteFileAction() {
    }

    @Override
    public ActionResult run(TaskService taskService, TaskExecution execution, VariableExtractorUtil variableExtractorUtil) throws IOException {
        String filenameVariable = variableExtractorUtil.extract(filename, execution, getScriptLanguage());
        String contentsVariable = variableExtractorUtil.extract(contents, execution, getScriptLanguage());

        try {
            // Support shell ~ for home directory
            if (filenameVariable.contains("~/") && filenameVariable.startsWith("~")) {
                filenameVariable = filenameVariable.replace("~/", System.getProperty("user.home") + "/");
            }

            Files.writeString(Paths.get(filenameVariable), contentsVariable, StandardOpenOption.CREATE, StandardOpenOption.WRITE);

            ActionResult actionResult = new ActionResult();
            actionResult.setOutput(contents);
            actionResult.setOutputType(OutputType.STRING);
            actionResult.setStatusCode(StatusCode.SUCCESS);

            return actionResult;
        } catch (IOException e) {
            ActionResult actionResult = new ActionResult();
            actionResult.setOutput("File could not be written [filename=" + filenameVariable + "]");
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

    @Override
    public List<PropertyInformation> getActionInfo() {
        List<PropertyInformation> actionInfo = super.getActionInfo();

        actionInfo.add(new PropertyInformation("filename", "File location", PropertyInformation.PropertyInformationType.STRING, "", "File path. eg. /home/user/file.txt"));
        actionInfo.add(new PropertyInformation("contents", "Contents", PropertyInformation.PropertyInformationType.MULTILINE, "", "Contents of the file"));
        return actionInfo;
    }

    public String getContents() {
        return contents;
    }

    public void setContents(String contents) {
        this.contents = contents;
    }
}
