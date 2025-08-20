package com.thellex.payments.network.services

import InstantDeserializer
import android.content.Context
import android.util.Log
import com.google.gson.GsonBuilder
import com.thellex.payments.core.utils.AuthUtils
import com.thellex.payments.core.utils.Constants
import com.thellex.payments.core.utils.deserializers.NotificationKindEnumDeserializer
import com.thellex.payments.data.enums.TierEnum
import com.thellex.payments.data.model.NotificationKindEnum
import com.thellex.payments.data.model.PaymentStatusEnum
import com.thellex.payments.data.model.TransactionTypeEnum
import com.thellex.payments.settings.SupportedBlockchainEnum
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.nio.charset.StandardCharsets
import java.util.concurrent.TimeUnit
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

object ApiClient {
    private var appContext: Context? = null

    fun initialize(context: Context) {
        appContext = context.applicationContext
    }

    private fun getCommonInterceptor(token: String = ""): Interceptor {
        return Interceptor { chain ->
            val originalRequest = chain.request()
            val requestBuilder = originalRequest.newBuilder()

            // Authorization header if available
            if (token.isNotBlank()) {
                requestBuilder.addHeader("Authorization", "Bearer $token")
            }

            val context = appContext ?: throw IllegalStateException("ApiClient not initialized with context")

            // Root detection
            if (AuthUtils.isDeviceRooted()) {
                Log.e("ApiClient", "Rooted device detected, aborting request")
                throw SecurityException("Rooted device detected")
            }

            // Timestamp
            val timestamp = (System.currentTimeMillis() / 1000).toString()

            // Request body payload
            val payload = originalRequest.body?.let { body ->
                val buffer = okio.Buffer()
                body.writeTo(buffer)
                buffer.readString(StandardCharsets.UTF_8)
            } ?: "{}"

            // Certificate fingerprint
            val fingerprint = AuthUtils.getCertificateFingerprint(context)
                ?: throw IllegalStateException("Certificate fingerprint not found")

            // HMAC signature
            val signature = AuthUtils.generateRequestSignature(context, payload, timestamp)
                ?: throw IllegalStateException("Failed to generate signature")

            // Add headers
            requestBuilder
                .addHeader("X-Certificate-Fingerprint", fingerprint)
                .addHeader("X-Signature", signature)
                .addHeader("X-Timestamp", timestamp)
                .addHeader("X-Client-Type", "mobile")

            chain.proceed(requestBuilder.build())
        }
    }

    private fun getClient(token: String = ""): OkHttpClient {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.HEADERS
        }

        return OkHttpClient.Builder()
            .connectTimeout(60, TimeUnit.SECONDS)
            .readTimeout(90, TimeUnit.SECONDS)
            .writeTimeout(90, TimeUnit.SECONDS)
            .addInterceptor(getCommonInterceptor(token)) // shared logic
            .addInterceptor(logging)
            .build()
    }

    private val retrofitWithoutToken: Retrofit = Retrofit.Builder()
        .baseUrl(Constants.BASE_URL)
        .client(getClient()) // ✅ now also has fingerprint + headers
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    @OptIn(ExperimentalTime::class)
    private fun getRetrofitWithToken(token: String): Retrofit {
        val enumGson = GsonBuilder()
            .registerTypeAdapter(NotificationKindEnum::class.java, NotificationKindEnumDeserializer())
            .registerTypeAdapter(TierEnum::class.java, TierEnumDeserializer())
            .registerTypeAdapter(SupportedBlockchainEnum::class.java, SupportedBlockchainDeserializer())
            .registerTypeAdapter(PaymentStatusEnum::class.java, PaymentStatusDeserializer())
            .registerTypeAdapter(TransactionTypeEnum::class.java, TransactionTypeDeserializer())
            .registerTypeAdapter(Instant::class.java, InstantDeserializer())
            .create()

        return Retrofit.Builder()
            .baseUrl(Constants.BASE_URL)
            .client(getClient(token))
            .addConverterFactory(GsonConverterFactory.create(enumGson))
            .build()
    }

    // Public APIs (still have fingerprint/signature headers)
    fun getPublicApi(): AuthService = retrofitWithoutToken.create(AuthService::class.java)
    fun getPublicCrashReportApi(): CrashReportService = retrofitWithoutToken.create(CrashReportService::class.java)

    // Authenticated APIs
    fun getAuthenticatedApi(token: String): AuthService =
        getRetrofitWithToken(token).create(AuthService::class.java)

    fun getAuthenticatedPaymentApi(token: String): PaymentRequestService =
        getRetrofitWithToken(token).create(PaymentRequestService::class.java)

    fun getAuthenticatedWalletManagerApi(token: String): WalletManagerService =
        getRetrofitWithToken(token).create(WalletManagerService::class.java)

    fun getAuthenticatedKycApi(token: String): KycService =
        getRetrofitWithToken(token).create(KycService::class.java)

    fun getAuthenticatedNotificationApi(token: String): NotificationService =
        getRetrofitWithToken(token).create(NotificationService::class.java)
}
