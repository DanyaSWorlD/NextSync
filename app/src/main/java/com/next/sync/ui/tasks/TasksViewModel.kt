package com.next.sync.ui.tasks

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.next.sync.core.db.ObjectBox
import com.next.sync.core.db.data.TaskEntity
import com.next.sync.core.db.data.TaskEntity_
import com.next.sync.core.di.AccountService
import com.next.sync.core.di.DataBus
import com.next.sync.core.di.DataBusKey
import com.next.sync.ui.EventViewModel
import com.next.sync.ui.Routes
import com.next.sync.ui.events.TasksEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import io.objectbox.kotlin.boxFor
import javax.inject.Inject

data class TaskState(
    val tasks: List<TaskEntity> = listOf()
)

@HiltViewModel
class TasksViewModel @Inject constructor(
    private val accountService: AccountService,
    private val bus: DataBus
) : EventViewModel<TasksEvent>() {
    var state by mutableStateOf(TaskState())

    override val events: Map<String, (TasksEvent) -> Unit> = mutableMapOf(
        forEvent<TasksEvent.OpenItem> { editTask(it) },
        forEvent<TasksEvent.Delete> { deleteTask(it) }
    )

    init {
        feedData()
    }

    private fun editTask(event: TasksEvent.OpenItem) {
        bus.emit(DataBusKey.TaskId, event.id)
        event.navigate(Routes.CreateTasksScreen.name)
    }

    private fun deleteTask(event: TasksEvent.Delete) {
        val taskBox = ObjectBox.store.boxFor(TaskEntity::class)
        taskBox.remove(event.id)

        val items = state.tasks.filter { it.id != event.id }
        state = state.copy(tasks = items)
    }

    private fun feedData() {
        val taskBox = ObjectBox.store.boxFor(TaskEntity::class)
        val id = accountService.accountId
        val tasksQuery = taskBox.query(TaskEntity_.accountId.equal(id)).build()
        state = state.copy(tasks = tasksQuery.find())
        tasksQuery.close()
    }
}