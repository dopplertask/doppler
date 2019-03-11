package se.feraswilson.taskservice.service;

import se.feraswilson.taskservice.domain.TaskExecution;

public class VariableExtractorUtil {

    public static String extract(String fieldValue, TaskExecution execution) {
        if (fieldValue != null) {
            String replaced = fieldValue.replaceAll("\\$\\{(.*)\\}", "$1");

            if (!replaced.equals(fieldValue)) {
                return execution.getParameters().get(replaced);
            }

            return replaced;
        }

        return "";
    }
}
