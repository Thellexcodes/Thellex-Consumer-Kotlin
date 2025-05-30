package com.thellex.pos.data.model

object Constants {
    val BASE_URL: String
        get() = if (isEmulator()) {
            "http://10.0.2.2:8100" // For Android Emulator
        } else {
            "http://192.168.13.51:8100" // Your actual machine IP for physical devices
        }

    private fun isEmulator(): Boolean {
        return android.os.Build.FINGERPRINT.contains("generic")
                || android.os.Build.FINGERPRINT.lowercase().contains("emulator")
                || android.os.Build.MODEL.contains("Emulator")
                || android.os.Build.MODEL.contains("Android SDK built for x86")
    }
    const val LOGIN_ENDPOINT = "user"
    const val VERIFY_CODE_ENDPOINT = "user/verify"
}