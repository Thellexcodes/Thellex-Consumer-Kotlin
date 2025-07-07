package com.thellex.payments.core.utils
object Constants {
    private const val API_VERSION = "v1.0.0"
    private const val API_PREFIX = "api"
    private const val VERSIONED_BASE = "$API_PREFIX/$API_VERSION"

    const val LOGIN_ENDPOINT = "$VERSIONED_BASE/user/access"
    const val VERIFY_CODE_ENDPOINT = "$VERSIONED_BASE/user/verify"
    const val AUTH_LOGIN_ENDPOINT = "$VERSIONED_BASE/user/authenticate"

    // Payment Endpoints
    const val WITHDRAW_CRYPTO_PAYMENT_ENDPOINT = "$VERSIONED_BASE/payments/withdraw-crypto"

    // Wallet Manager Endpoints
    const val WALLET_MANAGER_BALANCE_ENDPOINT = "$VERSIONED_BASE/wallet-manager/balance"

    // KYC and KYB
    const val KYC_ENDPOINT = "$VERSIONED_BASE/kyc/basic-nin-bvn"
    const val KYC_VERIFY_SELFIE_AND_DOCUMENT = "$VERSIONED_BASE/kyc/basic-document-verify-selfie"

    //Notification Endpoints
    const val NOTIFICATION_CONSUME_ENDPOINT = "$VERSIONED_BASE/notifications/{id}/consume"

    val BASE_URL: String
        get() = if (isEmulator()) {
            "https://goat-touched-mite.ngrok-free.app/" // For Android Emulator
        } else {
            "https://goat-touched-mite.ngrok-free.app/" // Physical device
        }

    private fun isEmulator(): Boolean {
        return android.os.Build.FINGERPRINT.contains("generic")
                || android.os.Build.FINGERPRINT.lowercase().contains("emulator")
                || android.os.Build.MODEL.contains("Emulator")
                || android.os.Build.MODEL.contains("Android SDK built for x86")
    }
}