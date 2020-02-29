package com.dopplertask.doppler.domain.action.ui;

public enum UIActionType {
    /**
     * PRESS: Press on a field. SELECT: Selects an item, usually from a dropdown. WRITE: Write to the field, usually an input. WAIT:
     * Waits a specific amount of time
     */
    PRESS, SELECT, WRITE, WAIT, ACCEPT_ALERT
}
