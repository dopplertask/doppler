package com.dopplertask.doppler.domain.action.connection;

import com.dopplertask.doppler.domain.ActionResult;
import com.dopplertask.doppler.domain.StatusCode;
import com.dopplertask.doppler.domain.TaskExecution;
import com.dopplertask.doppler.domain.action.Action;
import com.dopplertask.doppler.service.BroadcastListener;
import com.dopplertask.doppler.service.TaskService;
import com.dopplertask.doppler.service.VariableExtractorUtil;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.Lob;
import javax.persistence.OneToMany;
import javax.persistence.Table;

@Entity
@Table(name = "HttpAction")
@DiscriminatorValue("http_action")
public class HttpAction extends Action {

    @Column
    private String url;

    @OneToMany(mappedBy = "httpAction", cascade = CascadeType.ALL)
    private List<HttpHeader> headers = new ArrayList<>();

    @Column
    private String method;

    @Lob
    @Column(columnDefinition = "TEXT")
    private String body;

    public HttpAction() {

    }

    @Override
    public ActionResult run(TaskService taskService, TaskExecution execution, VariableExtractorUtil variableExtractorUtil, BroadcastListener broadcastListener) throws IOException {

        // Extract variables
        String urlVariable = variableExtractorUtil.extract(url, execution, getScriptLanguage());
        String methodVariable = variableExtractorUtil.extract(method, execution, getScriptLanguage());
        String bodyVariable = variableExtractorUtil.extract(body, execution, getScriptLanguage());

        ActionResult actionResult = new ActionResult();

        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create(urlVariable))
                .timeout(Duration.ofMinutes(1));

        switch (methodVariable) {
            case "POST":
                builder = builder.POST(HttpRequest.BodyPublishers.ofString(bodyVariable));
                break;
            case "PUT":
                builder = builder.PUT(HttpRequest.BodyPublishers.ofString(bodyVariable));
                break;
            case "DELETE":
                builder = builder.DELETE();
                break;
            case "GET":
                builder = builder.GET();
                break;
            default:
                actionResult.setErrorMsg("HTTP method is not supported! [method=" + methodVariable + "]");
                actionResult.setStatusCode(StatusCode.FAILURE);
        }


        for (HttpHeader entry : headers) {
            builder = builder.header(variableExtractorUtil.extract(entry.getHeaderName(), execution, getScriptLanguage()), variableExtractorUtil.extract(entry.getHeaderValue(), execution, getScriptLanguage()));
        }

        HttpClient client = HttpClient.newHttpClient();

        HttpRequest request = builder.build();

        HttpResponse<String> response = null;

        try {
            response = client.send(request, HttpResponse.BodyHandlers.ofString());

            actionResult.setOutput("Status Code: " + response.statusCode() + "\nBody: " + response.body());
            actionResult.setStatusCode(StatusCode.SUCCESS);
        } catch (IOException e) {
            actionResult.setErrorMsg("Exception when sending http request: " + e);
            actionResult.setStatusCode(StatusCode.FAILURE);
        } catch (InterruptedException e) {
            actionResult.setErrorMsg("Exception when sending http request: " + e);
            actionResult.setStatusCode(StatusCode.FAILURE);
        }

        return actionResult;
    }

    @Override
    public List<PropertyInformation> getActionInfo() {
        List<PropertyInformation> actionInfo = super.actionInfo;

        actionInfo.add(new PropertyInformation("url", "URL", PropertyInformation.PropertyInformationType.STRING, "", "Hostname or IP"));
        actionInfo.add(new PropertyInformation("method", "Method", PropertyInformation.PropertyInformationType.DROPDOWN, "GET", "HTTP Method",
                List.of(new PropertyInformation("GET", "GET"),
                        new PropertyInformation("POST", "POST"),
                        new PropertyInformation("PUT", "PUT"),
                        new PropertyInformation("DELETE", "DELETE")
                )
        ));
        actionInfo.add(new PropertyInformation("body", "Body", PropertyInformation.PropertyInformationType.MULTILINE, "", "Contents to send"));
        actionInfo.add(new PropertyInformation("headers", "Headers", PropertyInformation.PropertyInformationType.MAP, "", "Username",
                List.of(new PropertyInformation("headerName", "Key"),
                        new PropertyInformation("headerValue", "Value")
                )));

        return actionInfo;
    }

    @Override
    public String getDescription() {
        return "Makes an HTTP request and returns the result";
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public List<HttpHeader> getHeaders() {
        return headers;
    }

    public void setHeaders(List<HttpHeader> headers) {
        headers.forEach(header -> header.setHttpAction(this));
        this.headers = headers;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }
}
