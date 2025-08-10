package com.tobibur.journey

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class JournalApp : Application(){
    companion object {
        lateinit var instance: JournalApp
            private set
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
    }
}