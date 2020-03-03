package com.dopplertask.doppler.dto;

import com.dopplertask.doppler.domain.action.Action;

import java.util.List;

public class ActionInfoDto {
    private String name;
    private List<Action.PropertyInformation> propertyInformationList;

    public ActionInfoDto(String name, List<Action.PropertyInformation> propertyInformationList) {
        this.name = name;
        this.propertyInformationList = propertyInformationList;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Action.PropertyInformation> getPropertyInformationList() {
        return propertyInformationList;
    }

    public void setPropertyInformationList(List<Action.PropertyInformation> propertyInformationList) {
        this.propertyInformationList = propertyInformationList;
    }
}
