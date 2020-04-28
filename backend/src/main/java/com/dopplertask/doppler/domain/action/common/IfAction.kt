package com.dopplertask.doppler.domain.action.common

import com.dopplertask.doppler.domain.ActionResult
import com.dopplertask.doppler.domain.StatusCode
import com.dopplertask.doppler.domain.TaskExecution
import com.dopplertask.doppler.domain.action.Action
import com.dopplertask.doppler.domain.action.Action.PropertyInformation.PropertyInformationType
import com.dopplertask.doppler.service.BroadcastListener
import com.dopplertask.doppler.service.TaskService
import com.dopplertask.doppler.service.VariableExtractorUtil
import java.io.IOException
import javax.persistence.DiscriminatorValue
import javax.persistence.Entity
import javax.persistence.Table

@Entity
@Table(name = "IfAction")
@DiscriminatorValue("if_action")
class IfAction : Action() {
    var condition: String? = null

    @Throws(IOException::class)
    override fun run(taskService: TaskService, execution: TaskExecution, variableExtractorUtil: VariableExtractorUtil, broadcastListener: BroadcastListener?): ActionResult {
        val actionResult = ActionResult()
        val localCondition: String

        if (condition?.isNotEmpty() == true) {
            localCondition = when (scriptLanguage) {
                ScriptLanguage.VELOCITY -> variableExtractorUtil.extract("#if($condition)\ntrue#else\nfalse#end", execution, scriptLanguage)
                ScriptLanguage.JAVASCRIPT -> variableExtractorUtil.extract("if($condition) {\n\"true\"; } else {\n\"false\";}", execution, ScriptLanguage.JAVASCRIPT)
            }
            if ("true" == localCondition) {
                actionResult.output = "If evaluated to true."
                if (outputPorts.isNotEmpty()) execution.currentAction = outputPorts[0].connectionSource?.target?.action
            } else {
                actionResult.output = "If evaluated to false."
                if (outputPorts.size > 1) execution.currentAction = outputPorts[1].connectionSource?.target?.action
            }
        } else {
            actionResult.statusCode = StatusCode.FAILURE
            actionResult.errorMsg = "Please enter a condition."
        }
        return actionResult
    }

    override val actionInfo: MutableList<PropertyInformation>
        get() {
            val actionInfo = super.actionInfo
            actionInfo.add(PropertyInformation("condition", "Condition", PropertyInformationType.STRING, "", "Condition to evaluate."))
            return actionInfo
        }

    override val description: String = "Evaluate a condition to decide the workflow route"
}