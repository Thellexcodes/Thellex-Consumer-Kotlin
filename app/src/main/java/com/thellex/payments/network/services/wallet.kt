package com.thellex.payments.network.services

import com.google.gson.JsonDeserializationContext
import com.thellex.payments.data.model.ApiResponse
import com.thellex.payments.core.utils.Constants
import com.thellex.payments.features.wallet.model.WalletBalanceDto
import com.thellex.payments.settings.SupportedBlockchainEnum
import retrofit2.http.GET
import com.google.gson.JsonDeserializer
import com.thellex.payments.data.model.PaymentStatusEnum
import com.thellex.payments.data.model.TransactionTypeEnum
import retrofit2.http.Query
import java.lang.reflect.Type

interface WalletManagerService {
    @GET(Constants.Endpoints.WALLET_BALANCE)
    suspend fun fetchBalance(@Query("action") action: String?): ApiResponse<WalletBalanceDto>
}

class SupportedBlockchainDeserializer : JsonDeserializer<SupportedBlockchainEnum> {
    override fun deserialize(
        json: com.google.gson.JsonElement?,
        typeOfT: Type?,
        context: JsonDeserializationContext?
    ): SupportedBlockchainEnum? {
        val value = json?.asString
        return value?.let { SupportedBlockchainEnum.fromValue(it) }
    }
}

class PaymentStatusDeserializer : JsonDeserializer<PaymentStatusEnum> {
    override fun deserialize(
        json: com.google.gson.JsonElement?,
        typeOfT: Type?,
        context: JsonDeserializationContext?
    ): PaymentStatusEnum? {
        val value = json?.asString
        return value?.let { PaymentStatusEnum.fromValue(it) }
    }
}

class TransactionTypeDeserializer : JsonDeserializer<TransactionTypeEnum> {
    override fun deserialize(
        json: com.google.gson.JsonElement?,
        typeOfT: Type?,
        context: JsonDeserializationContext?
    ): TransactionTypeEnum? {
        val value = json?.asString
        return value?.let { TransactionTypeEnum.fromValue(it) }
    }
}



