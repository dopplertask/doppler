package com.dopplertask.doppler.domain.action.io

import com.dopplertask.doppler.domain.ActionResult
import com.dopplertask.doppler.domain.OutputType
import com.dopplertask.doppler.domain.StatusCode
import com.dopplertask.doppler.domain.TaskExecution
import com.dopplertask.doppler.domain.action.Action
import com.dopplertask.doppler.domain.action.Action.PropertyInformation.PropertyInformationType
import com.dopplertask.doppler.service.BroadcastListener
import com.dopplertask.doppler.service.TaskService
import com.dopplertask.doppler.service.VariableExtractorUtil
import java.io.IOException
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Paths
import javax.persistence.Column
import javax.persistence.DiscriminatorValue
import javax.persistence.Entity
import javax.persistence.Table

@Entity
@Table(name = "ReadFileAction")
@DiscriminatorValue("readfile_action")
class ReadFileAction : Action() {
    @Column
    var filename: String? = null

    @Column
    var parameterName: String? = null

    @Throws(IOException::class)
    override fun run(taskService: TaskService, execution: TaskExecution, variableExtractorUtil: VariableExtractorUtil, broadcastListener: BroadcastListener?): ActionResult {
        var filenameVariable = variableExtractorUtil.extract(filename, execution, scriptLanguage)
        val parameterNameVariable = variableExtractorUtil.extract(parameterName, execution, scriptLanguage)
        return try { // Support shell ~ for home directory
            if (filenameVariable.contains("~/") && filenameVariable.startsWith("~")) {
                filenameVariable = filenameVariable.replace("~/", System.getProperty("user.home") + "/")
            }
            val fileContents = Files.readString(Paths.get(filenameVariable), StandardCharsets.UTF_8)
            execution.parameters[parameterNameVariable] = fileContents
            val actionResult = ActionResult()
            actionResult.output = fileContents
            actionResult.outputType = OutputType.STRING
            actionResult.statusCode = StatusCode.SUCCESS
            actionResult
        } catch (e: IOException) {
            val actionResult = ActionResult()
            actionResult.output = "File could not be read [filename=$filenameVariable]"
            actionResult.outputType = OutputType.STRING
            actionResult.statusCode = StatusCode.FAILURE
            actionResult
        }
    }

    override val actionInfo: MutableList<PropertyInformation>
        get() {
            val actionInfo = super.actionInfo
            actionInfo.add(PropertyInformation("filename", "File location", PropertyInformationType.STRING, "", "File path. eg. /home/user/file.txt"))
            actionInfo.add(PropertyInformation("parameterName", "Parameter Name", PropertyInformationType.STRING, "", "Parameter name to store contents."))
            return actionInfo
        }

    override val description: String
        get() = "Reads a file from disk"
}