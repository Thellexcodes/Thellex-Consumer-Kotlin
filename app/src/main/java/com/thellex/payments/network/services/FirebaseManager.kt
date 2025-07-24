package com.thellex.payments.network.services

import android.annotation.SuppressLint
import android.content.Intent
import android.util.Log
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import com.thellex.payments.core.utils.FcmHelper.sendFcmTokenToBackend
import com.thellex.payments.data.enums.NotificationEventsEnum
import com.thellex.payments.data.model.ITransactionHistoryDto
import com.thellex.payments.data.model.NotificationEntity
import com.thellex.payments.data.model.UserPreferences
import com.thellex.payments.features.auth.viewModel.UserRepository
import com.thellex.payments.features.auth.viewModel.UserViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull

@SuppressLint("MissingFirebaseInstanceTokenRefresh")
class FirebaseManager : FirebaseMessagingService() {
    private val tag = "Firebase"
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val userRepository by lazy { UserRepository.getInstance(applicationContext) }
    private val userViewModel by lazy { UserViewModel(applicationContext) }

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
    }

    private fun runIO(block: suspend () -> Unit) {
        serviceScope.launch {
            try {
                block()
            } catch (e: Exception) {
                Log.e(tag, "Coroutine error: ${e.localizedMessage}", e)
            }
        }
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        remoteMessage.notification?.body?.let {
            Log.d(tag, "Notification Body: $it")
        }

        if (remoteMessage.data.isNotEmpty()) {
            Log.d(tag, "Data Payload: ${remoteMessage.data}")

            val status = remoteMessage.data["status"]
            val event = remoteMessage.data["event"]
            val transactionJson = remoteMessage.data["transaction"]
            val notificationJson = remoteMessage.data["notification"]

            if (!event.isNullOrEmpty()) {
                try {
                    val eventEnum = NotificationEventsEnum.fromValue(event)
                    if (eventEnum != null) {
                        when (eventEnum) {
                            NotificationEventsEnum.FIAT_TO_CRYPTO_DEPOSIT,
                            NotificationEventsEnum.CRYPTO_TO_FIAT_WITHDRAWAL -> {
                                runIO {
                                    val transaction = transactionJson?.let {
                                        try {
                                            Gson().fromJson(it, ITransactionHistoryDto::class.java)
                                        } catch (e: JsonSyntaxException) {
                                            Log.e(tag, "Invalid transaction JSON: $it", e)
                                            null
                                        }
                                    }

                                    val notification = notificationJson?.let {
                                        try {
                                            Gson().fromJson(it, NotificationEntity::class.java)
                                        } catch (e: JsonSyntaxException) {
                                            Log.e(tag, "Invalid notification JSON: $it", e)
                                            null
                                        }
                                    }

                                    transaction?.let {
                                        val existing = UserPreferences.getTransactionById(applicationContext, it.transactionId)
                                        if (existing == null) {
                                            userViewModel.addTransaction(it)
                                            Log.d(tag, "Added new transaction for event: ${eventEnum.event}")
                                        } else {
                                            userViewModel.updateTransaction(it)
                                            Log.d(tag, "Updated transaction for event: ${eventEnum.event}")
                                        }
                                    }

                                    notification?.let {
                                        UserPreferences.addNotification(applicationContext, it)
                                        Log.d(tag, "Saved notification for event: ${eventEnum.event}")
                                    }

                                    Log.d(tag, "Processed event: ${eventEnum.event}")
                                }
                            }
                            else -> {
                                Log.w(tag, "Unhandled event: ${eventEnum.event}")
                            }
                        }
                    } else {
                        Log.w(tag, "Unknown event received: $event")
                    }
                } catch (e: Exception) {
                    Log.e(tag, "Failed to parse incoming data: ${e.message}", e)
                }
            } else {
                Log.w(tag, "Ignoring message due to invalid status or missing data: status=$status, event=$event")
            }
        }
    }

    override fun onNewToken(fcmToken: String) {
        super.onNewToken(fcmToken)
        runIO {
            try {
                val userAuthToken = withTimeoutOrNull(5000) {
                    userRepository.getToken().first { !it.isNullOrBlank() }
                }
                if (!userAuthToken.isNullOrBlank()) {
                    sendFcmTokenToBackend(userAuthToken = userAuthToken, fcmToken = fcmToken)
                    Log.d(tag, "FCM token sent to backend")
                } else {
                    Log.w(tag, "User auth token unavailable, cannot send FCM token")
                }
            } catch (e: Exception) {
                Log.e(tag, "Error sending FCM token to backend: ${e.localizedMessage}", e)
            }
        }
    }
}

