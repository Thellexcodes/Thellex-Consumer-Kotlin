package com.thellex.payments.core.utils

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
        Log.d(TAG, "isEmulator: $isEmulator (FINGERPRINT=${Build.FINGERPRINT}, MODEL=${Build.MODEL}, " +
                "MANUFACTURER=${Build.MANUFACTURER}, BRAND=${Build.BRAND}, DEVICE=${Build.DEVICE}, " +
                "PRODUCT=${Build.PRODUCT}, HARDWARE=${Build.HARDWARE}, qemu=${SystemProperties.get("ro.kernel.qemu")})")
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
    private const val API_VERSION = "v1.0.1"
    private const val VERSIONED_BASE = "$API_PREFIX/$API_VERSION"

    const val LOGIN_ENDPOINT = "$VERSIONED_BASE/user/access"
    const val VERIFY_CODE_ENDPOINT = "$VERSIONED_BASE/user/verify"
    const val AUTH_LOGIN_ENDPOINT = "$VERSIONED_BASE/user/authenticate"
    const val SAVE_DEVICE_INFO_ENDPOINT = "$VERSIONED_BASE/devices/save-info"

    // Payment Endpoints
    const val WITHDRAW_CRYPTO_PAYMENT_ENDPOINT = "$VERSIONED_BASE/payments/withdraw-crypto"

    // Wallet Manager Endpoints
    const val WALLET_MANAGER_BALANCE_ENDPOINT = "$VERSIONED_BASE/wallet-manager/balance"

    // KYC and KYB
    const val KYC_ENDPOINT = "$VERSIONED_BASE/kyc/basic-nin-bvn"
    const val KYC_VERIFY_SELFIE_AND_DOCUMENT = "$VERSIONED_BASE/kyc/basic-document-verify-selfie"
    const val KYC_VERIFY_BVN = "$VERSIONED_BASE/kyc/verify-bvn"

    // Notification Endpoints
    const val NOTIFICATION_CONSUME_ENDPOINT = "$VERSIONED_BASE/notifications/{id}/consume"

    // Trades
    const val WALLET_MANAGER_RATES_ENDPOINT = "$VERSIONED_BASE/payments/rates?fiatCode=ngn"
    const val FIAT_TO_CRYPTO_ONRAMP_ENDPOINT = "$VERSIONED_BASE/payments/fiat-to-crypto/onramp"
    const val CRYPTO_TO_FIAT_OFFRAMP_ENDPOINT = "$VERSIONED_BASE/payments/crypto-to-fiat/offramp"

    // Banking
    const val ADD_BANK_ACCOUNT_ENDPOINT = "$VERSIONED_BASE/settings/bank-account/add"

    // Crash endpoint
    const val CRASH_REPORT_ENDPOINT = "$VERSIONED_BASE/crash-report"

    val BASE_URL: String
        get() {
            val isEmulator = DeviceUtils.isEmulator()
            val url = if (isEmulator) {
                "https://goat-touched-mite.ngrok-free.app/" // For Android Emulator
            } else {
                "https://thellex-sandbox-backend.onrender.com/" // Physical device
            }
            Log.d(TAG, "Selected BASE_URL: $url (isEmulator: $isEmulator)")
            return url
        }
}

val reasonList = listOf(
    "Gift", "Bills", "Groceries", "Travel", "Health",
    "Entertainment", "Housing", "School Fees"
)