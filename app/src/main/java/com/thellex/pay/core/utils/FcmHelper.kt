package com.thellex.pay.core.utils

import android.content.Context
import android.os.Build
import android.util.Log
import com.thellex.pay.core.utils.Helpers.deviceId
import com.thellex.pay.data.model.DeviceRequestDto
import com.thellex.pay.network.services.ApiClient
import com.thellex.pay.network.services.AuthService

object FcmHelper {
    private const val TAG = "FcmHelper"

    suspend fun sendFcmTokenToBackend(context: Context, userAuthToken: String, fcmToken: String) {
        if (userAuthToken.isBlank() || fcmToken.isBlank()) {
            Log.w(TAG, "Missing user auth token or FCM token.")
            return
        }

        try {
            Log.d(TAG, "Sending FCM token to backend: $fcmToken")
            val api: AuthService = ApiClient.getAuthenticatedApi(context, userAuthToken)
            val response = api.updateFcmToken(
                DeviceRequestDto(
                    fcmToken = fcmToken,
                    platform = "Android",
                    deviceModel = "${Build.MANUFACTURER} ${Build.MODEL}",
                    osVersion = Build.VERSION.RELEASE,
                    deviceId = deviceId(context)
                )
            )

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
