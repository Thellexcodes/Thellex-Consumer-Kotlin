package com.thellex.pay.settings

// --- Blockchain Only ---
enum class SupportedBlockchainEnum {
    lisk, base, stellar, bep20, matic, starknet, tron, ethereum, celo;

    companion object {
        fun fromValue(value: String): SupportedBlockchainEnum? {
            return entries.find { it.name.equals(value, ignoreCase = true) }
        }
    }
}

enum class TokenEnum {
    usdt, usdc, btc, eth, strk;

    companion object {
        fun fromValue(value: String): TokenEnum? {
            return entries.find { it.name.equals(value, ignoreCase = true) }
        }
    }
}

enum class FiatEnum(val code: String) {
    ngn("ngn"), ghc("ghc");

    companion object {
        fun fromCode(code: String): FiatEnum? {
            return entries.find { it.code.equals(code, ignoreCase = true) }
        }
    }
}

//--- Payment Only ---
const val LocalValue = "NGN"

enum class PaymentType {
    REQUEST_FIAT,
    WITHDRAW_FIAT,
    REQUEST_CRYPTO,
    WITHDRAW_CRYPTO
}

data class FiatCurrency(
    val name: String,
    val country: String,
    val symbol: String,
    val currencyCode: String
)

object FiatTickers {
    private val currencies: Map<String, FiatCurrency> = mapOf(
        "ngn" to FiatCurrency(
            name = "Nigerian Naira",
            country = "Nigeria",
            symbol = "₦",
            currencyCode = "NGN"
        ),
        "ghc" to FiatCurrency(
            name = "Ghanaian Cedi",
            country = "Ghana",
            symbol = "₵",
            currencyCode = "GHS"
        ),
        "kes" to FiatCurrency(
            name = "Kenyan Shilling",
            country = "Kenya",
            symbol = "KSh",
            currencyCode = "KES"
        ),
        "zar" to FiatCurrency(
            name = "South African Rand",
            country = "South Africa",
            symbol = "R",
            currencyCode = "ZAR"
        ),
        "bwp" to FiatCurrency(
            name = "Botswana Pula",
            country = "Botswana",
            symbol = "P",
            currencyCode = "BWP"
        ),
        "usd" to FiatCurrency(
            name = "United States Dollar",
            country = "United States",
            symbol = "$",
            currencyCode = "USD"
        )
    )

    // ISO 3166-1 alpha-2 country code to key mapping
    private val countryCodeToCurrencyKey: Map<String, String> = mapOf(
        "ng" to "ngn",
        "gh" to "ghc",
        "ke" to "kes",
        "za" to "zar",
        "bw" to "bwp",
        "us" to "usd"
    )

    fun getByCodeOrCountry(input: String): FiatCurrency? {
        val key = input.lowercase()
        return currencies[key] ?: currencies[countryCodeToCurrencyKey[key]]
    }

    fun getByCode(code: String): FiatCurrency? {
        return currencies[code.lowercase()]
    }
}

data class TransactionLimits(
    val deposit: Int,
    val withdrawal: Int
)

val MIN_TRANSACTION_AMOUNT: Map<String, TransactionLimits> = mapOf(
    "NGN" to TransactionLimits(deposit = 100, withdrawal = 100),
    "GHC" to TransactionLimits(deposit = 100, withdrawal = 100),
    "KES" to TransactionLimits(deposit = 500, withdrawal = 500),
    "ZAR" to TransactionLimits(deposit = 100, withdrawal = 100),
    "BWP" to TransactionLimits(deposit = 100, withdrawal = 100)
)

val minimumAmountInFiat = MIN_TRANSACTION_AMOUNT["NGN"]?.deposit ?: 0