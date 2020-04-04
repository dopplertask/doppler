package com.dopplertask.doppler.domain.action.common;

import com.fasterxml.jackson.annotation.JsonIgnore;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(name = "SwitchCase")
public class SwitchCase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", updatable = false, nullable = false)
    @JsonIgnore
    private Long id;

    @ManyToOne
    @JoinColumn(nullable = false)
    @JsonIgnore
    private SwitchAction switchAction;

    private String currentCase;

    public String getCurrentCase() {
        return currentCase;
    }

    public void setCurrentCase(String currentCase) {
        this.currentCase = currentCase;
    }

    public SwitchAction getSwitchAction() {
        return switchAction;
    }

    public void setSwitchAction(SwitchAction switchAction) {
        this.switchAction = switchAction;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
}
