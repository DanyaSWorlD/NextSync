package com.next.sync.ui.folderPicker

import android.os.Environment
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.next.sync.core.di.DataBus
import com.next.sync.core.di.DataBusKey
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject

@HiltViewModel
class FolderPickerLocalViewModel @Inject constructor(
    private val bus: DataBus
) : ViewModel(), IFolderPickerViewModel {
    private var folderState by mutableStateOf(FolderPickerState())
    private var currentDir: File? = null

    init {
        val root = Environment.getExternalStorageDirectory()
        currentDir = root
        folderState = folderState.copy(path = root.absolutePath, isLoading = true)
        scanAndUpdate(root)
    }

    override fun getState(): FolderPickerState = folderState

    override fun select(folder: String) {
        val dir = currentDir ?: return
        viewModelScope.launch(Dispatchers.IO) {
            val resolved = dir.listFiles()
                ?.firstOrNull { it.isDirectory && it.name == folder }
            if (resolved != null) {
                currentDir = resolved
                withContext(Dispatchers.Main) {
                    folderState = folderState.copy(path = resolved.absolutePath, isLoading = true)
                }
                scanAndUpdate(resolved)
            }
        }
    }

    override fun up() {
        val dir = currentDir ?: return
        viewModelScope.launch(Dispatchers.IO) {
            val parent = dir.parentFile
            if (parent != null) {
                currentDir = parent
                withContext(Dispatchers.Main) {
                    folderState = folderState.copy(path = parent.absolutePath, isLoading = true)
                }
                scanAndUpdate(parent)
            }
        }
    }

    override fun confirm(navigateBack: () -> Unit) {
        bus.emit(DataBusKey.LocalPathPick, currentDir?.absolutePath)
        navigateBack()
    }

    private fun scanAndUpdate(dir: File) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val names = dir.list() ?: emptyArray()
                val count = names.size
                withContext(Dispatchers.Main) {
                    folderState = folderState.copy(filesCount = count)
                }

                val folders = names.filter { name ->
                    try {
                        File(dir, name).isDirectory
                    } catch (_: Exception) {
                        false
                    }
                }.sorted()

                withContext(Dispatchers.Main) {
                    folderState = folderState.copy(folders = folders, isLoading = false)
                }
            } catch (e: Exception) {
                Log.e("FolderPicker", "scan failed", e)
                withContext(Dispatchers.Main) {
                    folderState = folderState.copy(isLoading = false)
                }
            }
        }
    }
}