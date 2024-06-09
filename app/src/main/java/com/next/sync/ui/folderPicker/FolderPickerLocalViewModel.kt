package com.next.sync.ui.folderPicker

import android.os.Environment
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.next.sync.core.di.DataBus
import com.next.sync.core.di.DataBusKey
import dagger.hilt.android.lifecycle.HiltViewModel
import java.io.File
import javax.inject.Inject

@HiltViewModel
class FolderPickerLocalViewModel @Inject constructor(
    private val bus: DataBus
) : ViewModel(), IFolderPickerViewModel {
    private var folderState by mutableStateOf(FolderPickerState())
    private var file: File? = null

    init {
        val root = Environment.getExternalStorageDirectory()
        updateState(root)
    }

    override fun getState(): FolderPickerState {
        return folderState
    }

    override fun select(folder: String) {
        if (file == null)
            file = File(folderState.path)

        val folderToOpen =
            file?.listFiles()?.firstOrNull { file -> file.isDirectory && file.name == folder }

        if (folderToOpen != null) {
            file = folderToOpen
            updateState(folderToOpen)
        }
    }

    override fun up() {
        if (file == null) return

        if (file?.parentFile?.listFiles()?.any() != true) return

        if (file?.parentFile != null)
            file = file?.parentFile

        updateState(file!!)
    }

    override fun confirm(navigateBack: () -> Unit) {
        bus.emit(DataBusKey.LocalPathPick, file?.absolutePath)
        navigateBack()
    }

    private fun updateState(file: File) {
        val folders = file.listFiles() ?: arrayOf()

        folderState = folderState.copy(
            path = file.absolutePath,
            folders = folders.filter { f -> f.isDirectory }.map { f -> f.name }
                .toList()
        )
    }
}