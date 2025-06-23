package com.thellex.payments.data.model

import com.google.gson.annotations.SerializedName
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CreateSubAccountRequest(
    @SerialName("email") val email: String
)

@Serializable
data class SubAccountData(
    @SerialName("id") val id: String,
    @SerialName("sn") val sn: String,
    @SerialName("email") val email: String,
    @SerialName("reference") val reference: String,
    @SerialName("first_name") val firstName: String,
    @SerialName("last_name") val lastName: String,
    @SerialName("display_name") val displayName: String,
    @SerialName("created_at") val createdAt: String,
    @SerialName("updated_at") val updatedAt: String
)

//Wallets
@Serializable
data class QwalletNetwork(
    val id: String,
    val name: String,
    val deposits_enabled: Boolean,
    val withdraws_enabled: Boolean
)

data class QWallet(
    @SerializedName("id") val id: String,
    @SerializedName("reference") val reference: String?,
    @SerializedName("currency") val currency: String,
    @SerializedName("address") val address: String,
    @SerializedName("network") val network: String,  // ✅ Change to String
    @SerializedName("is_crypto") val isCrypto: Boolean,
    @SerializedName("destination_tag") val destinationTag: String?,
    @SerializedName("deposit_address") val depositAddress: String?,
    @SerializedName("total_payments") val totalPayments: String?,
    @SerializedName("created_at") val createdAt: String,
    @SerializedName("updated_at") val updatedAt: String
)

data class QWalletProfileEntity(
    @SerializedName("id") val id: String,
    @SerializedName("qid") val qid: String,
    @SerializedName("q_sn") val qsn: String,
    @SerializedName("state") val state: String,
    @SerializedName("first_name") val firstName: String?,
    @SerializedName("last_name") val lastName: String?,
    @SerializedName("reference") val reference: String?,
    @SerializedName("display_name") val displayName: String?,
    @SerializedName("wallets") val wallets: List<QWalletEntity>?,
    @SerializedName("created_at") val createdAt: String,
    @SerializedName("updated_at") val updatedAt: String
)


data class QWalletEntity(
    @SerializedName("qid") val qid: String,
    @SerializedName("qsn") val qsn: String,
    @SerializedName("state") val state: String,
    @SerializedName("firstName") val firstName: String?,
    @SerializedName("lastName") val lastName: String?,
    @SerializedName("reference") val reference: String?,
    @SerializedName("displayName") val displayName: String?,
    @SerializedName("walletProvider") val walletProvider: String,
//    @SerializedName("wallets") val wallets: List<WalletDto>
)

@Serializable
data class QWalletWithdrawalFeeResponse(
    @SerialName("fee") val fee: Double,
    @SerialName("type") val type: Fee
)

@Serializable
data class SwapData(
    @SerialName("id") val id: String,
    @SerialName("from_currency") val fromCurrency: String,
    @SerialName("to_currency") val toCurrency: String,
    @SerialName("quoted_price") val quotedPrice: String,
    @SerialName("quoted_currency") val quotedCurrency: String,
    @SerialName("from_amount") val fromAmount: String,
    @SerialName("to_amount") val toAmount: String,
    @SerialName("confirmed") val confirmed: Boolean,
    @SerialName("expires_at") val expiresAt: String,
    @SerialName("created_at") val createdAt: String,
    @SerialName("updated_at") val updatedAt: String,
    @SerialName("user") val user: SubAccountData
)

@Serializable
data class WithdrawRecipientDetails(
    @SerialName("address") val address: String,
    @SerialName("destination_tag") val destinationTag: String? = null,
    @SerialName("name") val name: String? = null
)

@Serializable
data class WithdrawRecipient(
    @SerialName("type") val type: String,
    @SerialName("details") val details: WithdrawRecipientDetails
)

@Serializable
data class WithdrawWallet(
    @SerialName("id") val id: String,
    @SerialName("currency") val currency: String,
    @SerialName("balance") val balance: String,
    @SerialName("locked") val locked: String,
    @SerialName("staked") val staked: String,
    @SerialName("converted_balance") val convertedBalance: String,
    @SerialName("reference_currency") val referenceCurrency: String,
    @SerialName("is_crypto") val isCrypto: Boolean,
    @SerialName("created_at") val createdAt: String,
    @SerialName("updated_at") val updatedAt: String,
    @SerialName("deposit_address") val depositAddress: String,
    @SerialName("destination_tag") val destinationTag: String? = null
)

@Serializable
data class WithdrawData(
    @SerialName("id") val id: String,
    @SerialName("reference") val reference: String? = null,
    @SerialName("type") val type: String,
    @SerialName("currency") val currency: String,
    @SerialName("amount") val amount: String,
    @SerialName("fee") val fee: String,
    @SerialName("total") val total: String,
    @SerialName("txid") val txid: String? = null,
    @SerialName("transaction_note") val transactionNote: String,
    @SerialName("narration") val narration: String,
    @SerialName("status") val status: String,
    @SerialName("reason") val reason: String? = null,
    @SerialName("created_at") val createdAt: String,
    @SerialName("done_at") val doneAt: String? = null,
    @SerialName("recipient") val recipient: WithdrawRecipient,
    @SerialName("wallet") val wallet: WithdrawWallet,
    @SerialName("user") val user: SubAccountData
)
