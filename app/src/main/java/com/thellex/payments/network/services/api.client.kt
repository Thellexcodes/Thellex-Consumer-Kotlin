package com.thellex.payments.network.services

import InstantDeserializer
import com.google.gson.GsonBuilder
import com.thellex.payments.core.utils.Constants
import com.thellex.payments.data.enums.TierEnum
import com.thellex.payments.settings.SupportedBlockchainEnum
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

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

    @OptIn(ExperimentalTime::class)
    private fun getRetrofitWithToken(token: String): Retrofit {
        val enumGson = GsonBuilder()
            .registerTypeAdapter(TierEnum::class.java, TierEnumDeserializer())
            .registerTypeAdapter(SupportedBlockchainEnum::class.java, SupportedBlockchainDeserializer())
            .registerTypeAdapter(Instant::class.java, InstantDeserializer())
            .create()

        return Retrofit.Builder()
            .baseUrl(Constants.BASE_URL)
            .client(getClientWithToken(token))
            .addConverterFactory(GsonConverterFactory.create(enumGson))
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

    // Authenticated API for KycService
    fun getAuthenticatedKycApi(token: String): KycService = getRetrofitWithToken(token).create(KycService::class.java)
}
