package com.dopplertask.doppler.domain.action;

import com.dopplertask.doppler.domain.ActionResult;
import com.dopplertask.doppler.domain.TaskExecution;
import com.dopplertask.doppler.service.VariableExtractorUtil;
import com.dopplertask.doppler.domain.StatusCode;
import com.dopplertask.doppler.service.TaskService;

import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name = "TimedWait")
@DiscriminatorValue("timedwait_action")
public class TimedWait extends Action {

    @Column
    private Integer seconds;

    public TimedWait() {
    }

    public TimedWait(Integer seconds) {
        this.seconds = seconds;
    }


    @Override
    public ActionResult run(TaskService taskService, TaskExecution execution) {
        String amountOfSeconds = VariableExtractorUtil.extract("" + seconds, execution);

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
}
