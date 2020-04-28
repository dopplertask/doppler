package com.dopplertask.doppler.domain.action.trigger

import com.dopplertask.doppler.domain.action.Action
import com.dopplertask.doppler.domain.action.Action.PropertyInformation.PropertyInformationType
import com.fasterxml.jackson.annotation.JsonIgnore
import javax.persistence.Column
import javax.persistence.MappedSuperclass
import javax.persistence.Transient

@MappedSuperclass
abstract class Trigger : Action() {
    @Column
    var path = ""

    @Column
    var triggerSuffix = ""

    @JsonIgnore
    @Transient
    var parameters: Map<String, String>? = null

    override val actionInfo: MutableList<PropertyInformation>
        get() {
            val actionInfo = super.actionInfo
            actionInfo.add(PropertyInformation("path", "Path", PropertyInformationType.STRING))
            return actionInfo
        }
}