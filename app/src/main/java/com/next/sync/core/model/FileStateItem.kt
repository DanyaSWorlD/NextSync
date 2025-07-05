package com.next.sync.core.model

import kotlinx.serialization.Serializable

@Serializable
class FileStateItem(
    val relativePath: String,
) {
    var isFolder: Boolean = false
    var tag: String? = null
    var lastEdited: Long = 0
    var fileSize: Long = -1
    var child: MutableList<FileStateItem> = mutableListOf()
}