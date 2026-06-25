package com.next.sync.core.sync.model

data class SyncProgressState(
    val isRunning: Boolean = false,
    val filesTotal: Int = 0,
    val filesDone: Int = 0,
    val bytesTotal: Long = 0,
    val bytesDone: Long = 0,
    val currentFile: String = "",
    val speedBytesPerSec: Long = 0,
    val estimatedTimeLeftMs: Long = 0L,
    val errors: List<String> = emptyList()
)
