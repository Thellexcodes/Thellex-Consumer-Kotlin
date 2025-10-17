package com.thellex.pay.network.services

import PaymentStatusEnumDeserializer
import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Build
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.google.gson.GsonBuilder
import com.google.gson.JsonSyntaxException
import com.thellex.pay.R
import com.thellex.pay.core.utils.EventBus
import com.thellex.pay.core.utils.Helpers
import com.thellex.pay.core.utils.deserializers.NotificationKindEnumDeserializer
import com.thellex.pay.core.utils.deserializers.TransactionTypeEnumDeserializer
import com.thellex.pay.data.enums.NotificationEventsEnum
import com.thellex.pay.data.model.ITransactionHistoryDto
import com.thellex.pay.data.model.NotificationEntity
import com.thellex.pay.data.model.NotificationKindEnum
import com.thellex.pay.data.model.PaymentStatusEnum
import com.thellex.pay.data.model.TransactionTypeEnum
import com.thellex.pay.data.model.UserPreferences
import com.thellex.pay.data.model.WalletWebhookEventEnum
import com.thellex.pay.features.auth.viewModel.UserRepository
import com.thellex.pay.features.dashboard.ui.MainActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull

class FCMService : FirebaseMessagingService() {
    private val tag = "FirebaseManager"
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val userRepository by lazy { UserRepository.getInstance(applicationContext) }

    companion object {
        private const val CHANNEL_ID = "app_notifications"
        private const val CHANNEL_NAME = "App Notifications"
        private const val NOTIFICATION_ID = 1001
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifications for app updates and transactions"
                enableLights(true)
                lightColor = Color.BLUE
                enableVibration(true)
            }
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun runIO(block: suspend () -> Unit) {
        serviceScope.launch {
            try {
                Log.d(tag, "Running IO coroutine")
                block()
            } catch (e: Exception) {
                Log.e(tag, "Coroutine error: ${e.localizedMessage}", e)
            }
        }
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        remoteMessage.notification?.let { notification ->
            Log.d(tag, "Notification body: ${notification.body}")
            showNotification(
                title = notification.title ?: "App Update",
                body = notification.body ?: "New update received",
                event = remoteMessage.data["event"]
            )
        }

        if (remoteMessage.data.isNotEmpty()) {
            val status = remoteMessage.data["status"]
            val event = remoteMessage.data["event"]
            val transactionJson = remoteMessage.data["transaction"]
            val notificationJson = remoteMessage.data["notification"]

            Log.d(tag, "Extracted event: $event")
            Log.d(tag, "Extracted transaction JSON: $transactionJson")
            Log.d(tag, "Extracted notification JSON: $notificationJson")

            if (!event.isNullOrEmpty()) {
                try {
                    val eventEnum = Helpers.firstMatchingEnum(
                        event,
                        NotificationEventsEnum::fromValue,
                        WalletWebhookEventEnum::fromValue
                    )

                    if (eventEnum != null) {
                        when (eventEnum) {
                            WalletWebhookEventEnum.DEPOSIT_SUCCESSFUL,
                            WalletWebhookEventEnum.WITHDRAW_SUCCESSFUL,
                            NotificationEventsEnum.FIAT_TO_CRYPTO_DEPOSIT,
                            NotificationEventsEnum.CRYPTO_TO_FIAT_WITHDRAWAL -> {
                                runIO {
                                    val gson = GsonBuilder()
                                        .registerTypeAdapter(TransactionTypeEnum::class.java, TransactionTypeEnumDeserializer())
                                        .registerTypeAdapter(PaymentStatusEnum::class.java, PaymentStatusEnumDeserializer())
                                        .registerTypeAdapter(NotificationKindEnum::class.java, NotificationKindEnumDeserializer())
                                        .create()

                                    val transaction = transactionJson?.let {
                                        try {
                                            gson.fromJson(it, ITransactionHistoryDto::class.java)
                                        } catch (e: JsonSyntaxException) {
                                            Log.e(tag, "Invalid transaction JSON: $it", e)
                                            null
                                        }
                                    }

                                    val notification = notificationJson?.let {
                                        try {
                                            gson.fromJson(it, NotificationEntity::class.java)
                                        } catch (e: JsonSyntaxException) {
                                            Log.e(tag, "Invalid notification JSON: $it", e)
                                            null
                                        }
                                    }

                                    Log.d(tag, "Transaction parsed: $transaction")

                                    transaction?.let {
                                        val existing = UserPreferences.getTransactionById(applicationContext, it.id)
                                        if (existing == null) {
                                            UserPreferences.addTransactionHistory(applicationContext, it)
                                            Log.d(tag, "Added new transaction for event: ${eventEnum.name}, ID: ${it.id}")
                                        } else {
                                            UserPreferences.updateTransactionById(applicationContext, it.id, it)
                                            Log.d(tag, "Updated transaction for event: ${eventEnum.name}, ID: ${it.id}")
                                        }
                                        EventBus.postTransactionUpdate(it)
                                    }

                                    notification?.let {
                                        UserPreferences.addNotification(applicationContext, it)
                                        Log.d(tag, "Saved notification for event: ${eventEnum.name}")
                                        showNotification(
                                            title = it.title ?: "App Update",
                                            body = it.message ?: "New transaction or event received",
                                            event = event
                                        )
                                    }
                                }
                            }
                            else -> {
                                Log.w(tag, "Unhandled event: ${eventEnum.name}")
                            }
                        }
                    } else {
                        Log.w(tag, "Unknown event received: $event")
                    }
                } catch (e: Exception) {
                    Log.e(tag, "Failed to parse incoming data: ${e.message}", e)
                }
            } else {
                Log.w(tag, "Ignoring message due to invalid status or missing event: status=$status, event=$event")
            }
        } else {
            Log.w(tag, "No data payload in message")
        }
    }

    override fun onNewToken(fcmToken: String) {
        super.onNewToken(fcmToken)
        Log.d(tag, "New FCM token received: $fcmToken")
        runIO {
            try {
                Log.d(tag, "Fetching user auth token")
                val userAuthToken = withTimeoutOrNull(5000) {
                    userRepository.getToken().first { !it.isNullOrBlank() }
                }
                Log.d(tag, "User auth token: $userAuthToken")
                if (!userAuthToken.isNullOrBlank()) {
                    sendFcmTokenToBackend(applicationContext, userAuthToken, fcmToken)
                    Log.d(tag, "FCM token sent to backend successfully")
                } else {
                    Log.w(tag, "User auth token unavailable, cannot send FCM token")
                }
            } catch (e: Exception) {
                Log.e(tag, "Error sending FCM token to backend: ${e.localizedMessage}", e)
            }
        }
    }

    override fun onDeletedMessages() {
        super.onDeletedMessages()
        Log.d(tag, "Messages deleted, refreshing token")
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val newToken = task.result
                Log.d(tag, "Refreshed FCM token: $newToken")
                runIO {
                    val userAuthToken = withTimeoutOrNull(5000) {
                        userRepository.getToken().first { !it.isNullOrBlank() }
                    }
                    if (!userAuthToken.isNullOrBlank()) {
                        sendFcmTokenToBackend(applicationContext, userAuthToken, newToken)
                        Log.d(tag, "Refreshed FCM token sent to backend")
                    }
                }
            } else {
                Log.e(tag, "Failed to refresh FCM token", task.exception)
            }
        }
    }

    private fun showNotification(title: String, body: String, event: String?) {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Create intent to open MainActivity
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        // Create PendingIntent
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Build notification
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_thellex_logo_x)
            .setContentTitle(title)
            .setContentText(body)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        // Post notification
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
                notificationManager.notify(NOTIFICATION_ID, notification)
            } else {
                Log.w(tag, "POST_NOTIFICATIONS permission not granted; notification not posted")
                // Permission must be requested in an Activity
            }
        } else {
            notificationManager.notify(NOTIFICATION_ID, notification)
        }
    }

    private fun sendFcmTokenToBackend(context: Context, userAuthToken: String, fcmToken: String) {
        // Implement your backend API call to send FCM token
    }
}