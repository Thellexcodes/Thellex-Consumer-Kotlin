package com.thellex.payments.core.utils

import android.util.Log
import com.thellex.payments.data.model.FcmTokenDto
import com.thellex.payments.network.services.ApiClient
import com.thellex.payments.network.services.AuthService

object FcmHelper {
    private const val TAG = "TAGY"

    suspend fun sendFcmTokenToBackend(userAuthToken: String, fcmToken: String) {
        if (userAuthToken.isBlank() || fcmToken.isBlank()) {
            Log.w(TAG, "Missing user auth token or FCM token.")
            return
        }

        try {
            Log.d(TAG, "Sending FCM token to backend: $fcmToken")

            val api: AuthService = ApiClient.getAuthenticatedApi(userAuthToken)
            val response = api.updateFcmToken(FcmTokenDto(token = fcmToken))

            if (response.isSuccessful) {
                Log.d(TAG, "FCM token sent successfully.")
            } else {
                Log.e(TAG, "Failed to send FCM token: ${response.errorBody()?.string()}")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Exception sending FCM token: ${e.localizedMessage}")
        }
    }
}