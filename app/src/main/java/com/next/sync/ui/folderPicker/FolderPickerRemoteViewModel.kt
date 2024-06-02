package com.next.sync.ui.folderPicker

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.next.sync.core.di.DataBus
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class FolderPickerRemoteViewModel @Inject constructor(
    private val bus: DataBus
) : ViewModel(), IFolderPickerViewModel {
    private var folderState by mutableStateOf(FolderPickerState())

    override fun getState(): FolderPickerState {
        return folderState
    }

    override fun select(folder: String) {
        TODO("Not yet implemented")
    }

    override fun up() {
        TODO("Not yet implemented")
    }

    override fun confirm(navigateBack: () -> Unit) {
        TODO("Not yet implemented")
    }

}