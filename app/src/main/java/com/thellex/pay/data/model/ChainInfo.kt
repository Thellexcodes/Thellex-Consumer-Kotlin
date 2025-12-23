package com.thellex.pay.data.model
import com.thellex.pay.settings.SupportedBlockchainEnum
import com.thellex.pay.settings.TokenEnum
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

data class ChainInfo(
    val id: SupportedBlockchainEnum,
    val displayName: String,
    val fee: Double,
    val minimumWithdrawal: Int,
    val arrivalTime: String,
    val supportedTokens: List<TokenInfo>
)