package com.thellex.payments.network.services
import android.annotation.SuppressLint
import android.util.Log
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.thellex.payments.core.utils.FcmHelper
import com.thellex.payments.core.utils.FcmHelper.sendFcmTokenToBackend
import com.thellex.payments.core.utils.Helpers.showSystemNotification
import com.thellex.payments.features.auth.viewModel.UserRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch


@SuppressLint("MissingFirebaseInstanceTokenRefresh")
class MyFirebaseMessagingService : FirebaseMessagingService() {
    private val tag = "TAGY"
    private val userRepository by lazy { UserRepository.getInstance(applicationContext) }

    override fun onCreate() {
        super.onCreate()
        Log.d(tag, "Firebase Messaging Service Created")
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        Log.d(tag, "From: ${remoteMessage.from}")

//        showSystemNotification(
//            this@MyFirebaseMessagingService,
//            "Withdraw Complete",
//            "You've successfully withdrawn."
//        )

        remoteMessage.notification?.let {
            Log.d(tag, "Notification Body: ${it.body}")
        }

        if (remoteMessage.data.isNotEmpty()) {
            Log.d(tag, "Data Payload: ${remoteMessage.data}")
        }
    }

    override fun onNewToken(fcmToken: String) {
        super.onNewToken(fcmToken)
        Log.d(tag, "Refreshed token: $fcmToken")

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val userAuthToken = userRepository.getToken()
                    .first { !it.isNullOrBlank() }

                Log.d(tag, "AuthToken is $userAuthToken")
                sendFcmTokenToBackend(userAuthToken = userAuthToken!!, fcmToken = fcmToken)

            } catch (e: Exception) {
                Log.e(tag, "Failed to send refreshed token: ${e.localizedMessage}")
            }
        }
    }
}
