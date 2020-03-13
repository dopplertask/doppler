package com.dopplertask.doppler.domain.action.connection;

import com.dopplertask.doppler.domain.ActionResult;
import com.dopplertask.doppler.domain.SSHManager;
import com.dopplertask.doppler.domain.StatusCode;
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
import java.util.List;

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
    public ActionResult run(TaskService taskService, TaskExecution execution, VariableExtractorUtil variableExtractorUtil) throws IOException {
        String connectionIP = variableExtractorUtil.extract(getHostname(), execution, getScriptLanguage());
        String userName = variableExtractorUtil.extract(getUsername(), execution, getScriptLanguage());
        String password = variableExtractorUtil.extract(getPassword(), execution, getScriptLanguage());
        String command = variableExtractorUtil.extract(getCommand(), execution, getScriptLanguage());

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

    @Override
    public List<PropertyInformation> getActionInfo() {
        List<PropertyInformation> actionInfo = super.getActionInfo();

        actionInfo.add(new PropertyInformation("hostname", "Hostname", PropertyInformation.PropertyInformationType.STRING, "", "Hostname or IP"));
        actionInfo.add(new PropertyInformation("username", "Username", PropertyInformation.PropertyInformationType.STRING, "", "Username"));
        actionInfo.add(new PropertyInformation("password", "Password", PropertyInformation.PropertyInformationType.STRING, "", "Password"));
        actionInfo.add(new PropertyInformation("command", "Command", PropertyInformation.PropertyInformationType.MULTILINE, "", "Eg. echo \"Hello world\""));
        return actionInfo;
    }
}
