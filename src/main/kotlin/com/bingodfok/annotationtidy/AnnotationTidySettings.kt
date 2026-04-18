package com.bingodfok.annotationtidy

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.BaseState
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.SettingsCategory
import com.intellij.openapi.components.SimplePersistentStateComponent
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage

@Service(Service.Level.APP)
@State(
    name = "AnnotationTidySettings",
    storages = [Storage("annotation-tidy.xml")],
    category = SettingsCategory.TOOLS,
)
class AnnotationTidySettings : SimplePersistentStateComponent<AnnotationTidySettings.State>(State()) {

    class State : BaseState() {
        var reformatCodeAfterTidying by property(true)
    }

    var reformatCodeAfterTidying: Boolean
        get() = state.reformatCodeAfterTidying
        set(value) {
            state.reformatCodeAfterTidying = value
        }

    companion object {
        fun getInstance(): AnnotationTidySettings {
            return ApplicationManager.getApplication().getService(AnnotationTidySettings::class.java)
        }
    }
}
