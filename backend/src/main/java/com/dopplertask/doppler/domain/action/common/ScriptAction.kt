package com.dopplertask.doppler.domain.action.common

import com.dopplertask.doppler.domain.ActionResult
import com.dopplertask.doppler.domain.TaskExecution
import com.dopplertask.doppler.domain.action.Action
import com.dopplertask.doppler.domain.action.Action.PropertyInformation.PropertyInformationType
import com.dopplertask.doppler.service.BroadcastListener
import com.dopplertask.doppler.service.TaskService
import com.dopplertask.doppler.service.VariableExtractorUtil
import java.io.IOException
import javax.persistence.*

@Entity
@Table(name = "ScriptAction")
@DiscriminatorValue("script_action")
class ScriptAction : Action() {
    @Lob
    @Column(columnDefinition = "TEXT")
    var script: String? = null

    @Throws(IOException::class)
    override fun run(taskService: TaskService, execution: TaskExecution, variableExtractorUtil: VariableExtractorUtil, broadcastListener: BroadcastListener?): ActionResult {
        val actionResult = ActionResult()
        actionResult.output = variableExtractorUtil.extract(script, execution, scriptLanguage)
        return actionResult
    }

    override val actionInfo: MutableList<PropertyInformation>
        get() {
            val actionInfo = super.actionInfo
            actionInfo.add(PropertyInformation("script", "Script", PropertyInformationType.MULTILINE, "", "Command to execute."))
            return actionInfo
        }

    override val description: String
        get() = "Run a script written in Javascript or Velocity"
}