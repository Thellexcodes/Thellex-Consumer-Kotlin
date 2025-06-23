package com.thellex.payments.data.enums


enum class ERRORS(val message: String) {
    EMAIL_EXISTS("This email already exists."),
    BRAND_EXISTS("This brand already exists."),
    REJECTED_AGREEMENT("Accept terms and conditions."),
    INVALID_CREDENTIAL("Invalid credentials."),
    EMAIL_NOT_VERIFIED("Email not verified."),
    PHONE_NOT_VERIFIED("Phone number not verified."),
    USER_NOT_FOUND("User not found."),
    UNAUTHORIZED("Unauthorized. Please login."),
    INVALID_TOKEN("Invalid or expired token."),
    USER_SUSPENDED("Your account is currently suspended."),
    UNKNOWN_ERROR("An unknown error occurred.");

    companion object {
        fun fromCode(code: String?): ERRORS {
            return when (code) {
                "auth/unauthorized" -> UNAUTHORIZED
                "auth/invalid-token" -> INVALID_TOKEN
                "auth/user-not-found" -> USER_NOT_FOUND
                "auth/user-suspended" -> USER_SUSPENDED
                "email/exists" -> EMAIL_EXISTS
                "brand/exists" -> BRAND_EXISTS
                "agreement/rejected" -> REJECTED_AGREEMENT
                "auth/invalid-credential" -> INVALID_CREDENTIAL
                "email/not-verified" -> EMAIL_NOT_VERIFIED
                "phone/not-verified" -> PHONE_NOT_VERIFIED
                else -> UNKNOWN_ERROR
            }
        }
    }
}
