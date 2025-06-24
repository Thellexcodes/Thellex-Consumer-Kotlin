package com.thellex.payments.settings

// --- Blockchain Only ---
enum class SupportedBlockchain {
    lisk, base, stellar, bep20, matic
}

val SUPPORTED_BLOCKCHAINS = listOf(
    SupportedBlockchain.lisk,
    SupportedBlockchain.base,
    SupportedBlockchain.stellar,
)

enum class Token {
    usdc, usdt, xlm, btc
}

//--- Payment Only ---
val LocalValue = "NGN"

enum class PaymentType {
    REQUEST_FIAT,
    WITHDRAW_FIAT,
    REQUEST_CRYPTO,
}