package com.thellex.pay.features.auth.repository

import android.content.Context
import android.util.Log
import com.thellex.pay.data.model.ApiResponse
import com.thellex.pay.data.model.ChallengeResponse
import com.thellex.pay.data.model.VerifyAuthenticationRequest
import com.thellex.pay.network.services.ApiClient
import retrofit2.Response

class BiometricRepository(
    private val context: Context,
) {

    suspend fun createChallenge(): ApiResponse<ChallengeResponse>  {
        val prefs = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
//        val authToken = prefs.getString("token", null)
        val authToken = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpZCI6IjJkOGUwOThlLWUxOGItNDExYS1iOWM3LTcxNTY4YTc4ZGFjOCIsImlhdCI6MTc2MjIxMTc4MywiZXhwIjoxNzY0ODAzNzgzfQ.g1UaUgaz7-e5NDgoqAkjBC9mf_fYAIeOYugx1QyzbYw"
        Log.e("RegisterPasskey", "token is = $authToken")
        val response = ApiClient.getAuthenticatedApi(context, authToken).createChallenge()
        return response
    }

    suspend fun getAuthOptions(): Response<ApiResponse<Unit>> {
        val prefs = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        val authToken = prefs.getString("token", null)
        val response =  ApiClient.getAuthenticatedApi(context, authToken!!).getAuthOptions()
        Log.d("Response", "$response")

        return response
    }

    suspend fun verifyAuth(request: VerifyAuthenticationRequest) {
//        return ApiClient.instance.verifyAuth(request)
    }

    companion object {
        private val TAG = "BiometricRepository"
    }
}