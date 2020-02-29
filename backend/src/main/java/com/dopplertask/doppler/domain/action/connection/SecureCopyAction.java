package com.dopplertask.doppler.domain.action.connection;

import com.dopplertask.doppler.domain.ActionResult;
import com.dopplertask.doppler.domain.SSHManager;
import com.dopplertask.doppler.domain.StatusCode;
import com.dopplertask.doppler.domain.TaskExecution;
import com.dopplertask.doppler.domain.action.Action;
import com.dopplertask.doppler.service.TaskService;
import com.dopplertask.doppler.service.VariableExtractorUtil;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.SftpException;

import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name = "SecureCopyAction")
@DiscriminatorValue("securecopy_action")
public class SecureCopyAction extends Action {


    @Column
    private String hostname;

    @Column
    private String username;

    @Column
    private String password;

    @Column
    private String sourceFilename;

    @Column
    private String destinationFilename;

    @Override
    public ActionResult run(TaskService taskService, TaskExecution execution, VariableExtractorUtil variableExtractorUtil) {
        String connectionIP = variableExtractorUtil.extract(getHostname(), execution);
        String localUsername = variableExtractorUtil.extract(getUsername(), execution);
        String localPassword = variableExtractorUtil.extract(getPassword(), execution);
        String localSourceFilename = variableExtractorUtil.extract(getSourceFilename(), execution);
        String localDestinationFilename = variableExtractorUtil.extract(getDestinationFilename(), execution);

        SSHManager instance = new SSHManager(localUsername, localPassword, connectionIP, "");
        String errorMessage = instance.connect();

        if (errorMessage != null) {
            ActionResult actionResult = new ActionResult();
            actionResult.setErrorMsg(errorMessage);
            actionResult.setStatusCode(StatusCode.FAILURE);
            return actionResult;
        }

        try {
            ChannelSftp sftpChannel = (ChannelSftp) instance.openChannel("sftp");
            sftpChannel.connect();

            sftpChannel.put(localSourceFilename, localDestinationFilename);
            sftpChannel.disconnect();

            // close only after all commands are sent
            instance.close();

            ActionResult actionResult = new ActionResult();
            actionResult.setOutput("File transfer completed [sourceFilename=" + localSourceFilename + ", destinationFilename=" + localDestinationFilename + "]");
            actionResult.setStatusCode(StatusCode.SUCCESS);
            return actionResult;
        } catch (JSchException | SftpException e) {
            ActionResult actionResult = new ActionResult();
            actionResult.setErrorMsg(e.toString());
            actionResult.setStatusCode(StatusCode.FAILURE);
            return actionResult;
        }
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

    public String getSourceFilename() {
        return sourceFilename;
    }

    public void setSourceFilename(String sourceFilename) {
        this.sourceFilename = sourceFilename;
    }

    public String getDestinationFilename() {
        return destinationFilename;
    }

    public void setDestinationFilename(String destinationFilename) {
        this.destinationFilename = destinationFilename;
    }
}
