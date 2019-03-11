package se.feraswilson.automationservice.service;

public class VariableExtractorUtil {

    public static String extract(String fieldValue) {
        if (fieldValue != null) {
            return fieldValue.replaceAll("\\$\\{(.*)\\}", "$1");
        }

        return "";
    }
}
