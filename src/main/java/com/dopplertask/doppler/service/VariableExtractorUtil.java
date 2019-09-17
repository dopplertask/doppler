package com.dopplertask.doppler.service;

import com.dopplertask.doppler.domain.TaskExecution;

import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;

import java.io.StringWriter;

public class VariableExtractorUtil {

    public static String extract(String fieldValue, TaskExecution execution) {
        if (fieldValue != null) {
            VelocityEngine velocityEngine = new VelocityEngine();
            velocityEngine.init();
            VelocityContext context = new VelocityContext();
            context.put("execution", execution);

            // Easy access to lastLog
            if(execution != null && execution.getLogs() != null && execution.getLogs().size() > 0) {
                context.put("lastLog", execution.getLogs().get(execution.getLogs().size() - 1));
            }

            StringWriter writer = new StringWriter();

            String replaced = fieldValue.replaceAll("\\$\\{(.*)\\}", "$1");

            // Replace variable and evaluate the parameter
            if (!replaced.equals(fieldValue) && execution.getParameters().get(replaced) != null) {
                velocityEngine.evaluate(context, writer, "VelExtract", execution.getParameters().get(replaced));
                return writer.toString();
            }

            // Evaluate the original field
            velocityEngine.evaluate(context, writer, "VelExtract", fieldValue);
            return writer.toString();
        }

        return "";
    }
}
