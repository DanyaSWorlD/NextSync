package com.next.sync.ui.tasks_create

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.next.sync.core.db.ObjectBox
import com.next.sync.core.db.data.TaskEntity
import com.next.sync.core.di.AccountService
import com.next.sync.core.di.DataBus
import com.next.sync.core.di.DataBusKey
import com.next.sync.core.extensions.toInt
import com.next.sync.core.model.SyncFlowDirection
import com.next.sync.ui.EventViewModel
import com.next.sync.ui.Routes
import com.next.sync.ui.components.bottom_bar.BottomBarScreen
import com.next.sync.ui.events.CreateTaskEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import io.objectbox.kotlin.boxFor
import javax.inject.Inject


data class CreateTaskState(
    val localPath: String = "",
    val remotePath: String = "",
    val name: String = "",
    val direction: SyncFlowDirection = SyncFlowDirection.ToCloud
)

@HiltViewModel
class CreateTaskViewModel @Inject constructor(
    private val accountService: AccountService,
    private val bus: DataBus,
) : EventViewModel<CreateTaskEvent>() {
    var state by mutableStateOf(CreateTaskState())

    override val events: Map<String, (CreateTaskEvent) -> Unit> = mapOf(
        forEvent<CreateTaskEvent.SetName> { setName(it) },
        forEvent<CreateTaskEvent.SetDirection> { setDirection(it) },
        forEvent<CreateTaskEvent.OpenLocalPicker> { openLocalPicker(it) },
        forEvent<CreateTaskEvent.OpenRemotePicker> { openRemotePicker(it) },
        forEvent<CreateTaskEvent.Save> { save(it) },
    )

    private fun setName(event: CreateTaskEvent.SetName) {
        state = state.copy(name = event.name)
    }

    private fun setDirection(event: CreateTaskEvent.SetDirection) {
        state = state.copy(direction = event.direction)
    }

    private fun openLocalPicker(event: CreateTaskEvent.OpenLocalPicker) {
        bus.consume(DataBusKey.LocalPathPick) {
            bus.tryCast<String>(it) {
                state = state.copy(localPath = this)
            }
        }

        event.navigate(Routes.FolderPickerLocalScreen.name)
    }

    private fun openRemotePicker(event: CreateTaskEvent.OpenRemotePicker) {
        bus.consume(DataBusKey.RemotePathPick) {
            bus.tryCast<String>(it) {
                state = state.copy(remotePath = this)
            }
        }

        event.navigate(Routes.FolderPickerRemoteScreen.name)
    }

    private fun save(event: CreateTaskEvent.Save) {
        val taskBox = ObjectBox.store.boxFor(TaskEntity::class)

        val task = TaskEntity(
            accountId = accountService.accountId,
            name = state.name,
            remotePath = state.remotePath,
            localPath = state.localPath,
            direction = state.direction.toInt()
        )

        taskBox.put(task)

        event.navigate(BottomBarScreen.Tasks.route)
    }
}
