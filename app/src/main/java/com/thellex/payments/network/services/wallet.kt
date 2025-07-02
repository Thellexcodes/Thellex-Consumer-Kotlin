package com.thellex.payments.network.services

import com.google.gson.JsonDeserializationContext
import com.thellex.payments.data.model.ApiResponse
import com.thellex.payments.core.utils.Constants
import com.thellex.payments.features.wallet.model.WalletBalanceDto
import com.thellex.payments.settings.SupportedBlockchainEnum
import retrofit2.http.GET
import com.google.gson.JsonDeserializer
import java.lang.reflect.Type

interface WalletManagerService {
    @GET(Constants.WALLET_MANAGER_BALANCE_ENDPOINT)
    suspend fun fetchBalance(): ApiResponse<WalletBalanceDto>
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

