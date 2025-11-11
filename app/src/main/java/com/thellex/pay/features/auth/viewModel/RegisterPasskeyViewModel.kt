package com.thellex.pay.features.auth.viewModel

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.credentials.CreatePublicKeyCredentialRequest
import androidx.credentials.CredentialManager
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.google.android.gms.fido.Fido
import com.google.android.gms.fido.fido2.api.common.Attachment
import com.google.android.gms.fido.fido2.api.common.AuthenticatorAttestationResponse
import com.google.android.gms.fido.fido2.api.common.AuthenticatorSelectionCriteria
import com.google.android.gms.fido.fido2.api.common.PublicKeyCredential
import com.google.android.gms.fido.fido2.api.common.PublicKeyCredentialCreationOptions
import com.thellex.pay.core.utils.buildFido2RegisterOptions
import com.thellex.pay.features.auth.repository.BiometricRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import kotlin.math.log


class RegisterPasskeyViewModel(
    private val context: Context,
    private val repository: BiometricRepository
) : ViewModel() {

    private val TAG = "RegisterPasskey"

    var message by androidx.compose.runtime.mutableStateOf("")
        private set

    private var registerLauncher: ActivityResultLauncher<IntentSenderRequest>? = null
    private var storedChallenge: String? = null


    private val credentialManager = CredentialManager.create(context)


    fun setRegisterLauncher(launcher: ActivityResultLauncher<IntentSenderRequest>) {
        Log.d(TAG, "Launcher initialized successfully")
        this.registerLauncher = launcher
    }

    @SuppressLint("PublicKeyCredential")
    fun registerPasskey() {
        viewModelScope.launch {
            Log.d(TAG, "Starting passkey registration process...")
            try {
                // ✅ Step 1: Get registration challenge from backend
                Log.d(TAG, "Requesting registration challenge from backend...")
                val challengeResponse = repository.createChallenge()
                val data = challengeResponse.result!!

                storedChallenge = data.challenge
                Log.d(TAG, "Received challenge: $storedChallenge")

                // ✅ Step 2: Build registration options for FIDO2
                Log.d(TAG, "Building FIDO2 registration options...")
                Log.d(TAG, "Registration options built successfully")

                val userId = data.user?.id ?: ""
                val userName = data.user?.name ?: ""
                val displayName = data.user?.displayName ?: ""
//                val requestJson = """
//                            {
//                              "challenge": "${data.challenge}",
//                              "rp": {
//                                "id": "${data.rp.id}",
//                                "name": "${data.rp.name}"
//                              },
//                              "user": {
//                                "id": "$userId",
//                                "name": "$userName",
//                                "displayName": "$displayName"
//                              },
//                              "pubKeyCredParams": [
//                                {"type": "public-key", "alg": -8},
//                                {"type": "public-key", "alg": -7},
//                                {"type": "public-key", "alg": -257}
//                              ],
//                              "timeout": 60000,
//                              "attestation": "none",
//                              "excludeCredentials": [],
//                              "authenticatorSelection": {
//                                "residentKey": "preferred",
//                                "requireResidentKey": false,
//                                "userVerification": "preferred",
//                                "authenticatorAttachment": "cross-platform",
//                              },
//                              "extensions": { "credProps": true },
//                              "hints": [],
//                              "mode": "register"
//                            }
//                        """.trimIndent()
                val requestJson = """
{
  "challenge": "${data.challenge}",
  "rp": { "id": "${data.rp.id}", "name": "${data.rp.name}" },
  "user": {
    "id": "${data.user?.id}",
    "name": "${data.user?.name}",
    "displayName": "${data.user?.displayName}"
  },
  "pubKeyCredParams": [
    {"type": "public-key", "alg": -7},
    {"type": "public-key", "alg": -257}
  ],
  "authenticatorSelection": {
    "authenticatorAttachment": "cross-platform",
    "userVerification": "preferred"
  },
  "attestation": "none"
}
""".trimIndent()


                Log.d(TAG, "Options request: $requestJson")
                val options = buildFido2RegisterOptions(data)
                val request = CreatePublicKeyCredentialRequest(requestJson)
                val result = credentialManager.createCredential(context, request)
                Log.d(TAG, "result is ${result}")

//                val credential = result.credential as PublicKeyCredential
//                val responseJson = JSONObject(credential.authenticationResponseJson)

                return@launch

//
//                // ✅ Step 3: Request FIDO2 registration PendingIntent
//                Log.d(TAG, "Requesting FIDO2 PendingIntent from Play Services...")
//                val fido2Client = Fido.getFido2ApiClient(context)
//                fido2Client.getRegisterPendingIntent(options)
//                    .addOnSuccessListener { pendingIntent ->
//                        if (pendingIntent == null) {
//                            Log.e(TAG, "No authenticator available for registration")
//                            updateMessage("No authenticator available on this device")
//                            return@addOnSuccessListener
//                        }
//
//                        Log.d(TAG, "Successfully obtained PendingIntent for registration")
//                        Log.d(TAG, "Launching biometric registration dialog...")
//                        val intentSenderRequest =
//                            IntentSenderRequest.Builder(pendingIntent.intentSender).build()
//                        if (registerLauncher != null) {
//                            registerLauncher!!.launch(intentSenderRequest)
//                            Log.d(TAG, "Launched FIDO2 biometric registration flow")
//                        } else {
//                            Log.e(TAG, "Launcher not set! Call setRegisterLauncher first.")
//                            updateMessage("Launcher not initialized")
//                        }
//                    }
//                    .addOnFailureListener { e ->
//                        Log.e(TAG, "FIDO2 pending intent creation failed", e)
//                        updateMessage("Error preparing FIDO2 registration: ${e.message}")
//                    }

            } catch (e: Exception) {
                Log.e(TAG, "Exception during registration", e)
                updateMessage("Error starting registration: ${e.message}")
            }
        }
    }

    fun handleRegisterResult(resultCode: Int, data: Intent?) {
        Log.d(TAG, "Handling FIDO2 registration result...")

        viewModelScope.launch {
            try {
                if (resultCode != Activity.RESULT_OK) {
                    Log.e(TAG, "Result code not OK: $resultCode")
                    updateMessage("Registration cancelled or failed")
                    return@launch
                }

                if (data == null) {
                    Log.e(TAG, "Result intent is null")
                    updateMessage("Registration failed: No data received")
                    return@launch
                }

                Log.d(TAG, "Intent data received. Extracting FIDO2 response...")

                // Log all extras for debugging
                val extrasList = data.extras?.keySet()?.joinToString(", ") ?: "none"
                Log.d("RegisterPasskey", "Intent extras dump: $extrasList")

                // ✅ Step 5: Extract the FIDO2 response payload
                val responseBytes = data.getByteArrayExtra(Fido.FIDO2_KEY_RESPONSE_EXTRA)
                if (responseBytes == null) {
                    Log.e(TAG, "No FIDO2_KEY_RESPONSE_EXTRA found in intent")
                    Log.e(TAG, "Intent extras dump: ${data.extras?.keySet()?.joinToString() ?: "None"}")
                    throw IllegalStateException("No FIDO2 response extra found in intent")
                }

                Log.d(TAG, "Successfully extracted FIDO2 response (${responseBytes.size} bytes)")
                val credential = PublicKeyCredential.deserializeFromBytes(responseBytes)
                val attestationResponse = credential.response as? AuthenticatorAttestationResponse
                    ?: throw IllegalStateException("Invalid attestation response type")

                Log.d(TAG, "Deserialized credential type: ${credential.type}")
                Log.d(TAG, "RawId length: ${credential.rawId?.size}")
                Log.d(TAG, "Attestation object length: ${attestationResponse.attestationObject.size}")
                Log.d(TAG, "Client data JSON length: ${attestationResponse.clientDataJSON.size}")

                // TODO: send to backend verification when ready
                Log.d(TAG, "Ready to send credential to backend for verification (commented out section)")

            } catch (e: Exception) {
                Log.e(TAG, "Handle result failed", e)
                updateMessage("Verification failed: ${e.message}")
            }
        }
    }

    private fun updateMessage(msg: String) {
        message = msg
        Log.d(TAG, "UI message updated: $msg")
    }

    companion object {
        const val REGISTER_REQUEST_CODE = 101
    }
}


class RegisterPasskeyViewModelFactory(
    private val context: Context,
    private val repository: BiometricRepository
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(RegisterPasskeyViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return RegisterPasskeyViewModel(context, repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

