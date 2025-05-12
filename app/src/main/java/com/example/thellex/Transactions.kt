package com.example.thellex

class Transactions {
}

data class Transaction(
    val id: String,
    val type: TransactionType,
    val description: String,
    val amount: String,
    val timestamp: String,
    val iconResId: Int
)

enum class TransactionType {
        DEPOSIT,
        WITHDRAW,
//        BANK_TRANSFER
}

data class PosTransaction(
    val iconResId: Int,
    val tokenSymbol: String,
    val senderName: String,
    val time: String,
    val amount: String,
    val fiatValue: String,
    val statusIconResId: Int
)

data class Crypto(
    val name: String,
    val iconResId: Int
)