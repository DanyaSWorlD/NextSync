package com.next.sync.core.db.data

import io.objectbox.annotation.Entity
import io.objectbox.annotation.Id

@Entity
data class FileStateEntity(
    @Id
    var id: Long = 0,
    var taskId: Long,
    var folderContent: String
)