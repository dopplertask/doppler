package com.dopplertask.doppler.service;

import com.dopplertask.doppler.domain.ActionResult;
import com.dopplertask.doppler.domain.TaskExecution;

import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;

import java.io.StringWriter;

public class VariableExtractorUtil {

    public static String extract(String fieldValue, TaskExecution execution, ActionResult result) {
        if (fieldValue != null) {
            VelocityEngine velocityEngine = new VelocityEngine();
            velocityEngine.init();
            VelocityContext context = new VelocityContext();
            context.put("parameters", execution.getParameters());
            context.put("executionId", execution.getId());
            context.put("logs", execution.getLogs());

            // Useful for retry
            context.put("result", result);

            // Easy access to lastLog
            if (execution != null && execution.getLogs() != null && execution.getLogs().size() > 0) {
                context.put("lastLog", execution.getLogs().get(execution.getLogs().size() - 1));
            }

            StringWriter writer = new StringWriter();

            // Evaluate the original field
            velocityEngine.evaluate(context, writer, "VelExtract", fieldValue);
            return writer.toString();
        }

        return "";
    }

    public static String extract(String filename, TaskExecution execution) {
        return extract(filename, execution, null);
    }
}
