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

enum class TokensEnum {
    usdc, usdt, xlm, btc
}

enum class FiatEnum(val code: String) {
    NGN("ngn"),
    GHC("ghc");

    companion object {
        fun fromCode(code: String): FiatEnum? {
            return entries.find { it.code.equals(code, ignoreCase = true) }
        }
    }
}

//--- Payment Only ---
val LocalValue = "NGN"

enum class PaymentType {
    REQUEST_FIAT,
    WITHDRAW_FIAT,
    REQUEST_CRYPTO,
    WITHDRAW_CRYPTO
}