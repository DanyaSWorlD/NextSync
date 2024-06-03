package com.next.sync.ui.tasks

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.next.sync.core.db.ObjectBox
import com.next.sync.core.db.data.TaskEntity
import com.next.sync.core.db.data.TaskEntity_
import com.next.sync.core.di.AccountService
import dagger.hilt.android.lifecycle.HiltViewModel
import io.objectbox.kotlin.boxFor
import javax.inject.Inject

data class TaskState(
    val tasks: List<TaskEntity> = listOf()
)

@HiltViewModel
class TasksViewModel @Inject constructor(
    private val accountService: AccountService
) : ViewModel() {
    var state by mutableStateOf(TaskState())

    init {
        feedData()
    }

    private fun feedData() {
        val taskBox = ObjectBox.store.boxFor(TaskEntity::class)
        val id = accountService.accountId
        val tasksQuery = taskBox.query(TaskEntity_.accountId.equal(id)).build()
        state = state.copy(tasks = tasksQuery.find())
        tasksQuery.close()
    }
}