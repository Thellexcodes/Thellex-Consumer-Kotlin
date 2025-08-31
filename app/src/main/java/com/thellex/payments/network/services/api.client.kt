package com.thellex.payments.network.services

import InstantDeserializer
import android.content.Context
import com.google.firebase.crashlytics.internal.model.CrashlyticsReport.Session.User
import com.google.gson.GsonBuilder
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
import java.util.concurrent.TimeUnit
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

object ApiClient {
    private var appContext: Context? = null

    fun initialize(context: Context) {
        appContext = context.applicationContext
    }

    // --- OkHttpClient factory ---
    private fun buildClient(token: String = "", enableLogging: Boolean = true): OkHttpClient {
        val dispatcher = Dispatcher().apply {
            maxRequests = 64
            maxRequestsPerHost = 16
        }

        val cacheSize = (10 * 1024 * 1024).toLong() // 10 MB
        val cache = Cache(appContext!!.cacheDir, cacheSize)

        val builder = OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS) // allow slower backend responses
            .writeTimeout(15, TimeUnit.SECONDS)
            .retryOnConnectionFailure(true) // auto retry transient failures
            .dispatcher(dispatcher)
            .cache(cache)
            .addInterceptor { chain ->
                val original = chain.request()
                val requestBuilder = original.newBuilder()

                if (token.isNotBlank()) {
                    requestBuilder.addHeader("Authorization", "Bearer $token")
                }

                chain.proceed(requestBuilder.build())
            }

        if (enableLogging) {
            val logging = HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BASIC
            }
            builder.addInterceptor(logging)
        }

        return builder.build()
    }

    // --- Retrofit caches ---
    private val retrofitWithoutToken: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(Constants.BASE_URL)
            .client(buildClient(enableLogging = true))
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    private val retrofitWithTokenCache = mutableMapOf<String, Retrofit>()

    @OptIn(ExperimentalTime::class)
    private fun getRetrofitWithToken(token: String): Retrofit {
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
                .client(buildClient(token, enableLogging = true))
                .addConverterFactory(GsonConverterFactory.create(enumGson))
                .build()
        }
    }

    // --- Public APIs ---
    fun getPublicApi(): AuthService =
        retrofitWithoutToken.create(AuthService::class.java)

    fun getPublicCrashReportApi(): CrashReportService =
        retrofitWithoutToken.create(CrashReportService::class.java)

    fun getPublicErrorReportApi(): ErrorService =
        retrofitWithoutToken.create(ErrorService::class.java)

    // --- Authenticated APIs ---
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

    fun getAuthenticatedAdminApi(token: String): AdminService =
        getRetrofitWithToken(token).create(AdminService::class.java)

    fun getAuthenticatedUserApi(token: String): UserService =
        getRetrofitWithToken(token).create(UserService::class.java)
}
