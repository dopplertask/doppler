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
            StringWriter writer = new StringWriter();

            String replaced = fieldValue.replaceAll("\\$\\{(.*)\\}", "$1");

            if (!replaced.equals(fieldValue) && execution.getParameters().get(replaced) != null) {
                velocityEngine.evaluate(context, writer, "VelExtract", execution.getParameters().get(replaced));
                return writer.toString();
            }


            return fieldValue;
        }

        return "";
    }
}
