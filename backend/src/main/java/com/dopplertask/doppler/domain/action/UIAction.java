package com.dopplertask.doppler.domain.action;

import com.fasterxml.jackson.annotation.JsonIgnore;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(name = "UIAction")
public class UIAction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @JsonIgnore
    private Long id;

    @ManyToOne
    @JoinColumn(nullable = false)
    @JsonIgnore
    private BrowseWebAction browseWebAction;

    @Column
    private String fieldName;

    @Enumerated(EnumType.STRING)
    @Column
    private UIActionType action = UIActionType.PRESS;

    @Enumerated(EnumType.STRING)
    @Column
    private UIFieldFindByType findByType = UIFieldFindByType.ID;

    @Column
    private String value;


    public BrowseWebAction getBrowseWebAction() {
        return browseWebAction;
    }

    public void setBrowseWebAction(BrowseWebAction browseWebAction) {
        this.browseWebAction = browseWebAction;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public UIActionType getAction() {
        return action;
    }

    public void setAction(UIActionType action) {
        this.action = action;
    }

    public String getFieldName() {
        return fieldName;
    }

    public void setFieldName(String fieldName) {
        this.fieldName = fieldName;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public UIFieldFindByType getFindByType() {
        return findByType;
    }

    public void setFindByType(UIFieldFindByType findByType) {
        this.findByType = findByType;
    }
}
