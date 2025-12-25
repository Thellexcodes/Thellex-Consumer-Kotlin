package com.thellex.pay.core.utils

import android.os.Build
import android.util.Log

object DeviceUtils {
    private const val TAG = "DeviceUtils"

    fun isEmulator(): Boolean {
        val isEmulator = (Build.FINGERPRINT.contains("generic", ignoreCase = true) ||
                Build.FINGERPRINT.contains("emulator", ignoreCase = true) ||
                Build.FINGERPRINT.contains("unknown", ignoreCase = true) ||
                Build.MODEL.contains("Emulator", ignoreCase = true) ||
                Build.MODEL.contains("Android SDK built for", ignoreCase = true) ||
                Build.MANUFACTURER.contains("Genymotion", ignoreCase = true) ||
                Build.BRAND.contains("generic", ignoreCase = true) ||
                Build.DEVICE.contains("generic", ignoreCase = true) ||
                Build.PRODUCT.contains("sdk", ignoreCase = true) ||
                Build.HARDWARE.contains("goldfish", ignoreCase = true) ||
                Build.HARDWARE.contains("ranchu", ignoreCase = true) ||
                Build.HARDWARE.contains("vbox86", ignoreCase = true) ||
                SystemProperties.get("ro.kernel.qemu") == "1" ||
                SystemProperties.get("ro.product.device") == "emulator")
//        Log.d(TAG, "isEmulator: $isEmulator (FINGERPRINT=${Build.FINGERPRINT}, MODEL=${Build.MODEL}, " +
//                "MANUFACTURER=${Build.MANUFACTURER}, BRAND=${Build.BRAND}, DEVICE=${Build.DEVICE}, " +
//                "PRODUCT=${Build.PRODUCT}, HARDWARE=${Build.HARDWARE}, qemu=${SystemProperties.get("ro.kernel.qemu")})")
        return isEmulator
    }

    // Helper to access SystemProperties (requires reflection for private APIs)
    private object SystemProperties {
        fun get(key: String): String? {
            return try {
                val clazz = Class.forName("android.os.SystemProperties")
                val method = clazz.getMethod("get", String::class.java)
                method.invoke(null, key) as? String
            } catch (e: Exception) {
                Log.e(TAG, "Failed to access SystemProperties for key: $key", e)
                null
            }
        }
    }
}

object Constants {
    private const val TAG = "Constants"
    private const val API_PREFIX = "api"

    // ---- Versions ----
    object Versions {
        data class VersionInfo(
            val version: String,
            val minSupported: String,
            val latest: String
        )

        val versionMap = mapOf(
            "V1_0_1" to VersionInfo(
                version = "v1.0.1",
                minSupported = "v1.0.0",
                latest = "v1.0.2"
            ),
            "V1_0_2" to VersionInfo(
                version = "v1.0.2",
                minSupported = "v1.0.1",
                latest = "v1.1.0"
            ),
            "V2_0_0" to VersionInfo(
                version = "v2.0.0",
                minSupported = "v2.0.0",
                latest = "v2.0.1"
            )
        )

        fun getVersionInfo(versionKey: String): VersionInfo {
            return versionMap[versionKey]
                ?: throw IllegalArgumentException("Invalid version: $versionKey")
        }
    }

    // ---- Base URL Builder ----
    private fun buildBaseUrl(version: String): String {
        require(version.isNotBlank()) { "Version cannot be blank" }
        return "$API_PREFIX/$version"
    }

    // ---- Endpoints ----
    object Endpoints {
        private fun versionedEndpoint(path: String, versionKey: String): String {
            val versionInfo = Versions.getVersionInfo(versionKey)
            return "${buildBaseUrl(versionInfo.version)}/$path"
        }

        // User
        const val LOGIN = "api/v1.0.1/user/access"
        const val VERIFY_CODE = "api/v2.0.0/user/verify"
        const val AUTH_LOGIN = "api/v1.0.1/user/authenticate"
        const val SAVE_DEVICE_INFO = "api/v1.0.1/devices/save-info"
        const val SET_PIN = "api/v2.0.0/auth/set-pin"
        const val VERIFY_PIN = "api/v2.0.0/auth/verify-pin"

        // Payments
        const val WITHDRAW_CRYPTO = "api/v2.0.0/payments/withdraw-crypto"

        // Wallet Manager
        const val WALLET_BALANCE = "api/v2.0.0/wallet-manager/balance"

        // KYC and KYB
        const val KYC = "api/v1.0.1/kyc/basic-nin-bvn"
        const val KYC_VERIFY_SELFIE_AND_DOC = "api/v1.0.1/kyc/basic-document-verify-selfie"
        const val KYC_VERIFY_BVN = "api/v1.0.1/kyc/verify-bvn"

        // Notifications
        fun notificationConsume(id: String): String {
            require(id.isNotBlank()) { "Notification ID cannot be blank" }
            return "api/v1.0.1/notifications/$id/consume"
        }

        // Trades
        const val RATES = "api/v1.0.1/payments/rates?fiatCode=ngn"
        const val FIAT_TO_CRYPTO_ONRAMP = "api/v1.0.1/payments/fiat-to-crypto/onramp"
        const val CRYPTO_TO_FIAT_OFFRAMP = "api/v1.0.1/payments/crypto-to-fiat/offramp"

        // Banking
        const val ADD_BANK_ACCOUNT = "api/v1.0.1/settings/bank-account/add"

        // Crash Reports
        const val CRASH_REPORT = "api/v1.0.1/crash-report"
        const val ERROR_REPORT = "api/v1.0.1/error-report"

        // Admin
        const val ADMIN_REVENUES = "api/v1.0.1/admin/revenues"
        const val ADMIN_RAMP_TRANSACTIONS = "api/v1.0.1/admin/ramp_transactions"
        const val ADMIN_APPROVE_RAMP = "api/v1.0.1/admin/approve_ramp_transactions"

        // User
        const val USER_TRANSACTIONS = "api/v1.0.1/user/transactions"
        const val USER_RAMP_TRANSACTIONS = "api/v1.0.1/user/ramp_transactions"
        const val USER_NOTIFICATIONS = "api/v1.0.1/user/notifications"

        // App (not versioned)
        const val CHECK_APP_VERSION = "$API_PREFIX/v1.0.1/app/check-version"
        const val GET_BASE_APP_SETTINGS = "$API_PREFIX/v2.0.0/settings/base"

        // Version mapping (for reference or dynamic use)
        private val endpointVersions = mapOf(
            "user/access" to "V1_0_1",
            "user/verify" to "V1_0_1",
            "user/authenticate" to "V1_0_1",
            "devices/save-info" to "V1_0_1",
            "payments/withdraw-crypto" to "V1_0_1",
            "wallet-manager/balance" to "V1_0_1",
            "kyc/basic-nin-bvn" to "V1_0_1",
            "kyc/basic-document-verify-selfie" to "V1_0_1",
            "kyc/verify-bvn" to "V1_0_1",
            notificationConsume("") to "V1_0_1",
            "payments/rates?fiatCode=ngn" to "V1_0_1",
            "payments/fiat-to-crypto/onramp" to "V1_0_1",
            "payments/crypto-to-fiat/offramp" to "V1_0_1",
            "settings/bank-account/add" to "V1_0_1",
            "crash-report" to "V1_0_1",
            "error-report" to "V1_0_1",
            "admin/revenues" to "V1_0_1",
            "admin/ramp_transactions" to "V1_0_1",
            "admin/approve_ramp_transactions" to "V1_0_1",
            "user/transactions" to "V1_0_1",
            "user/ramp_transactions" to "V1_0_1",
            "user/notifications" to "V1_0_1"
        )

        fun getFullEndpoint(endpoint: String): String {
            return when {
                endpoint == CHECK_APP_VERSION -> CHECK_APP_VERSION
                endpoint.startsWith("notifications/") && endpoint.endsWith("/consume") -> {
                    versionedEndpoint(endpoint, endpointVersions[notificationConsume("")]!!)
                }
                endpointVersions.containsKey(endpoint) -> {
                    versionedEndpoint(endpoint, endpointVersions[endpoint]!!)
                }
                else -> throw IllegalArgumentException("Unknown endpoint: $endpoint")
            }
        }
    }

    val BASE_URL: String by lazy {
        val isEmulator = DeviceUtils.isEmulator()
        if (isEmulator) {
            "https://goat-touched-mite.ngrok-free.app/"
        } else {
            "https://thellex-sandbox-backend.onrender.com/"
        }
    }

    fun getCompleteUrl(endpoint: String): String {
        return "$BASE_URL$endpoint"
    }

    fun isVersionSupported(version: String): Boolean {
        return Versions.versionMap.values.any { it.minSupported <= version && version <= it.latest }
    }
}