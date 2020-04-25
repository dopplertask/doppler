package com.dopplertask.doppler.domain.action.trigger

import com.dopplertask.doppler.domain.ActionResult
import com.dopplertask.doppler.domain.OutputType
import com.dopplertask.doppler.domain.StatusCode
import com.dopplertask.doppler.domain.TaskExecution
import com.dopplertask.doppler.service.BroadcastListener
import com.dopplertask.doppler.service.TaskService
import com.dopplertask.doppler.service.VariableExtractorUtil
import java.io.IOException
import javax.persistence.Entity

@Entity
class Webhook : Trigger() {
    @Throws(IOException::class)
    override fun run(taskService: TaskService, execution: TaskExecution, variableExtractorUtil: VariableExtractorUtil, broadcastListener: BroadcastListener?): ActionResult {
        val result = ActionResult()
        result.output = "Webhook triggered"
        result.statusCode = StatusCode.SUCCESS
        result.outputType = OutputType.STRING
        return result
    }

    override val description: String
        get() = "Starts the workflow when the webhook URL is called."
}