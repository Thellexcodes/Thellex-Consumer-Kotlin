package com.thellex.pos.core.utils

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat
import com.thellex.pos.network.services.SocketService

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (Intent.ACTION_BOOT_COMPLETED == intent.action) {
            val serviceIntent = Intent(context, SocketService::class.java)
            ContextCompat.startForegroundService(context, serviceIntent)
        }
    }
}
