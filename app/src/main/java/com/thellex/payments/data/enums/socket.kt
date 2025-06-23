package com.thellex.payments.data.enums

enum class NotificationSockets(val event: String) {
    DEPOSIT_SUCCESSFUL("deposit_successful"),
    WITHDRAWAL_SUCCESSFUL("withdrawal_successful"),
    TRANSACTION_FAILED("transaction_failed"),
    NEW_MESSAGE("new_message"),
    FRIEND_REQUEST("friend_request"),
    SYSTEM_ALERT("system_alert"),
    ACCOUNT_UPDATED("account_updated"),
    PASSWORD_CHANGED("password_changed"),
    PROMOTION("promotion");
}
