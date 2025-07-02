package com.thellex.payments.data.enums

import com.thellex.payments.core.utils.AppError

enum class UserErrorEnum(
    override val code: String,
    override val message: String
) : AppError {
    EMAIL_EXISTS("access/EMAIL_EXISTS", "This email already exists."),
    BRAND_EXISTS("access/BRAND_EXISTS", "This brand already exists."),
    REJECTED_AGREEMENT("access/REJECTED_AGREEMENT", "Accept terms and conditions."),
    INVALID_CREDENTIAL("access/INVALID_CREDENTIAL", "Invalid credentials."),
    EMAIL_NOT_VERIFIED("access/EMAIL_NOT_VERIFIED", "Email not verified."),
    PHONE_NOT_VERIFIED("access/PHONE_NOT_VERIFIED", "Phone number not verified."),
    INVALID_EMAIL_FORMAT("access/INVALID_EMAIL_FORMAT", "Invalid email format."),
    USER_NOT_FOUND("access/USER_NOT_FOUND", "User not found."),
    CODE_ALREADY_SENT("access/CODE_ALREADY_SENT", "Verification code already sent."),
    USER_SUSPENDED("access/USER_SUSPENDED", "Your account is currently suspended."),
    UNAUTHORIZED("access/UNAUTHORIZED", "Unauthorized. Please login."),
    UNKNOWN_ERROR("unknown", "An unknown error occurred.");

    companion object {
        fun fromCode(code: String?): UserErrorEnum {
            return entries.find { it.code == code } ?: UNKNOWN_ERROR
        }
    }
}

enum class PaymentErrorEnum(override val code: String, override val message: String) : AppError {
    INSUFFICIENT_FUNDS("payments/INSUFFICIENT_FUNDS", "Insufficient funds to complete this transaction."),
    INVALID_ADDRESS("payments/INVALID_ADDRESS", "The wallet address provided is invalid."),
    NETWORK_NOT_SUPPORTED("payments/NETWORK_NOT_SUPPORTED", "The selected network is currently unsupported."),
    TOKEN_NOT_FOUND("payments/TOKEN_NOT_FOUND", "The selected token is not available."),
    PAYMENT_LIMIT_EXCEEDED("payments/LIMIT_EXCEEDED", "You have exceeded your withdrawal limit."),
    DUPLICATE_REQUEST("payments/DUPLICATE_REQUEST", "This request has already been submitted."),
    WITHDRAWAL_BLOCKED("payments/WITHDRAWAL_BLOCKED", "Withdrawals are temporarily disabled."),
    UNKNOWN("payments/UNKNOWN", "An unknown payment error occurred.");

    companion object {
        fun fromCode(code: String?): PaymentErrorEnum {
            return entries.firstOrNull { it.code == code } ?: UNKNOWN
        }
    }
}
