package com.next.sync.ui.folderPicker

data class FolderPickerState(
    val path: String = "",
    val folders: List<String> = listOf(),
    val filesCount: Int = 0,
    val isLoading: Boolean = false,
)

interface IFolderPickerViewModel {
    fun getState(): FolderPickerState
    fun select(folder: String)
    fun up()
    fun confirm(navigateBack: () -> Unit)
}