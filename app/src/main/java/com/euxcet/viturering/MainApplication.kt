package com.euxcet.viturering

import android.app.Application
import com.dmitrybrant.modelviewer.ModuleProxy
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class MainApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        ModuleProxy.initModule(this)
    }
}