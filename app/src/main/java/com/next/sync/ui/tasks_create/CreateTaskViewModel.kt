package com.next.sync.ui.tasks_create

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.next.sync.core.db.ObjectBox
import com.next.sync.core.db.data.TaskEntity
import com.next.sync.core.di.AccountService
import com.next.sync.core.di.DataBus
import com.next.sync.core.di.DataBusKey
import com.next.sync.core.extensions.toInt
import com.next.sync.core.model.SyncFlowDirection
import com.next.sync.ui.Routes
import com.next.sync.ui.components.bottom_bar.BottomBarScreen
import dagger.hilt.android.lifecycle.HiltViewModel
import io.objectbox.kotlin.boxFor
import kotlinx.coroutines.launch
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

    ) : ViewModel() {
    var state by mutableStateOf(CreateTaskState())

    fun setName(name: String) {
        state = state.copy(name = name)
    }

    fun setDirection(direction: SyncFlowDirection) {
        state = state.copy(direction = direction)
    }

    fun openLocalPicker(navigate: (String) -> Unit) {
        viewModelScope.launch {
            bus.consume(DataBusKey.LocalPathPick) {
                bus.tryCast<String>(it) {
                    state = state.copy(localPath = this)
                }
            }
        }

        navigate(Routes.FolderPickerLocalScreen.name)
    }

    fun openRemotePicker(navigate: (String) -> Unit) {
        viewModelScope.launch {
            bus.consume(DataBusKey.RemotePathPick) {
                bus.tryCast<String>(it) {
                    state = state.copy(remotePath = this)
                }
            }
        }

        navigate(Routes.FolderPickerRemoteScreen.name)
    }

    fun save(navigate: (String) -> Unit) {
        val taskBox = ObjectBox.store.boxFor(TaskEntity::class)

        val task = TaskEntity(
            accountId = accountService.accountId,
            name = state.name,
            remotePath = state.remotePath,
            localPath = state.localPath,
            direction = state.direction.toInt()
        )

        taskBox.put(task)

        navigate(BottomBarScreen.Tasks.route)
    }
}
