package com.thellex.payments.network.services

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.annotation.RequiresApi
import com.google.gson.Gson
import com.thellex.payments.core.utils.Constants
import com.thellex.payments.data.model.NotificationPayload
import com.thellex.payments.data.model.UserPreferences
import com.thellex.payments.data.enums.NotificationSockets
import io.socket.client.IO
import io.socket.client.Socket
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.net.URISyntaxException

class SocketService : Service() {
    private lateinit var socket: Socket
    private var alertID: String = "default-id"
    private val coroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()

        Log.d("SocketService", "SocketService onCreate")

        val notification = Notification.Builder(this, "socket_channel")
            .setContentTitle("Socket Service Running")
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .build()
        startForeground(1, notification)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        alertID = intent?.getStringExtra("alertID") ?: "default-id"
        if (!this::socket.isInitialized) {
            initializeSocket()
        }
        return START_STICKY
    }

    private fun initializeSocket() {
        try {
            val opts = IO.Options().apply {
                forceNew = true
                reconnection = true
                query = "userId=$alertID"
            }

            socket = IO.socket(Constants.BASE_URL, opts)

            socket.on(Socket.EVENT_CONNECT) {
                socket.emit("join", alertID)
            }

            socket.on(NotificationSockets.DEPOSIT_SUCCESSFUL.event) { args ->
                try {
                    val json = args[0] as JSONObject
                    val gson = Gson()
                    val payload = gson.fromJson(json.toString(), NotificationPayload::class.java)


                    coroutineScope.launch {
                        try {
                            val appContext = this@SocketService.applicationContext
                            UserPreferences.addTransactionHistory(appContext, payload.transaction)
                            UserPreferences.addNotification(appContext, payload.notification)

                        } catch (e: Exception) {
                            Log.e("TAG", "Failed to update UserEntity: ${e.message}", e)
                        }
                    }
                } catch (e: Exception) {
                    Log.e("TAG", "Failed to process socket event: ${e.message}", e)
                }
            }

            socket.on(NotificationSockets.WITHDRAWAL_SUCCESSFUL.event) { args ->
                val json = args[0] as JSONObject
                val gson = Gson()
                val payload = gson.fromJson(json.toString(), NotificationPayload::class.java)

                Log.d("TAG", "Received withdrawal payload: $payload")

                coroutineScope.launch {
                    try {
                        val appContext = this@SocketService.applicationContext
                        UserPreferences.updateTransactionById( appContext, payload.transaction.blockchainTxId, payload.transaction )
                        UserPreferences.addNotification(appContext, payload.notification)
                    } catch (e: Exception) {
                        Log.e("TAG", "Failed to update UserEntity: ${e.message}", e)
                    }
                }
            }

            socket.connect()
        } catch (e: URISyntaxException) {
            e.printStackTrace()
            Log.e("TAG", "URI Syntax Exception: ${e.message}", e)
        }
    }

    override fun onDestroy() {
        if (this::socket.isInitialized) {
            socket.disconnect()
            socket.off()
        }
        super.onDestroy()
        coroutineScope.cancel()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val serviceChannel = NotificationChannel(
                "socket_channel",
                "Socket Background Service",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager?.createNotificationChannel(serviceChannel)
        }
    }
}
