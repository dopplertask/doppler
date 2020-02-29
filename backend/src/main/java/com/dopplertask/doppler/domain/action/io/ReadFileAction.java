package com.dopplertask.doppler.domain.action.io;

import com.dopplertask.doppler.domain.ActionResult;
import com.dopplertask.doppler.domain.OutputType;
import com.dopplertask.doppler.domain.StatusCode;
import com.dopplertask.doppler.domain.TaskExecution;
import com.dopplertask.doppler.domain.action.Action;
import com.dopplertask.doppler.service.TaskService;
import com.dopplertask.doppler.service.VariableExtractorUtil;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

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

    public ReadFileAction() {
    }

    @Override
    public ActionResult run(TaskService taskService, TaskExecution execution, VariableExtractorUtil variableExtractorUtil) {
        String filenameVariable = variableExtractorUtil.extract(filename, execution);

        try {
            // Support shell ~ for home directory
            if (filenameVariable.contains("~/") && filenameVariable.startsWith("~")) {
                filenameVariable = filenameVariable.replace("~/", System.getProperty("user.home") + "/");
            }

            String fileContents = Files.readString(Paths.get(filenameVariable), StandardCharsets.UTF_8);

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
}
