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