package com.thellex.payments.features.dashboard.ui

import android.app.Application
import com.google.firebase.FirebaseApp
import com.thellex.payments.network.services.ApiClient

class ThellexApp : Application() {
    override fun onCreate() {
        super.onCreate()
//        ApiClient.initialize(this)
        FirebaseApp.initializeApp(this)
    }
}
