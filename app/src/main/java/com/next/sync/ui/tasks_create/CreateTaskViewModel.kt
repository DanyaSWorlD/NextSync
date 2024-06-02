package com.next.sync.ui.tasks_create

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.next.sync.core.di.DataBus
import com.next.sync.core.di.DataBusKey
import com.next.sync.ui.Routes
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject


data class CreateTaskState(
    val localPath: String = "",
    val remotePath: String = "",
    val name: String = ""
)

@HiltViewModel
class CreateTaskViewModel @Inject constructor(
    private val bus: DataBus
) : ViewModel() {
    var state by mutableStateOf(CreateTaskState())

    fun setName(name: String) {
        state = state.copy(name = name)
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
}