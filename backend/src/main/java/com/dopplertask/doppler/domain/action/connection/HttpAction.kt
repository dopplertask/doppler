package com.dopplertask.doppler.domain.action.connection

import com.dopplertask.doppler.domain.ActionResult
import com.dopplertask.doppler.domain.StatusCode
import com.dopplertask.doppler.domain.TaskExecution
import com.dopplertask.doppler.domain.action.Action
import com.dopplertask.doppler.domain.action.Action.PropertyInformation.PropertyInformationType
import com.dopplertask.doppler.service.BroadcastListener
import com.dopplertask.doppler.service.TaskService
import com.dopplertask.doppler.service.VariableExtractorUtil
import java.io.IOException
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.time.Duration
import java.util.*
import java.util.function.Consumer
import javax.persistence.*

@Entity
@Table(name = "HttpAction")
@DiscriminatorValue("http_action")
class HttpAction : Action() {
    @Column
    var url: String? = null

    @OneToMany(mappedBy = "httpAction", cascade = [CascadeType.ALL])
    private var headers: List<HttpHeader> = ArrayList()

    @Column
    var method: String? = null

    @Lob
    @Column(columnDefinition = "TEXT")
    var body: String? = null

    @Throws(IOException::class)
    override fun run(taskService: TaskService, execution: TaskExecution, variableExtractorUtil: VariableExtractorUtil, broadcastListener: BroadcastListener?): ActionResult { // Extract variables
        val urlVariable = variableExtractorUtil.extract(url, execution, scriptLanguage)
        val methodVariable = variableExtractorUtil.extract(method, execution, scriptLanguage)
        val bodyVariable = variableExtractorUtil.extract(body, execution, scriptLanguage)
        val actionResult = ActionResult()
        var builder = HttpRequest.newBuilder()
                .uri(URI.create(urlVariable))
                .timeout(Duration.ofMinutes(1))
        when (methodVariable) {
            "POST" -> builder = builder.POST(HttpRequest.BodyPublishers.ofString(bodyVariable))
            "PUT" -> builder = builder.PUT(HttpRequest.BodyPublishers.ofString(bodyVariable))
            "DELETE" -> builder = builder.DELETE()
            "GET" -> builder = builder.GET()
            else -> {
                actionResult.errorMsg = "HTTP method is not supported! [method=$methodVariable]"
                actionResult.statusCode = StatusCode.FAILURE
            }
        }
        for (entry in headers) {
            builder = builder.header(variableExtractorUtil.extract(entry.headerName, execution, scriptLanguage), variableExtractorUtil.extract(entry.headerValue, execution, scriptLanguage))
        }
        val client = HttpClient.newHttpClient()
        val request = builder.build()
        var response: HttpResponse<String>?
        try {
            response = client.send(request, HttpResponse.BodyHandlers.ofString())
            actionResult.output = "Status Code: " + response.statusCode() + "\nBody: " + response.body()
            actionResult.statusCode = StatusCode.SUCCESS
        } catch (e: IOException) {
            actionResult.errorMsg = "Exception when sending http request: $e"
            actionResult.statusCode = StatusCode.FAILURE
        } catch (e: InterruptedException) {
            actionResult.errorMsg = "Exception when sending http request: $e"
            actionResult.statusCode = StatusCode.FAILURE
        }
        return actionResult
    }

    override val actionInfo: MutableList<PropertyInformation>
        get() {
            val actionInfo = super.actionInfo
            actionInfo.add(PropertyInformation("url", "URL", PropertyInformationType.STRING, "", "Hostname or IP"))
            actionInfo.add(PropertyInformation("method", "Method", PropertyInformationType.DROPDOWN, "GET", "HTTP Method",
                    java.util.List.of(PropertyInformation("GET", "GET"),
                            PropertyInformation("POST", "POST"),
                            PropertyInformation("PUT", "PUT"),
                            PropertyInformation("DELETE", "DELETE")
                    )
            ))
            actionInfo.add(PropertyInformation("body", "Body", PropertyInformationType.MULTILINE, "", "Contents to send"))
            actionInfo.add(PropertyInformation("headers", "Headers", PropertyInformationType.MAP, "", "Username",
                    java.util.List.of(PropertyInformation("headerName", "Key"),
                            PropertyInformation("headerValue", "Value")
                    )))
            return actionInfo
        }

    override val description: String
        get() = "Makes an HTTP request and returns the result"

    fun getHeaders(): List<HttpHeader> {
        return headers
    }

    fun setHeaders(headers: List<HttpHeader>) {
        headers.forEach(Consumer { header: HttpHeader -> header.httpAction = this })
        this.headers = headers
    }

}