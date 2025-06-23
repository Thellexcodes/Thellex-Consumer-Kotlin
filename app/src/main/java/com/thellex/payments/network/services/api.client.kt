package com.thellex.payments.network.services

import com.thellex.payments.core.utils.Constants
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object ApiClient {

    private val retrofitWithoutToken: Retrofit = Retrofit.Builder()
        .baseUrl(Constants.BASE_URL)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    private fun getClientWithToken(token: String): OkHttpClient {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.HEADERS
        }

        return OkHttpClient.Builder()
            .addInterceptor { chain ->
                val originalRequest = chain.request()
                val requestBuilder = originalRequest.newBuilder()

                if (token.isNotBlank()) {
                    requestBuilder.addHeader("Authorization", "Bearer $token")
                }

                chain.proceed(requestBuilder.build())
            }
            .addInterceptor(logging)
            .build()
    }

    private fun getRetrofitWithToken(token: String): Retrofit {
        return Retrofit.Builder()
            .baseUrl(Constants.BASE_URL)
            .client(getClientWithToken(token))
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    // Public API without authentication
    fun getPublicApi(): AuthService = retrofitWithoutToken.create(AuthService::class.java)

    // Authenticated API for ApiService
    fun getAuthenticatedApi(token: String): AuthService = getRetrofitWithToken(token).create(
        AuthService::class.java)

    // Authenticated API for PaymentService
    fun getAuthenticatedPaymentApi(token: String): PaymentRequestService = getRetrofitWithToken(token).create(
        PaymentRequestService::class.java)

    // Authenticated API for WalletManagerService
    fun getAuthenticatedWalletManagerApi(token: String): WalletManagerService = getRetrofitWithToken(token).create(
        WalletManagerService::class.java)
}
