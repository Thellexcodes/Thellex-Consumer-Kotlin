package com.thellex.pay.core.utils

import android.content.Context
import android.hardware.biometrics.BiometricManager
import android.util.Base64
import com.google.android.gms.fido.fido2.api.common.*
import com.thellex.pay.data.model.ChallengeResponse



fun buildFido2RegisterOptions(response: ChallengeResponse): PublicKeyCredentialCreationOptions {
    return PublicKeyCredentialCreationOptions.Builder()
        .setRp(PublicKeyCredentialRpEntity(response.rp.id, response.rp.name, null))
        .setUser(PublicKeyCredentialUserEntity(response.user?.id!!.toByteArray(), response.user.name!!, null, response.user.displayName!!))
        .setChallenge(Base64.decode(response.challenge, Base64.URL_SAFE))
        .setParameters(response.pubKeyCredParams.map { PublicKeyCredentialParameters(it.type, it.alg) })
        .setTimeoutSeconds((response.timeout?.div(1000))?.toDouble())
        .setAttestationConveyancePreference(AttestationConveyancePreference.NONE)
        .build()
}

//fun isPasskeySupported(context: Context): Boolean {
//    val biometricManager = BiometricManager.from(context)
//
//    // Check if the device has a strong biometric (FIDO2 / passkey capable)
//    val canAuthenticate = biometricManager.canAuthenticate(
//        BiometricManager.Authenticators.BIOMETRIC_STRONG or
//                BiometricManager.Authenticators.DEVICE_CREDENTIAL
//    )
//
//    return when (canAuthenticate) {
//        BiometricManager.BIOMETRIC_SUCCESS -> true
//        else -> false
//    }
//}