package com.next.sync.ui.folderPicker

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.next.sync.core.di.DataBus
import com.next.sync.core.di.DataBusKey
import com.next.sync.core.di.NextcloudClientHelper
import com.owncloud.android.lib.resources.files.ReadFolderRemoteOperation
import com.owncloud.android.lib.resources.files.model.RemoteFile
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FolderPickerRemoteViewModel @Inject constructor(
    private val bus: DataBus, private val nextcloud: NextcloudClientHelper
) : ViewModel(), IFolderPickerViewModel {
    private var folderState by mutableStateOf(FolderPickerState())

    private var path: MutableList<String> = mutableListOf()

    init {
        loadFiles()
    }

    override fun getState(): FolderPickerState {
        return folderState
    }

    override fun select(folder: String) {
        path.add(getName(folder))
        loadFiles()
    }

    override fun up() {
        if (path.isEmpty()) return
        path.remove(path.last())
        loadFiles()
    }

    override fun confirm(navigateBack: () -> Unit) {
        bus.emit(DataBusKey.RemotePathPick, getPath(path))
        navigateBack()
    }

    private fun loadFiles() {
        folderState = folderState.copy(isLoading = true)
        getFiles {
            updateState(it)
        }
    }

    private fun updateState(files: List<RemoteFile>) {
        folderState = folderState.copy(
            path = getPath(path),
            folders = files.subList(1, files.size).filter { f -> f.mimeType == "DIR" }
                .map { f -> getName(f.remotePath!!) },
            isLoading = false
        )
    }

    private fun getFiles(callback: (List<RemoteFile>) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            val result =
                ReadFolderRemoteOperation(getPath(path)).execute(nextcloud.ownCloudClient!!)
            callback(result.data as List<RemoteFile>)
        }
    }

    private fun getName(file: String): String {
        return file.split("/").last { x -> x.isNotEmpty() }
    }

    private fun getPath(path: List<String>): String {
        return "/" + path.joinToString(separator = "/")
    }
}