package com.thellex.payments.settings

// --- Blockchain Types ---
enum class SupportedBlockchain {
    lisk, base, stellar, bep20, matic
}

// --- Blockchain Network Support ---
val SUPPORTED_BLOCKCHAINS = listOf(
    SupportedBlockchain.lisk,
    SupportedBlockchain.base,
    SupportedBlockchain.stellar,
)

// --- Token Support ---
enum class Token {
    usdc, usdt, xlm, btc
}

//--- Payment Types ---
enum class PaymentType {
    REQUEST_FIAT,
    WITHDRAW_FIAT,
    REQUEST_CRYPTO,
}