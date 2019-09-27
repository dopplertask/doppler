package com.dopplertask.doppler.domain.action;

import com.dopplertask.doppler.domain.ActionResult;
import com.dopplertask.doppler.domain.StatusCode;
import com.dopplertask.doppler.domain.TaskExecution;
import com.dopplertask.doppler.service.TaskService;
import com.dopplertask.doppler.service.VariableExtractorUtil;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

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
    @OneToMany(mappedBy = "browseWebAction", cascade = CascadeType.PERSIST)
    private List<UIAction> actionList = new ArrayList<>();

    public BrowseWebAction() {
    }

    public BrowseWebAction(String url) {
        this.url = url;
    }


    @Override
    public ActionResult run(TaskService taskService, TaskExecution execution) {
        String urlVariable = VariableExtractorUtil.extract(url, execution);

        ActionResult actionResult = new ActionResult();

        String os = System.getProperty("os.name");

        if (os.contains("Windows")) {
            System.setProperty(CHROME_DRIVER, "chromedriver.exe");
        } else if (os.contains("Mac")) {
            System.setProperty(CHROME_DRIVER, "chromedriver-mac");
        } else {
            System.setProperty(CHROME_DRIVER, "chromedriver");
        }

        WebDriver webDriver = new ChromeDriver();
        WebDriverWait wait = new WebDriverWait(webDriver, 10);

        // Open page
        webDriver.get(urlVariable);

        for (UIAction uiAction : actionList) {
            if (uiAction.getAction() == UIActionType.WAIT) {
                try {
                    Thread.sleep(Long.parseLong(uiAction.getValue()));
                } catch (Exception e) {
                    actionResult.setErrorMsg("Exception occured during sleeping in UI Action");
                    actionResult.setStatusCode(StatusCode.FAILURE);
                }
            } else if (uiAction.getAction() == UIActionType.ACCEPT_ALERT) {
                try {
                    webDriver.switchTo().alert().accept();
                } catch (Exception e) {
                    actionResult.setErrorMsg("Exception occured during accepting alert in UI Action");
                    actionResult.setStatusCode(StatusCode.FAILURE);
                }
            } else {
                // Normal UI Actions
                WebElement element = findWebElement(uiAction, wait, actionResult);
                executeUIAction(uiAction, element);
            }
        }

        webDriver.quit();

        actionResult.setOutput("WebDriver executed successfully");
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

    private void executeUIAction(UIAction uiAction, WebElement element) {
        if (element != null) {
            switch (uiAction.getAction()) {
                case PRESS:
                    element.click();
                    break;
                case WRITE:
                    element.sendKeys(uiAction.getValue());
                    break;
                case SELECT:
                    ((Select) element).selectByVisibleText(uiAction.getValue());
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
        actionList.forEach(uiAction -> uiAction.setBrowseWebAction(this));
    }
}

