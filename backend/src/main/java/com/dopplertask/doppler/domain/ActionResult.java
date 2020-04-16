package com.dopplertask.doppler.domain;

public class ActionResult {
    private StatusCode statusCode;
    private String errorMsg;
    private String output = "";
    private OutputType outputType = OutputType.STRING;
    private boolean broadcastMessage;

    public ActionResult() {
        this.statusCode = StatusCode.SUCCESS;
        this.broadcastMessage = true;
    }

    public ActionResult(StatusCode statusCode) {
        this.statusCode = statusCode;
        this.broadcastMessage = true;
    }

    public ActionResult(StatusCode statusCode, String output, String errorMsg) {
        this.statusCode = statusCode;
        this.output = output;
        this.errorMsg = errorMsg;
        this.broadcastMessage = true;
    }

    public StatusCode getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(StatusCode statusCode) {
        this.statusCode = statusCode;
    }

    public String getErrorMsg() {
        return errorMsg;
    }

    public void setErrorMsg(String errorMsg) {
        this.errorMsg = errorMsg;
    }

    public String getOutput() {
        return output;
    }

    public void setOutput(String output) {
        this.output = output;
    }

    public OutputType getOutputType() {
        return outputType;
    }

    public void setOutputType(OutputType outputType) {
        this.outputType = outputType;
    }

    public boolean isBroadcastMessage() {
        return broadcastMessage;
    }

    public void setBroadcastMessage(boolean broadcastMessage) {
        this.broadcastMessage = broadcastMessage;
    }
}
