package com.next.sync.core.db.data

import io.objectbox.annotation.Entity
import io.objectbox.annotation.Id

@Entity
data class DirectoryEntity(
    @Id var id: Long = 0,
    var directoryPath: String,
    var fileMap: Map<String, Long>
)