package com.thellex.pos.data.model

import com.thellex.pos.settings.SupportedBlockchain
import com.thellex.pos.settings.Token

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

//data class Crypto(
//    val name: SupportedBlockchain,
//    val iconResId: Int
//)

data class BlockchainItem(val chain: SupportedBlockchain, val iconRes: Int)


data class Crypto(val blockchain: Token, val iconRes: Int) {
    override fun toString(): String {
        return blockchain.name
    }
}
