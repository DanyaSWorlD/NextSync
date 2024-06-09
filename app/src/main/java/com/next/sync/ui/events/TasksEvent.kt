package com.next.sync.ui.events

sealed class TasksEvent {
    data class OpenItem(val id: Long, val navigate: (String) -> Unit) : TasksEvent()
    data class Delete(val id: Long) : TasksEvent()
}