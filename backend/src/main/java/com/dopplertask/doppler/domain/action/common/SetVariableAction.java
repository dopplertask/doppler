package com.dopplertask.doppler.domain.action.common;

import com.dopplertask.doppler.domain.ActionResult;
import com.dopplertask.doppler.domain.TaskExecution;
import com.dopplertask.doppler.domain.action.Action;
import com.dopplertask.doppler.service.TaskService;
import com.dopplertask.doppler.service.VariableExtractorUtil;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;
import java.util.function.Consumer;

import javax.persistence.CascadeType;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.OneToMany;
import javax.persistence.Table;

@Entity
@Table(name = "SetVariableAction")
@DiscriminatorValue("setvariable_action")
public class SetVariableAction extends Action {

    @OneToMany(mappedBy = "setVariableAction", cascade = CascadeType.ALL)
    private List<SetVariable> setVariableList;

    @Override
    public ActionResult run(TaskService taskService, TaskExecution execution, VariableExtractorUtil variableExtractorUtil) {

        ActionResult actionResult = new ActionResult();
        StringBuilder builder = new StringBuilder();

        setVariableList.forEach(setVariable -> {
            if (setVariable.getValue() != null) {
                String evaluatedValue = variableExtractorUtil.extract(setVariable.getValue(), execution);
                execution.getParameters().put(setVariable.getName(), evaluatedValue);

                builder.append("Setting variable [key=" + setVariable.getName() + ", value=" + evaluatedValue + "]\n");
            }
        });

        actionResult.setOutput(builder.toString());
        return actionResult;
    }


    public List<SetVariable> getSetVariableList() {
        return setVariableList;
    }

    public void setSetVariableList(List<SetVariable> setVariableList) {
        setVariableList.forEach(setVariable -> setVariable.setSetVariableAction(this));
        this.setVariableList = setVariableList;
    }


    private static class StreamGobbler implements Runnable {
        private InputStream inputStream;
        private Consumer<String> consumer;

        public StreamGobbler(InputStream inputStream, Consumer<String> consumer) {
            this.inputStream = inputStream;
            this.consumer = consumer;
        }

        @Override
        public void run() {
            new BufferedReader(new InputStreamReader(inputStream)).lines()
                    .forEach(consumer);
        }
    }
}
