package com.thellex.payments.network.services

import InstantDeserializer
import android.content.Context
import android.util.Log
import com.google.firebase.crashlytics.internal.model.CrashlyticsReport.Session.User
import com.google.gson.GsonBuilder
import com.thellex.payments.core.utils.AuthUtils
import com.thellex.payments.core.utils.Constants
import com.thellex.payments.core.utils.deserializers.NotificationKindEnumDeserializer
import com.thellex.payments.data.enums.RoleEnum
import com.thellex.payments.data.enums.RoleTypeDeserializer
import com.thellex.payments.data.enums.TierEnum
import com.thellex.payments.data.model.NotificationKindEnum
import com.thellex.payments.data.model.PaymentStatusEnum
import com.thellex.payments.data.model.TransactionTypeEnum
import com.thellex.payments.settings.SupportedBlockchainEnum
import okhttp3.Cache
import okhttp3.Dispatcher
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.IOException
import java.nio.Buffer
import java.util.concurrent.TimeUnit
import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

object ApiClient {
    private const val TAG = "ApiClient"

    // --- OkHttpClient factory ---
    @OptIn(ExperimentalTime::class)
    private fun buildClient(context: Context, token: String = "", enableLogging: Boolean = true): OkHttpClient {
        val dispatcher = Dispatcher().apply {
            maxRequests = 64
            maxRequestsPerHost = 16
        }

//        val builder = OkHttpClient.Builder()
//            .connectTimeout(30, TimeUnit.SECONDS)
//            .readTimeout(30, TimeUnit.SECONDS)
//            .writeTimeout(30, TimeUnit.SECONDS)
//            .retryOnConnectionFailure(true)
//            .dispatcher(dispatcher)
//            .addInterceptor { chain ->
//                val original = chain.request()
//                val requestBuilder = original.newBuilder()
//
//                if (token.isNotBlank()) {
//                    requestBuilder.addHeader("Authorization", "Bearer $token")
//                }
//
//                try {
//                    chain.proceed(requestBuilder.build())
//                } catch (e: Exception) {
//                    Log.e(TAG, "Network request failed: ${e.localizedMessage}", e)
//                    throw e
//                }
//            }
        val builder = OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .retryOnConnectionFailure(true)
            .dispatcher(dispatcher)
            .addInterceptor { chain ->
                val original = chain.request()
                val requestBody = original.body
                val payload = requestBody?.let { body ->
                    val buffer = okio.Buffer()
                    body.writeTo(buffer)
                    buffer.readUtf8()
                } ?: "{}"

                val timestamp = Clock.System.now().toString()
//                val fingerprint = AuthUtils.getCertificateFingerprint(context)
//                    ?: throw IOException("Certificate fingerprint unavailable")
                val signature = AuthUtils.generateRequestSignature(context, payload, timestamp)
                    ?: throw IOException("Request signature generation failed")

                val requestBuilder = original.newBuilder()
                    .header("x-signature", signature)
                    .header("x-timestamp", timestamp)
                    .header("x-certificate-fingerprint", "")
                    .header("x-client-type", "mobile")
                    .apply {
                        if (token.isNotBlank()) {
                            addHeader("Authorization", "Bearer $token")
                        }
                    }

                try {
                    chain.proceed(requestBuilder.build())
                } catch (e: Exception) {
                    Log.e(TAG, "Network request failed: ${e.localizedMessage}", e)
                    throw e
                }
            }

        if (enableLogging) {
            val logging = HttpLoggingInterceptor { message ->
                Log.d(TAG, message)
            }.apply {
                level = HttpLoggingInterceptor.Level.BODY
            }
            builder.addInterceptor(logging)
        }

        return builder.build()
    }

    // --- Retrofit caches ---
    // Modified to accept Context
    fun getRetrofitWithoutToken(context: Context): Retrofit = lazy {
        Retrofit.Builder()
            .baseUrl(Constants.BASE_URL)
            .client(buildClient(context = context, enableLogging = true))
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }.value

    private val retrofitWithTokenCache = mutableMapOf<String, Retrofit>()

    @OptIn(ExperimentalTime::class)
    private fun getRetrofitWithToken(context: Context, token: String): Retrofit {
        return retrofitWithTokenCache.getOrPut(token) {
            val enumGson = GsonBuilder()
                .registerTypeAdapter(NotificationKindEnum::class.java, NotificationKindEnumDeserializer())
                .registerTypeAdapter(TierEnum::class.java, TierEnumDeserializer())
                .registerTypeAdapter(SupportedBlockchainEnum::class.java, SupportedBlockchainDeserializer())
                .registerTypeAdapter(PaymentStatusEnum::class.java, PaymentStatusDeserializer())
                .registerTypeAdapter(TransactionTypeEnum::class.java, TransactionTypeDeserializer())
                .registerTypeAdapter(RoleEnum::class.java, RoleTypeDeserializer())
                .registerTypeAdapter(Instant::class.java, InstantDeserializer())
                .create()

            Retrofit.Builder()
                .baseUrl(Constants.BASE_URL)
                .client(buildClient(context, token, enableLogging = true))
                .addConverterFactory(GsonConverterFactory.create(enumGson))
                .build()
        }
    }

    // --- Public APIs ---
    fun getPublicApi(context: Context): AuthService =
        getRetrofitWithoutToken(context).create(AuthService::class.java)

    fun getPublicCrashReportApi(context: Context): CrashReportService =
        getRetrofitWithoutToken(context).create(CrashReportService::class.java)

    fun getPublicErrorReportApi(context: Context): ErrorService =
        getRetrofitWithoutToken(context).create(ErrorService::class.java)

    // --- Authenticated APIs ---
    fun getAuthenticatedApi(context: Context, token: String): AuthService =
        getRetrofitWithToken(context, token).create(AuthService::class.java)

    fun getAuthenticatedPaymentApi(context: Context, token: String): PaymentRequestService =
        getRetrofitWithToken(context, token).create(PaymentRequestService::class.java)

    fun getAuthenticatedWalletManagerApi(context: Context, token: String): WalletManagerService =
        getRetrofitWithToken(context, token).create(WalletManagerService::class.java)

    fun getAuthenticatedKycApi(context: Context, token: String): KycService =
        getRetrofitWithToken(context, token).create(KycService::class.java)

    fun getAuthenticatedNotificationApi(context: Context, token: String): NotificationService =
        getRetrofitWithToken(context, token).create(NotificationService::class.java)

    fun getAuthenticatedAdminApi(context: Context, token: String): AdminService =
        getRetrofitWithToken(context, token).create(AdminService::class.java)

    fun getAuthenticatedUserApi(context: Context, token: String): UserService =
        getRetrofitWithToken(context, token).create(UserService::class.java)

    fun getAppApi(context: Context, token: String): AppService =
        getRetrofitWithToken(context, token).create(AppService::class.java)
}

//object ApiClient {
//
//    // --- OkHttpClient factory ---
//    private fun buildClient(token: String = "", enableLogging: Boolean = true): OkHttpClient {
//        val dispatcher = Dispatcher().apply {
//            maxRequests = 64
//            maxRequestsPerHost = 16
//        }
//
//        val builder = OkHttpClient.Builder()
//            .connectTimeout(30, TimeUnit.SECONDS)
//            .readTimeout(30, TimeUnit.SECONDS)
//            .writeTimeout(30, TimeUnit.SECONDS)
//            .retryOnConnectionFailure(true)
//            .dispatcher(dispatcher)
//            .addInterceptor { chain ->
//                val original = chain.request()
//                val requestBuilder = original.newBuilder()
//
//                if (token.isNotBlank()) {
//                    requestBuilder.addHeader("Authorization", "Bearer $token")
//                }
//
//                try {
//                    chain.proceed(requestBuilder.build())
//                } catch (e: Exception) {
//                    Log.e("ApiClient", "Network request failed: ${e.localizedMessage}", e)
//                    throw e
//                }
//            }
//
//        if (enableLogging) {
//            val logging = HttpLoggingInterceptor { message ->
//                Log.d("ApiClient", message)
//            }.apply {
//                level = HttpLoggingInterceptor.Level.BODY
//            }
//            builder.addInterceptor(logging)
//        }
//
//        return builder.build()
//    }
//
//    // --- Retrofit caches ---
//    private val retrofitWithoutToken: Retrofit by lazy {
//        Retrofit.Builder()
//            .baseUrl(Constants.BASE_URL)
//            .client(buildClient(enableLogging = true))
//            .addConverterFactory(GsonConverterFactory.create())
//            .build()
//    }
//
//    private val retrofitWithTokenCache = mutableMapOf<String, Retrofit>()
//
//    @OptIn(ExperimentalTime::class)
//    private fun getRetrofitWithToken(token: String): Retrofit {
//        return retrofitWithTokenCache.getOrPut(token) {
//            val enumGson = GsonBuilder()
//                .registerTypeAdapter(NotificationKindEnum::class.java, NotificationKindEnumDeserializer())
//                .registerTypeAdapter(TierEnum::class.java, TierEnumDeserializer())
//                .registerTypeAdapter(SupportedBlockchainEnum::class.java, SupportedBlockchainDeserializer())
//                .registerTypeAdapter(PaymentStatusEnum::class.java, PaymentStatusDeserializer())
//                .registerTypeAdapter(TransactionTypeEnum::class.java, TransactionTypeDeserializer())
//                .registerTypeAdapter(RoleEnum::class.java, RoleTypeDeserializer())
//                .registerTypeAdapter(Instant::class.java, InstantDeserializer())
//                .create()
//
//            Retrofit.Builder()
//                .baseUrl(Constants.BASE_URL)
//                .client(buildClient(token, enableLogging = true))
//                .addConverterFactory(GsonConverterFactory.create(enumGson))
//                .build()
//        }
//    }
//
//    // --- Public APIs ---
//    fun getPublicApi(): AuthService =
//        retrofitWithoutToken.create(AuthService::class.java)
//
//    fun getPublicCrashReportApi(): CrashReportService =
//        retrofitWithoutToken.create(CrashReportService::class.java)
//
//    fun getPublicErrorReportApi(): ErrorService =
//        retrofitWithoutToken.create(ErrorService::class.java)
//
//    // --- Authenticated APIs ---
//    fun getAuthenticatedApi(token: String): AuthService =
//        getRetrofitWithToken(token).create(AuthService::class.java)
//
//    fun getAuthenticatedPaymentApi(token: String): PaymentRequestService =
//        getRetrofitWithToken(token).create(PaymentRequestService::class.java)
//
//    fun getAuthenticatedWalletManagerApi(token: String): WalletManagerService =
//        getRetrofitWithToken(token).create(WalletManagerService::class.java)
//
//    fun getAuthenticatedKycApi(token: String): KycService =
//        getRetrofitWithToken(token).create(KycService::class.java)
//
//    fun getAuthenticatedNotificationApi(token: String): NotificationService =
//        getRetrofitWithToken(token).create(NotificationService::class.java)
//
//    fun getAuthenticatedAdminApi(token: String): AdminService =
//        getRetrofitWithToken(token).create(AdminService::class.java)
//
//    fun getAuthenticatedUserApi(token: String): UserService =
//        getRetrofitWithToken(token).create(UserService::class.java)
//
//    fun getAppApi(token: String): AppService = getRetrofitWithToken(token).create(AppService::class.java)
//}

