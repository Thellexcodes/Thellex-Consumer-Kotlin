package com.thellex.pos.services

import com.thellex.pos.data.model.Constants
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

//object ApiClient {
//    private val retrofitWithoutToken = Retrofit.Builder()
//        .baseUrl(Constants.BASE_URL)
//        .addConverterFactory(GsonConverterFactory.create())
//        .build()
//
//    fun getPublicApi(): ApiService = retrofitWithoutToken.create(ApiService::class.java)
//
//    fun getAuthenticatedApi(token: String): ApiService {
//
//        val logging = HttpLoggingInterceptor().apply {
//            level = HttpLoggingInterceptor.Level.HEADERS
//        }
//
//        val client = OkHttpClient.Builder()
//            .addInterceptor { chain ->
//                val originalRequest = chain.request()
//                val requestBuilder = originalRequest.newBuilder()
//
//                if (token.isNotBlank()) {
//                    requestBuilder.addHeader("Authorization", "Bearer $token")
//                }
//
//                val newRequest = requestBuilder.build()
//                chain.proceed(newRequest)
//            }
//            .addInterceptor(logging)
//            .build()
//
//        val retrofitWithToken = Retrofit.Builder()
//            .baseUrl(Constants.BASE_URL)
//            .client(client)
//            .addConverterFactory(GsonConverterFactory.create())
//            .build()
//
//        return retrofitWithToken.create(ApiService::class.java)
//    }
//}

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
    fun getAuthenticatedApi(token: String): AuthService = getRetrofitWithToken(token).create(AuthService::class.java)

    // Authenticated API for PaymentService
    fun getAuthenticatedPaymentApi(token: String): PaymentRequestService = getRetrofitWithToken(token).create(PaymentRequestService::class.java)

    // You can also add getPublicPaymentApi() if you have public payment endpoints
}
