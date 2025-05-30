package com.thellex.pos

enum class ERRORS(val message: String) {
    EMAIL_EXISTS("This email already exists."),
    BRAND_EXISTS("This brand already exists."),
    REJECTED_AGREEMENT("Accept terms and conditions"),
    INVALID_CREDENTIAL("Invalid Credentials"),
    EMAIL_NOT_VERIFIED("Email not verified"),
    PHONE_NOT_VERIFIED("Phone number not verified"),
    UNKNOWN_ERROR("An unknown error occurred.");
}