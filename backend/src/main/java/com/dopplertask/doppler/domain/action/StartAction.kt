package com.dopplertask.doppler.domain.action

import com.dopplertask.doppler.domain.ActionResult
import com.dopplertask.doppler.domain.OutputType
import com.dopplertask.doppler.domain.StatusCode
import com.dopplertask.doppler.domain.TaskExecution
import com.dopplertask.doppler.service.BroadcastListener
import com.dopplertask.doppler.service.TaskService
import com.dopplertask.doppler.service.VariableExtractorUtil
import java.io.IOException
import javax.persistence.DiscriminatorValue
import javax.persistence.Entity
import javax.persistence.Table

/**
 * This action defines the start of a task.
 */
@Entity
@Table(name = "StartAction")
@DiscriminatorValue("start_action")
class StartAction : Action() {
    @Throws(IOException::class)
    override fun run(taskService: TaskService, execution: TaskExecution, variableExtractorUtil: VariableExtractorUtil, broadcastListener: BroadcastListener?): ActionResult {
        val result = ActionResult()
        result.output = "--- Task execution started ---"
        result.statusCode = StatusCode.SUCCESS
        result.outputType = OutputType.STRING
        return result
    }

    override val description: String
        get() = "This is the start of every task."
}