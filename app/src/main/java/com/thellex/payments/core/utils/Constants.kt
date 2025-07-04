package com.thellex.payments.core.utils

object Constants {
    val BASE_URL: String
        get() = if (isEmulator()) {
            "https://goat-touched-mite.ngrok-free.app/" // For Android Emulator
        } else {
            "https://goat-touched-mite.ngrok-free.app/" // Your actual machine IP for physical devices
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
    const val WITHDRAW_CRYPTO_PAYMENT_ENDPOINT = "payments/withdraw-crypto"

    // Wallet Manager Endpoints
    const val WALLET_MANAGER_BALANCE_ENDPOINT= "wallet-manager/balance"

    // KYC and KYB
    const val KYC_ENDPOINT = "kyc/basic-nin-bvn"
    const val KYC_VERIFY_SELFIE_AND_DOCUMENT = "kyc/basic-document-verify-selfie"
}

//object Constants {
//    private const val API_VERSION = "v1"
//
//    val BASE_URL: String
//        get() = if (isEmulator()) {
//            "https://goat-touched-mite.ngrok-free.app/"
//        } else {
//            "https://goat-touched-mite.ngrok-free.app/"
//        }
//
//    private fun isEmulator(): Boolean {
//        return android.os.Build.FINGERPRINT.contains("generic")
//                || android.os.Build.FINGERPRINT.lowercase().contains("emulator")
//                || android.os.Build.MODEL.contains("Emulator")
//                || android.os.Build.MODEL.contains("Android SDK built for x86")
//    }
//
//    // Auth Endpoints
//    const val LOGIN_ENDPOINT = "$API_VERSION/user/access"
//    const val VERIFY_CODE_ENDPOINT = "$API_VERSION/user/verify"
//    const val AUTH_LOGIN_ENDPOINT = "$API_VERSION/user/authenticate"
//
//    // Payment Endpoints
//    const val REQUEST_CRYPTO_PAYMENT_ENDPOINT = "$API_VERSION/payments/request-crypto"
//    const val WITHDRAW_CRYPTO_PAYMENT_ENDPOINT = "$API_VERSION/payments/withdraw-crypto"
//
//    // Wallet Manager Endpoints
//    const val WALLET_MANAGER_BALANCE_ENDPOINT = "$API_VERSION/wallet-manager/balance"
//
//    // KYC and KYB
//    const val KYC_ENDPOINT = "$API_VERSION/kyc/basic"
//}
