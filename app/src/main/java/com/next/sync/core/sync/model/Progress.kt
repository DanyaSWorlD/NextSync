package com.next.sync.core.sync.model

data class Progress(
    val rate: Long,
    val done: Long,
    val total: Long,
    val fileName: String
)