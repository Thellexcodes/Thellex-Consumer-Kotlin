package com.thellex.pay.data.model
import com.thellex.pay.settings.SupportedBlockchainEnum
import kotlinx.serialization.Serializable

@Serializable
data class ChainInfoDto(
    val id: SupportedBlockchainEnum,
    val displayName: String,
    val fee: Double,
    val minimumWithdrawal: Int,
    val arrivalTime: String,
    val supportedTokens: List<TokenInfo>,
    val iconUrl: String
)