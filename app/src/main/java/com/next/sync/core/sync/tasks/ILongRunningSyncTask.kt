package com.next.sync.core.sync.tasks

import com.next.sync.core.sync.model.Progress
import kotlinx.coroutines.flow.Flow

interface ILongRunningSyncTask {
    val progressFlow: Flow<Progress?>
}