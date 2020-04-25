package com.dopplertask.doppler.domain.action.common;

import com.dopplertask.doppler.domain.ActionResult;
import com.dopplertask.doppler.domain.StatusCode;
import com.dopplertask.doppler.domain.TaskExecution;
import com.dopplertask.doppler.domain.action.Action;
import com.dopplertask.doppler.service.BroadcastListener;
import com.dopplertask.doppler.service.TaskService;
import com.dopplertask.doppler.service.VariableExtractorUtil;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;

import java.io.IOException;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.Lob;
import javax.persistence.Table;

@Entity
@Table(name = "XMLAction")
@DiscriminatorValue("xml_action")
public class XMLAction extends Action {

    @Lob
    @Column(columnDefinition = "TEXT")
    private String content;

    @Column
    private XMLActionType type = XMLActionType.JSON_TO_XML;

    public XMLAction() {
    }


    @Override
    public ActionResult run(TaskService taskService, TaskExecution execution, VariableExtractorUtil variableExtractorUtil, BroadcastListener broadcastListener) throws IOException {
        String contentVariable = variableExtractorUtil.extract(content, execution, getScriptLanguage());

        ActionResult actionResult = new ActionResult();

        if (contentVariable != null && !contentVariable.isEmpty()) {
            if (type == XMLActionType.XML_TO_JSON) {
                XmlMapper xmlMapper = new XmlMapper();
                JsonNode node = xmlMapper.readTree(contentVariable.getBytes());
                ObjectMapper jsonMapper = new ObjectMapper();

                actionResult.setOutput(jsonMapper.writeValueAsString(node));

            } else if (type == XMLActionType.JSON_TO_XML) {
                ObjectMapper objectMapper = new ObjectMapper();
                XmlMapper xmlMapper = new XmlMapper();
                JsonNode tree = objectMapper.readTree(content);

                actionResult.setOutput(xmlMapper.writer().withRootName("xml").writeValueAsString(tree));

            }

            actionResult.setStatusCode(StatusCode.SUCCESS);
        } else {
            actionResult.setErrorMsg("No output");
            actionResult.setStatusCode(StatusCode.FAILURE);
        }
        return actionResult;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    @Override
    public List<PropertyInformation> getActionInfo() {
        List<PropertyInformation> actionInfo = super.actionInfo;

        actionInfo.add(new PropertyInformation("content", "Content", PropertyInformation.PropertyInformationType.MULTILINE));
        actionInfo.add(new PropertyInformation("type", "Mode", PropertyInformation.PropertyInformationType.DROPDOWN, "JSON_TO_XML", "Convert from and to XML", List.of(
                new PropertyInformation("JSON_TO_XML", "JSON to XML"),
                new PropertyInformation("XML_TO_JSON", "XML to JSON")

        )));
        return actionInfo;
    }

    @Override
    public String getDescription() {
        return "Converts from/to XML";
    }

    public XMLActionType getType() {
        return type;
    }

    public void setType(XMLActionType type) {
        this.type = type;
    }
}

