package com.thellex.pay.features.auth.viewModel

import android.content.Context
import android.content.Intent
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.google.android.gms.fido.Fido
import com.thellex.pay.data.model.UserPreferences
import com.thellex.pay.features.auth.repository.BiometricRepository
import javax.inject.Inject

class BiometricLoginViewModel @Inject constructor(
    private val context: Context,
    private val bioRepo: BiometricRepository,
    private val prefs: UserPreferences
) : ViewModel() {

    private val fido2Client = Fido.getFido2ApiClient(context)
    var message by mutableStateOf("")
        private set

    fun startBiometricAuth() {
//        viewModelScope.launch {
//            val email = prefs.getUserEmail().firstOrNull() ?: return@launch
//            try {
//                val authOptions = bioRepo.getAuthOptions(email)
//                val options = /* build PublicKeyCredentialRequestOptions */
//                val intent = fido2Client.getSignIntent(options)
//                (context as Activity).startIntentSenderForResult(
//                    intent.intentSender, AUTH_REQUEST_CODE, null, 0, 0, 0, null
//                )
//            } catch (e: Exception) {
//                message = "Failed to start biometric auth: ${e.message}"
//            }
//        }
    }

    fun handleAuthResult(data: Intent?, challenge: String) {
//        viewModelScope.launch {
//            try {
//                val fido2Response = AuthenticatorAssertionResponse.deserializeFromBytes(
//                    data?.getByteArrayExtra(Fido.FIDO2_KEY_RESPONSE_EXTRA)
//                )
//
//                val verifyRequest = VerifyAuthenticationRequest(
//                    challenge = challenge,
//                    attestationResponse = fido2Response
//                )
//
//                val result = bioRepo.verifyAuth(verifyRequest)
//                message = result.message
//            } catch (e: Exception) {
//                message = "Authentication failed: ${e.message}"
//            }
//        }
    }

    companion object {
        const val AUTH_REQUEST_CODE = 202
    }
}
