package com.thellex.payments.data.model

import com.google.gson.annotations.SerializedName
import com.thellex.payments.settings.FiatEnum
import com.thellex.payments.settings.PaymentType
import com.thellex.payments.settings.SupportedBlockchainEnum
import com.thellex.payments.settings.TokensEnum
import kotlinx.serialization.Serializable

data class CreateRequestPaymentDto(
    @SerializedName("paymentType") val paymentType: PaymentType,
    @SerializedName("assetCode") val assetCode: TokensEnum,
    @SerializedName("assetIssuer") val assetIssuer: String? = null,
    @SerializedName("amount") val amount: String? = null,
    @SerializedName("network") val network: SupportedBlockchainEnum,
    @SerializedName("sourceAddress") val sourceAddress: String,
    @SerializedName("fund_uid") val fundUid: String,
    @SerializedName("transaction_note") val transactionNote: String? = null,
    @SerializedName("narration") val narration: String? = null,
    @SerializedName("fund_uid2") val fundUid2: String? = null
)

@Serializable()
data class IBankAccountDto(
    @SerializedName("bankName") val bankName: String,
    @SerializedName("accountName") val accountName: String,
    @SerializedName("accountNumber") val accountNumber: String,
    @SerializedName("swiftCode") val swiftCode: String? = null,
    @SerializedName("iban") val iban: String? = null,
    @SerializedName("isPrimary") val isPrimary: Boolean,
    @SerializedName("external_createdAt") val externalCreatedAt: String,
    @SerializedName("require_consent") val requireConsent: Boolean,
    @SerializedName("consent_url") val consentUrl: String? = null,
    @SerializedName("reference") val reference: String? = null,
    @SerializedName("eur") val eur: String? = null
)

enum class PaymentStatusEnum(val value: String) {
    None("None"),
    Complete("COMPLETE"),
    Confirmed("confirmed"),
    Accepted("accepted"),
    Done("Done"),
    Processing("Processing"),

    Outbound("OUTBOUND"),
    Inbound("INBOUND"),
    PendingRiskScreening("PENDING_RISK_SCREENING"),
    Queued("QUEUED"),
    Sent("SENT"),
    Rejected("Rejected");

    companion object {
        fun fromValue(value: String): PaymentStatusEnum? = entries.find { it.value.equals(value, ignoreCase = true) }
    }
}

enum class TransactionTypeEnum(val value: String) {
    CRYPTO_DEPOSIT("crypto_deposit"),
    CRYPTO_WITHDRAWAL("crypto_withdrawal"),

    FIAT_TO_CRYPTO_DEPOSIT("fiat_to_crypto_deposit"),
    FIAT_TO_CRYPTO_WITHDRAWAL("fiat_to_crypto_withdrawal"),

    CRYPTO_TO_FIAT_DEPOSIT("crypto_to_fiat_deposit"),
    CRYPTO_TO_FIAT_WITHDRAWAL("crypto_to_fiat_withdrawal"),

    FIAT_TO_FIAT_DEPOSIT("fiat_to_fiat_deposit"),
    FIAT_TO_FIAT_WITHDRAWAL("fiat_to_fiat_withdrawal");

    companion object {
        fun fromValue(value: String): TransactionTypeEnum? =
            values().find { it.value.equals(value, ignoreCase = true) }
    }
}

enum class WalletWebhookEventEnum(val value: String) {
    // Errors
    INVALID_USER("INVALID_USER"),
    INVALID_CURRENCY("INVALID_CURRENCY"),
    INVALID_AMOUNT("INVALID_AMOUNT"),
    DUPLICATE_TRANSACTION("DUPLICATE_TRANSACTION"),
    WALLET_NOT_FOUND("WALLET_NOT_FOUND"),
    NETWORK_NOT_SUPPORTED("NETWORK_NOT_SUPPORTED"),
    USER_NOT_FOUND("USER_NOT_FOUND"),
    TRANSACTION_NOT_CONFIRMED("TRANSACTION_NOT_CONFIRMED"),
    UNSUPPORTED_EVENT("UNSUPPORTED_EVENT"),
    INVALID_SIGNATURE("INVALID_SIGNATURE"),
    MISSING_REQUIRED_FIELDS("MISSING_REQUIRED_FIELDS"),
    INTERNAL_SERVER_ERROR("INTERNAL_SERVER_ERROR"),
    TRANSACTION_FOUND("TRANSACTION_FOUND"),
    TRANSACTION_NOT_FOUND("TRANSACTION_NOT_FOUND"),

    // Wallet events
    WALLET_UPDATED("WALLET_UPDATED"),
    WALLET_ADDRESS_GENERATED("WALLET_ADDRESS_GENERATED"),

    // Deposit events
    DEPOSIT_CONFIRMATION("DEPOSIT_CONFIRMATION"),
    DEPOSIT_SUCCESSFUL("DEPOSIT_SUCCESSFUL"),
    DEPOSIT_ON_HOLD("DEPOSIT_ON_HOLD"),
    DEPOSIT_FAILED_AML("DEPOSIT_FAILED_AML"),
    DEPOSIT_REJECTED("DEPOSIT_REJECTED"),

    // Withdrawal events
    WITHDRAW_SUCCESSFUL("WITHDRAW_SUCCESSFUL"),
    WITHDRAW_REJECTED("WITHDRAW_REJECTED"),

    // Order events
    ORDER_DONE("ORDER_DONE"),
    ORDER_CANCELLED("ORDER_CANCELLED"),

    // Swap events
    SWAP_COMPLETED("SWAP_COMPLETED"),
    SWAP_REVERSED("SWAP_REVERSED"),
    SWAP_FAILED("SWAP_FAILED"),

    // Sell transaction events
    SELL_TRANSACTION_SUCCESSFUL("SELL_TRANSACTION_SUCCESSFUL"),
    SELL_TRANSACTION_PROCESSING("SELL_TRANSACTION_PROCESSING"),
    SELL_TRANSACTION_FAILED("SELL_TRANSACTION_FAILED"),

    // Buy transaction events
    BUY_TRANSACTION_SUCCESSFUL("BUY_TRANSACTION_SUCCESSFUL"),
    BUY_TRANSACTION_PROCESSING("BUY_TRANSACTION_PROCESSING"),
    BUY_TRANSACTION_FAILED("BUY_TRANSACTION_FAILED");

    companion object {
        fun fromValue(value: String?): WalletWebhookEventEnum? {
            if (value.isNullOrBlank()) return null
            val normalizedValue = value.replace(".", "_").uppercase()
            return entries.firstOrNull { it.value == normalizedValue }
        }
    }
}

data class FiatToCryptoOnRampRequestDto(
    @SerializedName("userAmount") val userAmount: Double,
    @SerializedName("fiatCode") val fiatCode: String,
    @SerializedName("assetCode") val assetCode: String,
    @SerializedName("country") val country: String,
    @SerializedName("paymentReason") val paymentReason: String,
    @SerializedName("network") val network: String,
    @SerializedName("destinationAddress") val destinationAddress: String
)

data class CryptoToFiatOffRampRequestDto(
    @SerializedName("assetCode") val assetCode: TokensEnum,
    @SerializedName("network") val network: SupportedBlockchainEnum,
    @SerializedName("userAmount") val userAmount: Double,
    @SerializedName("fiatCode") val fiatCode: FiatEnum,
    @SerializedName("country") val country: String,
    @SerializedName("paymentMethod") val paymentMethod: String? = null,
    @SerializedName("paymentReason") val paymentReason: String,
    @SerializedName("sourceAddress") val sourceAddress: String,
    @SerializedName("bankInfo") val bankInfo: IBankInfoRequestDto,
    @SerializedName("mainAssetAmount") val mainAssetAmount: Double? = null,
    @SerializedName("mainFiatAmount") val mainFiatAmount: Double? = null
)

data class CreateBankAccountDto(
    @SerializedName("bankName") val bankName: String,
    @SerializedName("accountNumber") val accountNumber: Number,
    @SerializedName("bankCode") val bankCode: String,
    @SerializedName("accountName") val accountName: String? = null,
    @SerializedName("swiftCode") val swiftCode: String? = null,
    @SerializedName("iban") val iban: String? = null,
)




