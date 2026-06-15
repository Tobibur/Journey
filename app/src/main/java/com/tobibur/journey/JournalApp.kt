package com.tobibur.journey

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import com.tobibur.journey.notifications.NotificationHelper
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class JournalApp : Application(), Configuration.Provider {

    @Inject lateinit var workerFactory: HiltWorkerFactory
    @Inject lateinit var  notificationHelper: NotificationHelper

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()

    companion object {
        lateinit var instance: JournalApp
            private set
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
        notificationHelper.createChannel()
    }
}