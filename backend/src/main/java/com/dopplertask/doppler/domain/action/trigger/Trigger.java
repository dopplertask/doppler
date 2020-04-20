package com.dopplertask.doppler.domain.action.trigger;

import com.dopplertask.doppler.domain.action.Action;

public abstract class Trigger extends Action {

    private String path = "";

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }
}
