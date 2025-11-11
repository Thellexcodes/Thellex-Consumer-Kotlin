package com.thellex.pay.features.auth.repository

import android.content.Context
import android.util.Log
import com.thellex.pay.data.model.ApiResponse
import com.thellex.pay.data.model.PinRequest
import com.thellex.pay.network.services.ApiClient

class SecurityRepository(
    private val context: Context,
    ) {

    suspend fun fetchRemoteSecurityStatus(pin: String, authToken: String): ApiResponse<Boolean> {
        val response = ApiClient
            .getAuthenticatedApi(context, authToken)
            .verifySecurityPin(pin)
        return response
    }

    suspend fun updateRemoteSecurityPin(pin: String, authToken: String): ApiResponse<Boolean> {
        if (authToken.isEmpty()) {
            Log.e("SecurityRepo", "Auth token is missing!")
        }

        val response = ApiClient
            .getAuthenticatedApi(context, authToken)
            .updateSecurityPin(PinRequest(pin = pin))

        Log.d("SecurityRepo", "Backend response: $response")
        return response
    }

    companion object {
        private val SecurityRepository = "SecurityRepository"
    }
}
