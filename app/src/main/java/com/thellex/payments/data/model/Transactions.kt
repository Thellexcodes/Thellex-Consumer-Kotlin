package com.thellex.payments.data.model

import com.google.gson.annotations.SerializedName
import com.thellex.payments.settings.SupportedBlockchain
import com.thellex.payments.settings.Token

data class Transaction(
    @SerializedName("id") val id: String,
    @SerializedName("type") val type: TransactionType,
    @SerializedName("description") val description: String,
    @SerializedName("amount") val amount: String,
    @SerializedName("timestamp") val timestamp: String,
    @SerializedName("iconResId") val iconResId: Int )

 enum class TransactionType {
    @SerializedName("DEPOSIT") DEPOSIT,
    @SerializedName("WITHDRAW") WITHDRAW,
}

enum class TransactionStatus(val displayName: String) {
    COMPLETED("Completed"),
    REJECTED("Rejected"),
    PENDING("Pending");

    companion object {
        fun fromString(value: String): TransactionStatus {
            return values().firstOrNull {
                it.name.equals(value, ignoreCase = true) || it.displayName.equals(value, ignoreCase = true)
            } ?: PENDING
        }
    }
}

data class PosTransaction(
    @SerializedName("iconResId") val iconResId: Int?,                  // txn_icon
    @SerializedName("statusIconResId") val statusIconResId: Int?,      // status_icon
    @SerializedName("description") val description: String,           // txn_description
    @SerializedName("time") val time: String,                         // time_text
    @SerializedName("amountWithSymbol") val amountWithSymbol: String, // amount (e.g., "20 USDT")
    @SerializedName("status") val status:  TransactionStatus                     // status (e.g., "Completed")
)

data class BlockchainItem(
    @SerializedName("chain") val chain: SupportedBlockchain,
    @SerializedName("iconRes") val iconRes: Int )

data class Crypto(
    @SerializedName("blockchain") val blockchain: Token,
    @SerializedName("iconRes") val iconRes: Int
) {
    override fun toString(): String = blockchain.name
}