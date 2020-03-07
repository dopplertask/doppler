package com.dopplertask.doppler.domain;

import com.dopplertask.doppler.domain.action.Action;
import com.fasterxml.jackson.annotation.JsonIgnore;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Table;

@Entity
@Table
public class ActionPort {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id", updatable = false, nullable = false)
    private Long id;

    @Column
    private String externalId;

    @Enumerated(EnumType.STRING)
    @Column
    private ActionPortType portType = ActionPortType.INPUT;

    @JoinColumn(name = "action_id", referencedColumnName = "id")
    @ManyToOne
    @JsonIgnore
    private Action action;

    @OneToOne(mappedBy = "source", cascade = CascadeType.ALL,
            fetch = FetchType.LAZY, optional = false)
    private Connection connectionSource;

    @OneToOne(mappedBy = "target", cascade = CascadeType.ALL,
            fetch = FetchType.LAZY, optional = false)
    private Connection connectionTarget;


    public ActionPort() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public ActionPortType getPortType() {
        return portType;
    }

    public void setPortType(ActionPortType portType) {
        this.portType = portType;
    }

    public String getExternalId() {
        return externalId;
    }

    public void setExternalId(String externalId) {
        this.externalId = externalId;
    }

    public Connection getConnectionSource() {
        return connectionSource;
    }

    public void setConnectionSource(Connection connectionSource) {
        this.connectionSource = connectionSource;
    }

    public Connection getConnectionTarget() {
        return connectionTarget;
    }

    public void setConnectionTarget(Connection connectionTarget) {
        this.connectionTarget = connectionTarget;
    }

    public Action getAction() {
        return action;
    }

    public void setAction(Action action) {
        this.action = action;
    }
}
