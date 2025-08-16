package com.thellex.payments.network.services

import PaymentStatusEnumDeserializer
import android.annotation.SuppressLint
import android.util.Log
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonSyntaxException
import com.thellex.payments.core.utils.EventBus
import com.thellex.payments.core.utils.FcmHelper.sendFcmTokenToBackend
import com.thellex.payments.core.utils.Helpers
import com.thellex.payments.core.utils.deserializers.TransactionTypeEnumDeserializer
import com.thellex.payments.data.enums.NotificationEventsEnum
import com.thellex.payments.data.model.ITransactionHistoryDto
import com.thellex.payments.data.model.NotificationEntity
import com.thellex.payments.data.model.PaymentStatusEnum
import com.thellex.payments.data.model.TransactionTypeEnum
import com.thellex.payments.data.model.UserPreferences
import com.thellex.payments.data.model.WalletWebhookEventEnum
import com.thellex.payments.features.auth.viewModel.UserRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull

@SuppressLint("MissingFirebaseInstanceTokenRefresh")
class FCMService : FirebaseMessagingService() {
    private val tag = "FirebaseManager"
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val userRepository by lazy { UserRepository.getInstance(applicationContext) }

    override fun onCreate() {
        super.onCreate()
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
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

        remoteMessage.notification?.body?.let {
            Log.d(tag, "Notification body: $it")
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
                        WalletWebhookEventEnum::fromValue,
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
                                            Gson().fromJson(it, NotificationEntity::class.java)
                                        } catch (e: JsonSyntaxException) {
                                            Log.e(tag, "Invalid notification JSON: $it", e)
                                            null
                                        }
                                    }

                                    Log.d(tag, "Transaction parsed: $transaction")
                                    Log.d(tag, "Notification parsed: $notification")

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
//
                                    notification?.let {
                                        UserPreferences.addNotification(applicationContext, it)
                                        Log.d(tag, "Saved notification for event: ${eventEnum.name}")
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
                    sendFcmTokenToBackend(applicationContext,userAuthToken = userAuthToken, fcmToken = fcmToken)
                    Log.d(tag, "FCM token sent to backend successfully")
                } else {
                    Log.w(tag, "User auth token unavailable, cannot send FCM token")
                }
            } catch (e: Exception) {
                Log.e(tag, "Error sending FCM token to backend: ${e.localizedMessage}", e)
            }
        }
    }
}

