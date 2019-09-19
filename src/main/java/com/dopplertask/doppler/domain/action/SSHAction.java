package com.dopplertask.doppler.domain.action;

import com.dopplertask.doppler.domain.ActionResult;
import com.dopplertask.doppler.domain.SSHManager;
import com.dopplertask.doppler.domain.StatusCode;
import com.dopplertask.doppler.domain.TaskExecution;
import com.dopplertask.doppler.service.TaskService;
import com.dopplertask.doppler.service.VariableExtractorUtil;

import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.Lob;
import javax.persistence.Table;

@Entity
@Table(name = "SSHAction")
@DiscriminatorValue("ssh_action")
public class SSHAction extends Action {


    @Column
    private String hostname;

    @Column
    private String username;

    @Column
    private String password;

    @Lob
    @Column(columnDefinition = "TEXT")
    private String command;

    @Override
    public ActionResult run(TaskService taskService, TaskExecution execution) {
        String connectionIP = VariableExtractorUtil.extract(getHostname(), execution);
        String userName = VariableExtractorUtil.extract(getUsername(), execution);
        String password = VariableExtractorUtil.extract(getPassword(), execution);
        String command = VariableExtractorUtil.extract(getCommand(), execution);

        SSHManager instance = new SSHManager(userName, password, connectionIP, "");
        String errorMessage = instance.connect();

        if (errorMessage != null) {
            ActionResult actionResult = new ActionResult();
            actionResult.setErrorMsg(errorMessage);
            actionResult.setStatusCode(StatusCode.FAILURE);
            return actionResult;
        }

        // call sendCommand for each command and the output
        //(without prompts) is returned
        String result = instance.sendCommand(command);
        // close only after all commands are sent
        instance.close();

        ActionResult actionResult = new ActionResult();
        actionResult.setOutput(result);
        actionResult.setStatusCode(StatusCode.SUCCESS);
        return actionResult;
    }

    public String getHostname() {
        return hostname;
    }

    public void setHostname(String hostname) {
        this.hostname = hostname;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getCommand() {
        return command;
    }

    public void setCommand(String command) {
        this.command = command;
    }
}
