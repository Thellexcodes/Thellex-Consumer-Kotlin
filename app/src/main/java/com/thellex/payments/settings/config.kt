package com.thellex.payments.settings

// --- Blockchain Only ---
enum class SupportedBlockchainEnum {
    lisk, base, stellar, bep20, matic;

    companion object {
        fun fromValue(value: String): SupportedBlockchainEnum? {
            return values().find { it.name.equals(value, ignoreCase = true) }
        }
    }
}

enum class Token {
    usdc, usdt, xlm, btc
}

//--- Payment Only ---
val LocalValue = "NGN"

enum class PaymentType {
    REQUEST_FIAT,
    WITHDRAW_FIAT,
    REQUEST_CRYPTO,
    WITHDRAW_CRYPTO
}