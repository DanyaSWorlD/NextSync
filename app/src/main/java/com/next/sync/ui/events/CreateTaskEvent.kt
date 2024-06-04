package com.next.sync.ui.events

import com.next.sync.core.model.SyncFlowDirection

sealed class CreateTaskEvent {
    data class SetName(val name: String) : CreateTaskEvent()
    data class SetDirection(val direction: SyncFlowDirection) : CreateTaskEvent()
    data class OpenLocalPicker(val navigate: (String) -> Unit) : CreateTaskEvent()
    data class OpenRemotePicker(val navigate: (String) -> Unit) : CreateTaskEvent()
    data class Save(val navigate: (String) -> Unit) : CreateTaskEvent()
}