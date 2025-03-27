package com.massindustries.smarttraffic

import android.app.Application
import com.yandex.mapkit.MapKit
import com.yandex.mapkit.MapKitFactory

class MainApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        MapKitFactory.setApiKey("dfdbb989-440b-4f08-9fa6-25561a17fb96")
    }
}