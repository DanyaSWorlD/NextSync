package com.next.sync.core.sync.model

data class SyncRunRecord(
    val id: Long,
    val timestamp: Long,
    val filesTotal: Int,
    val filesDone: Int,
    val bytesTotal: Long,
    val bytesDone: Long,
    val durationMs: Long,
    val errors: List<String> = emptyList()
)
