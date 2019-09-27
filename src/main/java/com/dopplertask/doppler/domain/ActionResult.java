package com.dopplertask.doppler.domain;

public class ActionResult {
    private StatusCode statusCode;
    private String errorMsg;
    private String output = "";
    private OutputType outputType = OutputType.STRING;

    public ActionResult() {
        this.statusCode = StatusCode.SUCCESS;
    }

    public ActionResult(StatusCode statusCode) {
        this.statusCode = statusCode;
    }

    public ActionResult(StatusCode statusCode, String output, String errorMsg) {
        this.statusCode = statusCode;
        this.output = output;
        this.errorMsg = errorMsg;
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
}
