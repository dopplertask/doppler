package com.dopplertask.doppler;

import com.dopplertask.doppler.domain.ActionPort;
import com.dopplertask.doppler.domain.ActionPortType;
import com.dopplertask.doppler.domain.ActionResult;
import com.dopplertask.doppler.domain.Connection;
import com.dopplertask.doppler.domain.TaskExecution;
import com.dopplertask.doppler.domain.action.common.IfAction;
import com.dopplertask.doppler.domain.action.common.PrintAction;
import com.dopplertask.doppler.domain.action.common.XMLAction;
import com.dopplertask.doppler.domain.action.common.XMLActionType;
import com.dopplertask.doppler.service.VariableExtractorUtil;
import org.apache.velocity.app.VelocityEngine;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;
import java.util.List;

@RunWith(SpringRunner.class)
public class ActionTest {

    private VariableExtractorUtil variableExtractorUtil;

    @Before
    public void startUp() {
        VelocityEngine engine = new VelocityEngine();
        variableExtractorUtil = new VariableExtractorUtil(engine);
    }

    @Test
    public void testIfActionShouldReturnTrue() throws IOException {
        IfAction ifAction = new IfAction();
        PrintAction printActionTrue = new PrintAction();
        printActionTrue.setMessage("True path");

        PrintAction printActionFalse = new PrintAction();
        printActionFalse.setMessage("False path");

        TaskExecution taskExecution = new TaskExecution();

        Connection connectionTrue = new Connection();
        connectionTrue.setTarget(new ActionPort() {{
            setAction(printActionTrue);
        }});

        Connection connectionFalse = new Connection();
        connectionFalse.setTarget(new ActionPort() {{
            setAction(printActionFalse);
        }});


        ActionPort output1 = new ActionPort();
        output1.setPortType(ActionPortType.OUTPUT);
        output1.setConnectionSource(connectionTrue);
        output1.setAction(ifAction);

        ActionPort output2 = new ActionPort();
        output2.setPortType(ActionPortType.OUTPUT);
        output2.setConnectionSource(connectionFalse);
        output2.setAction(ifAction);


        ifAction.setPorts(List.of(output1, output2));


        ifAction.setCondition("'test' == 'test'");

        ActionResult actionResult = ifAction.run(null, taskExecution, variableExtractorUtil);
        Assert.assertEquals("If evaluated to true.", actionResult.getOutput());
        Assert.assertEquals("True path", ((PrintAction) taskExecution.getCurrentAction()).getMessage());
    }

    @Test
    public void testIfActionShouldReturnFalse() throws IOException {
        IfAction ifAction = new IfAction();
        PrintAction printActionTrue = new PrintAction();
        printActionTrue.setMessage("True path");

        PrintAction printActionFalse = new PrintAction();
        printActionFalse.setMessage("False path");

        TaskExecution taskExecution = new TaskExecution();

        Connection connectionTrue = new Connection();
        connectionTrue.setTarget(new ActionPort() {{
            setAction(printActionTrue);
        }});

        Connection connectionFalse = new Connection();
        connectionFalse.setTarget(new ActionPort() {{
            setAction(printActionFalse);
        }});


        ActionPort output1 = new ActionPort();
        output1.setPortType(ActionPortType.OUTPUT);
        output1.setConnectionSource(connectionTrue);
        output1.setAction(ifAction);

        ActionPort output2 = new ActionPort();
        output2.setPortType(ActionPortType.OUTPUT);
        output2.setConnectionSource(connectionFalse);
        output2.setAction(ifAction);


        ifAction.setPorts(List.of(output1, output2));

        ifAction.setCondition("'test' == 'test_false'");

        ActionResult actionResult = ifAction.run(null, taskExecution, variableExtractorUtil);
        Assert.assertEquals("If evaluated to false.", actionResult.getOutput());
        Assert.assertEquals("False path", ((PrintAction) taskExecution.getCurrentAction()).getMessage());
    }

    @Test
    public void testXMLActionXMLToJSON() throws IOException {
        TaskExecution taskExecution = new TaskExecution();

        XMLAction xmlAction = new XMLAction();
        xmlAction.setContent("<xml><doppler style=\"blue\"></doppler><example>Some text</example></xml>");
        xmlAction.setType(XMLActionType.XML_TO_JSON);

        ActionResult actionResult = xmlAction.run(null, taskExecution, variableExtractorUtil);
        Assert.assertNotEquals("{\"doppler\":{\"style\":\"blue\"},\"example\":\"Some text\"}", actionResult.getOutput());
    }

    @Test
    public void testXMLActionJSONToXML() throws IOException {
        TaskExecution taskExecution = new TaskExecution();

        XMLAction xmlAction = new XMLAction();
        xmlAction.setContent("{\"doppler\":{\"style\":\"blue\"},\"example\":\"Some text\"}");
        xmlAction.setType(XMLActionType.JSON_TO_XML);

        ActionResult actionResult = xmlAction.run(null, taskExecution, variableExtractorUtil);
        Assert.assertNotEquals("<xml><doppler><style>blue</style></doppler><example>Some text</example></xml>", actionResult.getOutput());
    }

}
