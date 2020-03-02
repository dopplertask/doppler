package com.dopplertask.doppler.domain.action.ui;

import com.dopplertask.doppler.domain.ActionResult;
import com.dopplertask.doppler.domain.StatusCode;
import com.dopplertask.doppler.domain.TaskExecution;
import com.dopplertask.doppler.domain.action.Action;
import com.dopplertask.doppler.service.TaskService;
import com.dopplertask.doppler.service.VariableExtractorUtil;

import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Table;
import java.awt.*;
import java.awt.event.InputEvent;
import java.io.IOException;

@Entity
@Table(name = "MouseAction")
@DiscriminatorValue("mouse_action")
public class MouseAction extends Action {

    @Enumerated(EnumType.STRING)
    @Column
    private MouseActionType action;

    private String positionX;
    private String positionY;

    private String button;

    @Override
    public ActionResult run(TaskService taskService, TaskExecution execution, VariableExtractorUtil variableExtractorUtil) throws IOException {
        String localPositionX = variableExtractorUtil.extract(positionX, execution, getScriptLanguage());
        String localPositionY = variableExtractorUtil.extract(positionY, execution, getScriptLanguage());
        String localButton = variableExtractorUtil.extract(button, execution, getScriptLanguage());

        System.setProperty("java.awt.headless", "false");

        ActionResult actionResult = new ActionResult();

        int selectedButton = InputEvent.BUTTON1_DOWN_MASK;
        if (localButton.equals("LEFT")) {
            selectedButton = InputEvent.BUTTON1_DOWN_MASK;
        } else if (localButton.equals("RIGHT")) {
            selectedButton = InputEvent.BUTTON2_DOWN_MASK;
        }

        if (action == null) {
            actionResult.setStatusCode(StatusCode.FAILURE);
            actionResult.setErrorMsg("Missing action. Ensure that you've entered a supported action.");
            return actionResult;
        }

        if (action == MouseActionType.MOVE && localPositionX.equals("") && localPositionY.equals("")) {
            actionResult.setStatusCode(StatusCode.FAILURE);
            actionResult.setErrorMsg("No X and/or Y position was provided.");
            return actionResult;
        }

        try {

            Robot robot = new Robot();

            switch (action) {
                case MOVE:
                    if (localPositionX != null && localPositionY != null) {
                        robot.mouseMove(Integer.parseInt(localPositionX), Integer.parseInt(localPositionY));
                        actionResult.setOutput("Mouse was moved to X: " + localPositionX + ", Y: " + localPositionY);
                    } else {
                        actionResult.setStatusCode(StatusCode.FAILURE);
                        actionResult.setErrorMsg("Positions for the mouse were not provided.");
                        return actionResult;
                    }
                    break;
                case CLICK:
                    robot.mousePress(selectedButton);
                    robot.mouseRelease(selectedButton);
                    actionResult.setOutput("Mouse " + selectedButton + " was clicked.");
                    break;
                case PRESS:
                    robot.mousePress(selectedButton);
                    actionResult.setOutput("Mouse " + selectedButton + " was pressed.");
                    break;
                case RELEASE:
                    robot.mouseRelease(selectedButton);
                    actionResult.setOutput("Mouse " + selectedButton + " was released.");
                    break;
                default:
                    // Do nothing.
            }

        } catch (AWTException | NumberFormatException e) {
            throw new RuntimeException(e);
        }

        return actionResult;

    }


    public String getPositionX() {
        return positionX;
    }

    public void setPositionX(String positionX) {
        this.positionX = positionX;
    }

    public String getPositionY() {
        return positionY;
    }

    public void setPositionY(String positionY) {
        this.positionY = positionY;
    }

    public MouseActionType getAction() {
        return action;
    }

    public void setAction(MouseActionType action) {
        this.action = action;
    }

    public String getButton() {
        return button;
    }

    public void setButton(String button) {
        this.button = button;
    }
}
