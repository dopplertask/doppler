package se.feraswilson.automationservice.service;

public class VariableExtractorUtil {

    public static String extract(String fieldValue) {
        return fieldValue.replaceAll("\\$\\{(.*)\\}", "$1");
    }
}
