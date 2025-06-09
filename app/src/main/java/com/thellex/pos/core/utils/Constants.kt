package com.thellex.pos.core.utils

object Constants {
    val BASE_URL: String
        get() = if (isEmulator()) {
            "http://192.168.13.51:8100" // For Android Emulator
        } else {
            "http://192.168.13.51:8100" // Your actual machine IP for physical devices
        }

    private fun isEmulator(): Boolean {
        return android.os.Build.FINGERPRINT.contains("generic")
                || android.os.Build.FINGERPRINT.lowercase().contains("emulator")
                || android.os.Build.MODEL.contains("Emulator")
                || android.os.Build.MODEL.contains("Android SDK built for x86")
    }
    const val LOGIN_ENDPOINT = "user/access"
    const val VERIFY_CODE_ENDPOINT = "user/verify"
    const val AUTH_LOGIN_ENDPOINT = "user/authenticate"

    // Payment Endpoints
    const val REQUEST_CRYPTO_PAYMENT_ENDPOINT = "payments/request-crypto"
    const val REQUEST_FIAT_PAYMENT_ENDPOINT = "payments/request-fiat"
    const val WITHDRAW_PAYMENT_ENDPOINT = "payments/withdraw"
    const val OFF_RAMP_PAYMENT_ENDPOINT = "payments/off-ramp"

    // Wallet Manager Endpoints
    const val WALLET_MANAGER_BALANCE_ENDPOINT= "wallet-manager/balance"
}