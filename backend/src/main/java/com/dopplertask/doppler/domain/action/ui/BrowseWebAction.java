package com.dopplertask.doppler.domain.action.ui;

import com.dopplertask.doppler.domain.ActionResult;
import com.dopplertask.doppler.domain.OutputType;
import com.dopplertask.doppler.domain.StatusCode;
import com.dopplertask.doppler.domain.TaskExecution;
import com.dopplertask.doppler.domain.action.Action;
import com.dopplertask.doppler.service.BroadcastListener;
import com.dopplertask.doppler.service.TaskService;
import com.dopplertask.doppler.service.VariableExtractorUtil;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.OneToMany;
import javax.persistence.Table;

@Entity
@Table(name = "BrowseWebAction")
@DiscriminatorValue("browseweb_action")
public class BrowseWebAction extends Action {

    private static final String CHROME_DRIVER = "webdriver.chrome.driver";

    @Column
    private String url;

    @OneToMany(mappedBy = "browseWebAction", cascade = CascadeType.ALL)
    private List<UIAction> actionList = new ArrayList<>();

    @Column(columnDefinition = "BOOLEAN")
    private boolean headless = true;

    public BrowseWebAction() {
    }

    public BrowseWebAction(String url) {
        this.url = url;
    }


    @Override
    public ActionResult run(TaskService taskService, TaskExecution execution, VariableExtractorUtil variableExtractorUtil, BroadcastListener broadcastListener) throws IOException {
        String urlVariable = variableExtractorUtil.extract(url, execution, getScriptLanguage());

        ActionResult actionResult = new ActionResult();

        String os = System.getProperty("os.name");

        if (os.contains("Windows")) {
            System.setProperty(CHROME_DRIVER, "bin/chromedriver.exe");
        } else if (os.contains("Mac")) {
            System.setProperty(CHROME_DRIVER, "bin/chromedriver-mac");
        } else {
            System.setProperty(CHROME_DRIVER, "bin/chromedriver");
        }

        ChromeOptions chromeOptions = new ChromeOptions();
        if (headless) {
            chromeOptions.addArguments("--headless");
        }

        WebDriver webDriver = new ChromeDriver(chromeOptions);
        WebDriverWait wait = new WebDriverWait(webDriver, 10);

        // Open page
        webDriver.get(urlVariable);

        // Go through all actions
        for (UIAction uiAction : actionList) {
            String uiActionValueVariable = variableExtractorUtil.extract(uiAction.getValue() != null ? uiAction.getValue() : "", execution, getScriptLanguage());
            if (uiAction.getAction() == UIActionType.WAIT) {
                try {
                    Thread.sleep(Long.parseLong(uiActionValueVariable));
                    actionResult.setOutput(actionResult.getOutput() + "Slept a specific amount of time [time=" + uiActionValueVariable + "]\n");
                } catch (Exception e) {
                    actionResult.setErrorMsg("Exception occured during sleeping in UI Action");
                    actionResult.setStatusCode(StatusCode.FAILURE);
                    return actionResult;
                }
            } else if (uiAction.getAction() == UIActionType.ACCEPT_ALERT) {
                try {
                    webDriver.switchTo().alert().accept();
                    actionResult.setOutput(actionResult.getOutput() + "Accepted alert\n");
                } catch (Exception e) {
                    actionResult.setErrorMsg("Exception occured during accepting alert in UI Action");
                    actionResult.setStatusCode(StatusCode.FAILURE);
                    return actionResult;
                }
            } else {
                // Normal UI Actions
                WebElement element = findWebElement(uiAction, wait, actionResult);
                if (actionResult.getStatusCode() == StatusCode.FAILURE) {
                    webDriver.quit();
                    return actionResult;
                }

                executeUIAction(uiAction.getAction(), uiAction.getFieldName(), uiActionValueVariable, element, actionResult);

            }
        }

        webDriver.quit();

        actionResult.setOutput(actionResult.getOutput() + "WebDriver executed successfully");
        actionResult.setOutputType(OutputType.STRING);
        actionResult.setStatusCode(StatusCode.SUCCESS);

        return actionResult;
    }

    private WebElement findWebElement(UIAction uiAction, WebDriverWait wait, ActionResult actionResult) {
        WebElement element = null;
        try {
            switch (uiAction.getFindByType()) {
                case ID:
                    element = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id(uiAction.getFieldName())));
                    break;
                case NAME:
                    element = wait.until(ExpectedConditions.visibilityOfElementLocated(By.name(uiAction.getFieldName())));
                    break;
                case XPATH:
                    element = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath(uiAction.getFieldName())));
                    break;
                case CSS:
                    element = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(uiAction.getFieldName())));
                    break;
                default:
                    throw new IllegalStateException("Unexpected value: " + uiAction.getFindByType());
            }

        } catch (Exception e) {
            // Could not find element, ignore, add to action result
            actionResult.setErrorMsg("Exception occured: " + e);
            actionResult.setStatusCode(StatusCode.FAILURE);
        }
        return element;
    }

    private void executeUIAction(UIActionType uiActionType, String fieldName, String uiActionValueVariable, WebElement element, ActionResult actionResult) {
        if (element != null) {
            switch (uiActionType) {
                case PRESS:
                    element.click();
                    actionResult.setOutput(actionResult.getOutput() + "Element has been clicked [element=" + fieldName + "]\n");
                    break;
                case WRITE:
                    element.sendKeys(uiActionValueVariable);
                    actionResult.setOutput(actionResult.getOutput() + "Wrote text to an element [element=" + fieldName + ", text=" + uiActionValueVariable + "]\n");
                    break;
                case SELECT:
                    ((Select) element).selectByVisibleText(uiActionValueVariable);
                    actionResult.setOutput(actionResult.getOutput() + "Selected item from dropdown [element=" + fieldName + ", text=" + uiActionValueVariable + "]\n");
                    break;
                default:
                    // Do nothing
            }
        }
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public List<UIAction> getActionList() {
        return actionList;
    }

    public void setActionList(List<UIAction> actionList) {
        this.actionList = actionList;
        this.actionList.forEach(uiAction -> uiAction.setBrowseWebAction(this));
    }

    public boolean isHeadless() {
        return headless;
    }

    public void setHeadless(boolean headless) {
        this.headless = headless;
    }

    @Override
    public List<PropertyInformation> getActionInfo() {
        List<PropertyInformation> actionInfo = super.actionInfo;

        actionInfo.add(new PropertyInformation("url", "URL", PropertyInformation.PropertyInformationType.STRING, "", "URL of the web page"));
        actionInfo.add(new PropertyInformation("headless", "Headless mode", PropertyInformation.PropertyInformationType.BOOLEAN, "true", "URL of the web page"));

        actionInfo.add(new PropertyInformation("actionList", "Action list", PropertyInformation.PropertyInformationType.MAP, "", "", List.of(
                new PropertyInformation("fieldName", "Field name"),
                new PropertyInformation("action", "Action", PropertyInformation.PropertyInformationType.DROPDOWN, "PRESS", "", List.of(
                        new PropertyInformation("PRESS", "Press"),
                        new PropertyInformation("SELECT", "Select"),
                        new PropertyInformation("WRITE", "Write"),
                        new PropertyInformation("WAIT", "Wait"),
                        new PropertyInformation("ACCEPT_ALERT", "Accept alert")
                )),
                new PropertyInformation("findByType", "Find By Type", PropertyInformation.PropertyInformationType.DROPDOWN, "ID", "", List.of(
                        new PropertyInformation("ID", "Id"),
                        new PropertyInformation("NAME", "Name"),
                        new PropertyInformation("XPATH", "XPath"),
                        new PropertyInformation("CSS", "CSS")
                )),
                new PropertyInformation("value", "Value")
        )));

        return actionInfo;
    }

    @Override
    public String getDescription() {
        return "Browse the web and do GUI actions.";
    }
}

