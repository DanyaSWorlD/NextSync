package com.next.sync.core.di

import android.content.Context
import com.next.sync.core.sync.model.Progress
import com.next.sync.ui.components.notifications.ProgressNotification
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class NotificationModule @Inject constructor(
    @ApplicationContext private val context: Context,
    private val bus: DataBus
) {
    init {
        bus.register(DataBusKey.ProgressFlowReset) {
            bus.tryCast<Flow<Progress>>(it) {
                ProgressNotification(context, this).show()
            }
        }
    }
}