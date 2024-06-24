package com.next.sync.core.di

import android.content.Context
import com.next.sync.core.sync.model.Progress
import com.next.sync.ui.components.notifications.ProgressNotification
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class NotificationModule @Inject constructor(
    @ApplicationContext private val context: Context,
    private val bus: DataBus
) {
    init {
        bus.register(DataBusKey.ProgressFlowReset) {

        }
    }

    fun createProgressNotification() {
        ProgressNotification(context, simple()).show()
    }

    fun simple(): Flow<Progress> = flow { // flow builder
        for (i in 1..100) {
            delay(100)
            emit(Progress(100, i.toLong(), 100, "test"))
        }
    }
}