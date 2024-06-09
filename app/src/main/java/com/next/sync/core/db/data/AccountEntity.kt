package com.next.sync.core.db.data

import io.objectbox.annotation.Entity
import io.objectbox.annotation.Id

@Entity
data class AccountEntity(
    @Id var id: Long = 0,
    var server: String,
    var user: String,
    var password: String
)