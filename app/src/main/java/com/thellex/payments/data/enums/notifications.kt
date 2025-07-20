package com.thellex.payments.data.enums

enum class NotificationEventsEnum(val event: String) {
    ACCOUNT_UPDATED("account_updated"),
    PASSWORD_CHANGED("password_changed"),
    NEW_MESSAGE("new_message"),
    FRIEND_REQUEST("friend_request"),
    WALLET_ADDRESS_GENERATED("wallet_address_generated"),
    DEPOSIT_SUCCESSFUL("deposit_successful"),
    WITHDRAWAL_SUCCESSFUL("withdrawal_successful"),
    PAYMENT_INITIATED("payment.initiated"),
    PAYMENT_COMPLETED("payment.completed"),
    PAYMENT_FAILED("payment.failed"),
    PAYMENT_REFUNDED("payment.refunded"),
    COLLECTION_CREATED("collection.created"),
    COLLECTION_FAILED("collection.failed"),
    COLLECTION_CANCELLED("collection.cancelled"),
    TRANSACTION_FAILED("transaction_failed"),
    SYSTEM_ALERT("system_alert"),
    PROMOTION("promotion");

    companion object {
        fun fromValue(value: String): NotificationEventsEnum? {
            return values().find { it.event == value }
        }
    }
}
