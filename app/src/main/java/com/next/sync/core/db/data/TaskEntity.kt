package com.next.sync.core.db.data

import io.objectbox.annotation.Entity
import io.objectbox.annotation.Id

@Entity
data class TaskEntity(
    @Id var id: Long = 0,
    var accountId: Long,
    var name: String? = null,
    var localPath: String,
    var remotePath: String,
    var direction: Int,
)