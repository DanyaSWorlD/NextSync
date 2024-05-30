package com.next.sync

import android.app.Application
import com.next.sync.core.db.ObjectBox
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class App : Application() {
    override fun onCreate() {
        super.onCreate()
        ObjectBox.init(this)
    }
}