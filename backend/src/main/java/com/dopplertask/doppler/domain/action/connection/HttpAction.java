package com.dopplertask.doppler.domain.action.connection;

import com.dopplertask.doppler.domain.ActionResult;
import com.dopplertask.doppler.domain.StatusCode;
import com.dopplertask.doppler.domain.TaskExecution;
import com.dopplertask.doppler.domain.action.Action;
import com.dopplertask.doppler.service.TaskService;
import com.dopplertask.doppler.service.VariableExtractorUtil;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.MapKeyColumn;
import javax.persistence.Table;

@Entity
@Table(name = "HttpAction")
@DiscriminatorValue("http_action")
public class HttpAction extends Action {

    @Column
    private String url;

    @ElementCollection
    @MapKeyColumn(name = "headerName")
    @Column(name = "headerValue")
    @CollectionTable(name = "httpaction_headers", joinColumns = @JoinColumn(name = "httpaction_id"))
    private Map<String, String> headers = new HashMap<>();

    @Column
    private String method;

    @Lob
    @Column(columnDefinition = "TEXT")
    private String body;

    public HttpAction() {

    }

    @Override
    public ActionResult run(TaskService taskService, TaskExecution execution, VariableExtractorUtil variableExtractorUtil) {

        // Extract variables
        String urlVariable = variableExtractorUtil.extract(url, execution);
        String methodVariable = variableExtractorUtil.extract(method, execution);
        String bodyVariable = variableExtractorUtil.extract(body, execution);

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


        for (Map.Entry<String, String> entry : headers.entrySet()) {
            builder = builder.header(entry.getKey(), variableExtractorUtil.extract(entry.getValue(), execution));
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
}
