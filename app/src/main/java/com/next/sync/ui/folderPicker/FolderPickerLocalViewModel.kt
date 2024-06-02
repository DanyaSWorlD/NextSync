package com.next.sync.ui.folderPicker

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import java.io.File

class FolderPickerLocalViewModel : ViewModel(), IFolderPickerViewModel {
    private var folderState by mutableStateOf(FolderPickerState())

    init {
        val folders = (File("/").listFiles()) ?: arrayOf()

        folderState = folderState.copy(
            path = File("/").absolutePath,
            folders = folders.filter { f -> !f.isFile }.map { f -> f.name }.toList()
        )
    }

    override fun getState(): FolderPickerState {
        return folderState
    }

    override fun select(folder: String) {
        TODO("Not yet implemented")
    }

    override fun up() {
        TODO("Not yet implemented")
    }

}