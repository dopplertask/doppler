package com.dopplertask.doppler.domain.action.common;

import com.dopplertask.doppler.domain.ActionResult;
import com.dopplertask.doppler.domain.StatusCode;
import com.dopplertask.doppler.domain.TaskExecution;
import com.dopplertask.doppler.domain.action.Action;
import com.dopplertask.doppler.service.TaskService;
import com.dopplertask.doppler.service.VariableExtractorUtil;

import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.Table;
import java.io.IOException;

@Entity
@Table(name = "TimedWait")
@DiscriminatorValue("timedwait_action")
public class TimedWait extends Action {

    @Column
    private Long seconds;

    public TimedWait() {
    }

    public TimedWait(Long seconds) {
        this.seconds = seconds;
    }


    @Override
    public ActionResult run(TaskService taskService, TaskExecution execution, VariableExtractorUtil variableExtractorUtil) throws IOException {
        String amountOfSeconds = variableExtractorUtil.extract("" + seconds, execution, getScriptLanguage());

        ActionResult actionResult = new ActionResult();

        try {
            Thread.sleep(Integer.parseInt(amountOfSeconds) * 1000);

            actionResult.setOutput(amountOfSeconds);
            actionResult.setStatusCode(StatusCode.SUCCESS);
        } catch (InterruptedException e) {
            actionResult.setErrorMsg("Interrupted wait. Error: " + e.getMessage());
            actionResult.setStatusCode(StatusCode.FAILURE);
        } catch (NumberFormatException e) {
            actionResult.setErrorMsg("Wrong input. Error: " + e.getMessage());
            actionResult.setStatusCode(StatusCode.FAILURE);
        }

        return actionResult;
    }

    public Long getSeconds() {
        return seconds;
    }

    public void setSeconds(Long seconds) {
        this.seconds = seconds;
    }
}
