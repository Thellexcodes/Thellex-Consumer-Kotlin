package com.thellex.payments.network.services
import android.annotation.SuppressLint
import android.util.Log
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.google.gson.Gson
import com.thellex.payments.core.utils.FcmHelper.sendFcmTokenToBackend
import com.thellex.payments.data.enums.NotificationEventsEnum
import com.thellex.payments.data.model.ITransactionHistoryEntity
import com.thellex.payments.data.model.UserPreferences
import com.thellex.payments.features.auth.viewModel.UserRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

@SuppressLint("MissingFirebaseInstanceTokenRefresh")
class FirebaseManager : FirebaseMessagingService() {
    private val tag = "TAG"
    private val userRepository by lazy { UserRepository.getInstance(applicationContext) }

    override fun onCreate() {
        super.onCreate()
    }

    // General helper to run coroutine tasks on IO dispatcher
    private fun runIO(block: suspend () -> Unit) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                block()
            } catch (e: Exception) {
                Log.e(tag, "Coroutine error: ${e.localizedMessage}", e)
            }
        }
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        remoteMessage.notification?.let {
            Log.d(tag, "Notification Body: ${it.body}")
        }

        if (remoteMessage.data.isNotEmpty()) {
            Log.d(tag, "Data Payload: ${remoteMessage.data}")

            val status = remoteMessage.data["status"]
            val event = remoteMessage.data["event"]
            val transactionJson = remoteMessage.data["transaction"]

            if (status == "success" && event != null && transactionJson != null) {
                try {
                    val eventEnum = NotificationEventsEnum.fromValue(event)
                    if (eventEnum != null) {
                        when (eventEnum) {
                            NotificationEventsEnum.COLLECTION_CREATED -> {
                                val transaction = Gson().fromJson(
                                    transactionJson,
                                    ITransactionHistoryEntity::class.java
                                )
                                val appContext = applicationContext

                                runIO {
                                    UserPreferences.addTransactionHistory(
                                        appContext,
                                        transaction
                                    )
                                }

                                Log.d(tag, "Transaction added for event: ${eventEnum.event}")
                            }
                            else -> {
                                Log.w(tag, "Unhandled event: $event")
                            }
                        }
                    } else {
                        Log.w(tag, "Unknown event received: $event")
                    }
                } catch (e: Exception) {
                    Log.e(tag, "Failed to parse transaction: ${e.message}", e)
                }
            } else {
                Log.w(tag, "Ignoring message due to invalid status or missing data: status=$status, event=$event")
            }
        }
    }

    override fun onNewToken(fcmToken: String) {
        super.onNewToken(fcmToken)
        runIO {
            val userAuthToken = userRepository.getToken()
                .first { !it.isNullOrBlank() }

            sendFcmTokenToBackend(userAuthToken = userAuthToken!!, fcmToken = fcmToken)
        }
    }
}
