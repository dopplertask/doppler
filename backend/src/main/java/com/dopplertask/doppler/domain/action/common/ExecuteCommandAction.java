package com.dopplertask.doppler.domain.action.common;

import com.dopplertask.doppler.domain.ActionResult;
import com.dopplertask.doppler.domain.StatusCode;
import com.dopplertask.doppler.domain.TaskExecution;
import com.dopplertask.doppler.domain.action.Action;
import com.dopplertask.doppler.service.TaskService;
import com.dopplertask.doppler.service.VariableExtractorUtil;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.function.Consumer;

import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name = "ExecuteCommandAction")
@DiscriminatorValue("executecommand_action")
public class ExecuteCommandAction extends Action {

    @Column
    private String command;

    @Override
    public ActionResult run(TaskService taskService, TaskExecution execution, VariableExtractorUtil variableExtractorUtil) throws IOException {
        String commandVar = variableExtractorUtil.extract(command, execution, getScriptLanguage());

        boolean isWindows = System.getProperty("os.name")
                .toLowerCase().startsWith("windows");

        ProcessBuilder builder = new ProcessBuilder();
        if (isWindows) {
            builder.command("cmd.exe", "/c", commandVar);
        } else {
            builder.command("sh", "-c", commandVar);
        }
        builder.directory(new File(System.getProperty("user.home")));
        Process process = null;
        ActionResult actionResult = new ActionResult();
        try {
            process = builder.start();

            StringBuilder output = new StringBuilder();
            StreamGobbler streamGobbler =
                    new StreamGobbler(process.getInputStream(), consumer -> output.append(consumer + "\n"));
            streamGobbler.run();

            int exitCode = process.waitFor();
            if (exitCode == 0) {
                actionResult.setOutput(output.toString());
                actionResult.setStatusCode(StatusCode.SUCCESS);
            }
        } catch (InterruptedException | IOException e) {
            actionResult.setStatusCode(StatusCode.FAILURE);
            actionResult.setErrorMsg("Could not execute task: " + e);
        }

        return actionResult;
    }

    public String getCommand() {
        return command;
    }

    public void setCommand(String command) {
        this.command = command;
    }


    private static class StreamGobbler implements Runnable {
        private InputStream inputStream;
        private Consumer<String> consumer;

        public StreamGobbler(InputStream inputStream, Consumer<String> consumer) {
            this.inputStream = inputStream;
            this.consumer = consumer;
        }

        @Override
        public void run() {
            new BufferedReader(new InputStreamReader(inputStream)).lines()
                    .forEach(consumer);
        }
    }
}
