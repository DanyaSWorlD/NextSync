package com.next.sync.ui.folderPicker

data class FolderPickerState(
    val path: String = "",
    val folders: List<String> = listOf()
)

interface IFolderPickerViewModel {
    fun getState(): FolderPickerState
    fun select(folder: String)
    fun up()
}