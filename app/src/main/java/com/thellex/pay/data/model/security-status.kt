package com.thellex.pay.data.model

data class SecurityStatus(
    val isPinSet: Boolean,
    val isBiometricEnabled: Boolean
)

data class SecurityUiState(
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val isError: Boolean = false,
    val errorMessage: String? = null
)

//data class PublicKeyCredentialCreationOptionsDto(
//    val challenge: String,
//    val rp: RPEntity,
//    val user: UserEntity,
//    val pubKeyCredParams: List<PubKeyCredParam>,
//    val timeout: Long?,
//    val attestation: String?,
//    val authenticatorSelection: AuthenticatorSelection?
//    // … other fields as per WebAuthn spec
//)

data class PinRequest(
    val pin: String
)