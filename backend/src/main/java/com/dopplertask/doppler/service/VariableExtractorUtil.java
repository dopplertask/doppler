package com.dopplertask.doppler.service;

import com.dopplertask.doppler.domain.ActionResult;
import com.dopplertask.doppler.domain.TaskExecution;
import org.apache.commons.lang3.StringUtils;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.tools.generic.CollectionTool;
import org.apache.velocity.tools.generic.DateTool;
import org.apache.velocity.tools.generic.EscapeTool;
import org.apache.velocity.tools.generic.LoopTool;
import org.apache.velocity.tools.generic.MathTool;
import org.apache.velocity.tools.generic.NumberTool;
import org.apache.velocity.tools.generic.RenderTool;
import org.apache.velocity.tools.generic.ValueParser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.script.Bindings;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

@Component
public class VariableExtractorUtil {

    private VelocityEngine velocityEngine;

    @Autowired
    public VariableExtractorUtil(VelocityEngine velocityEngine) {
        this.velocityEngine = velocityEngine;
    }

    public String extract(String fieldValue, TaskExecution execution, ActionResult result) {
        if (fieldValue != null) {
            VelocityContext context = new VelocityContext(getVelocityTools());
            context.put("parameters", execution.getParameters());
            context.put("executionId", execution.getId());
            context.put("logs", execution.getLogs());

            // Useful for retry
            if (result != null) {
                context.put("result", result);
            }

            // Easy access to lastLog
            if (execution != null && execution.getLogs() != null && !execution.getLogs().isEmpty()) {
                context.put("lastLog", execution.getLogs().get(execution.getLogs().size() - 1));
            }

            StringWriter writer = new StringWriter();

            // Evaluate the original field
            velocityEngine.evaluate(context, writer, "VelExtract", fieldValue);
            return writer.toString();
        }

        return "";
    }

    public String extract(String fieldValue, TaskExecution execution) {
        return extract(fieldValue, execution, null);
    }

    public String extractJavascript(String fieldValue, TaskExecution execution) throws ScriptException {
        return extractJavascript(fieldValue, execution, null);
    }

    public String extractJavascript(String fieldValue, TaskExecution execution, ActionResult result) throws ScriptException {
        if (fieldValue != null) {
            ScriptEngine engine = new ScriptEngineManager().getEngineByName("nashorn");

            Bindings bindings = engine.createBindings();

            bindings.putAll(getVelocityTools());
            bindings.put("parameters", execution.getParameters());
            bindings.put("executionId", execution.getId());
            bindings.put("logs", execution.getLogs());

            // Useful for retry
            if (result != null) {
                bindings.put("result", result);
            }

            // Easy access to lastLog
            if (execution != null && execution.getLogs() != null && !execution.getLogs().isEmpty()) {
                bindings.put("lastLog", execution.getLogs().get(execution.getLogs().size() - 1));
            }


            StringWriter writer = new StringWriter();

            // Evaluate the original field
            return engine.eval(fieldValue, bindings).toString();

        }

        return "";
    }

    public Map<String, Object> getVelocityTools() {
        Map<String, Object> tools = new HashMap<>();
        tools.put("dateTool", new DateTool());
        tools.put("escapeTool", new EscapeTool());
        tools.put("loopTool", new LoopTool());
        tools.put("mathTool", new MathTool());
        tools.put("numberTool", new NumberTool());
        tools.put("renderTool", new RenderTool());
        tools.put("collectionTool", new CollectionTool());
        tools.put("valueParser", new ValueParser(new HashMap<String, Object>()));
        tools.put("stringUtils", new StringUtils());
        return tools;
    }
}
