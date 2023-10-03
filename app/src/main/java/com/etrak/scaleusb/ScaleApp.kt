package com.etrak.scaleusb

import android.app.Application
import android.util.Log
import com.etrak.scaleusb.di.AppModule
import com.etrak.scaleusb.di.AppModuleImpl

class ScaleApp : Application() {

    companion object {
        lateinit var appModule: AppModule
    }

    override fun onCreate() {
        super.onCreate()
        appModule = AppModuleImpl(this)
    }
}