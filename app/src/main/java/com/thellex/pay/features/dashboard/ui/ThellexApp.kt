package com.thellex.pay.features.dashboard.ui

import android.app.Application
import com.google.firebase.FirebaseApp

class ThellexApp : Application() {
    override fun onCreate() {
        super.onCreate()
//        ApiClient.initialize(this)
        FirebaseApp.initializeApp(this)
    }
}
