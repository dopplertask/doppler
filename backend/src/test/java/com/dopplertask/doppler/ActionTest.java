package com.dopplertask.doppler;

import com.dopplertask.doppler.domain.ActionResult;
import com.dopplertask.doppler.domain.TaskExecution;
import com.dopplertask.doppler.domain.action.IfAction;
import com.dopplertask.doppler.service.VariableExtractorUtil;
import org.apache.velocity.app.VelocityEngine;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
public class ActionTest {

    private VariableExtractorUtil variableExtractorUtil;

    @Before
    public void startUp() {
        VelocityEngine engine = new VelocityEngine();
        variableExtractorUtil = new VariableExtractorUtil(engine);
    }

    @Test
    public void testIfActionShouldReturnTrue() {
        IfAction ifAction = new IfAction();
        TaskExecution taskExecution = new TaskExecution();

        ifAction.setPathFalse("falsePath");
        ifAction.setPathTrue("truePath");
        ifAction.setCondition("'test' == 'test'");

        ActionResult actionResult = ifAction.run(null, taskExecution, variableExtractorUtil);
        Assert.assertEquals("If evaluated to true. Next actions path: truePath", actionResult.getOutput());
        Assert.assertEquals("truePath", taskExecution.getActivePath());
    }

    @Test
    public void testIfActionShouldReturnFalse() {
        IfAction ifAction = new IfAction();
        TaskExecution taskExecution = new TaskExecution();

        ifAction.setPathFalse("falsePath");
        ifAction.setPathTrue("truePath");
        ifAction.setCondition("'test' == 'test_false'");

        ActionResult actionResult = ifAction.run(null, taskExecution, variableExtractorUtil);
        Assert.assertEquals("If evaluated to false. Next actions path: falsePath", actionResult.getOutput());
        Assert.assertEquals("falsePath", taskExecution.getActivePath());
    }

}
