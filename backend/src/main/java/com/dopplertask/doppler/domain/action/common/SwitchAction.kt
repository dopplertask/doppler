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
import java.util.*
import java.util.function.Consumer
import javax.persistence.*

@Entity
@Table(name = "SwitchAction")
@DiscriminatorValue("switch_action")
class SwitchAction : Action() {
    var value: String? = null
    @OneToMany(mappedBy = "switchAction", cascade = [CascadeType.ALL])
    private var switchCases: List<SwitchCase> = ArrayList()

    @Throws(IOException::class)
    override fun run(taskService: TaskService, execution: TaskExecution, variableExtractorUtil: VariableExtractorUtil, broadcastListener: BroadcastListener?): ActionResult {
        val actionResult = ActionResult()
        val localCondition: String
        val evaluatedCases: MutableList<String> = ArrayList()
        if (value != null && !value!!.isEmpty()) {
            val statement = StringBuilder()
            var i = 0
            when (scriptLanguage) {
                ScriptLanguage.VELOCITY -> {
                    for (switchCase in switchCases) {
                        val evaluatedCase = variableExtractorUtil.extract(switchCase.currentCase, execution, scriptLanguage)
                        evaluatedCases.add(evaluatedCase)
                        if (i == 0) {
                            statement.append("#if(\"" + value + "\" == \"" + evaluatedCase + "\")" +
                                    "0")
                        } else {
                            statement.append("#elseif(\"" + value + "\" == \"" + evaluatedCase + "\")" +
                                    "" + i)
                        }
                        i++
                    }
                    // If there is no cases then we will not print out anything.
                    if (i != 0) {
                        statement.append("#end")
                    }
                }
                ScriptLanguage.JAVASCRIPT -> {
                    statement.append("var outputPort = 0;")
                    for (switchCase in switchCases) {
                        val evaluatedCase = variableExtractorUtil.extract("\"" + switchCase.currentCase + "\"", execution, scriptLanguage)
                        evaluatedCases.add(evaluatedCase)
                        if (i == 0) {
                            statement.append("if(\"" + value + "\" == \"" + evaluatedCase + "\") {" +
                                    "outputPort = 0; }")
                        } else {
                            statement.append("\nelse if(\"" + value + "\" == \"" + evaluatedCase + "\") {" +
                                    "outputPort = " + i + ";}")
                        }
                        i++
                    }
                    statement.append("outputPort;")
                }
                else -> throw IllegalStateException("Unexpected script engine")
            }
            localCondition = variableExtractorUtil.extract(statement.toString(), execution, scriptLanguage)
            var portNr = 0
            try {
                portNr = localCondition.toInt()
                if (portNr < outputPorts.size) {
                    actionResult.output = "Switch evaluated to port nr: $localCondition"
                    if (outputPorts[portNr].connectionSource != null && outputPorts[portNr].connectionSource.target != null) {
                        execution.currentAction = outputPorts[portNr].connectionSource.target.action
                    }
                } else {
                    actionResult.statusCode = StatusCode.FAILURE
                    actionResult.errorMsg = "Could not match any of the cases."
                }
            } catch (e: NumberFormatException) {
                actionResult.statusCode = StatusCode.FAILURE
                actionResult.errorMsg = "Could not evaluate condition to any path."
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
            actionInfo.add(PropertyInformation("value", "Value", PropertyInformationType.STRING, "", "Value to compare"))
            actionInfo.add(PropertyInformation("switchCases", "Cases", PropertyInformationType.MAP, "", "Cases to match the value", java.util.List.of(
                    PropertyInformation("currentCase", "Case")
            )))
            return actionInfo
        }

    override val description: String
        get() = "Evaluate a condition and route to the desired path"

    fun getSwitchCases(): List<SwitchCase> {
        return switchCases
    }

    fun setSwitchCases(switchCases: List<SwitchCase>) {
        switchCases.forEach(Consumer { switchCase: SwitchCase -> switchCase.switchAction = this })
        this.switchCases = switchCases
    }
}