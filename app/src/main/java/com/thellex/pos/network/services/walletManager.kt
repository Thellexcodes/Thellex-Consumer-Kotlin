package com.thellex.pos.network.services

import com.thellex.pos.data.model.ApiResponse
import com.thellex.pos.core.utils.Constants
import com.thellex.pos.features.wallet.model.WalletManagerBalanceResponse
import retrofit2.http.GET

interface WalletManagerService {
    @GET(Constants.WALLET_MANAGER_BALANCE_ENDPOINT)
    suspend fun fetchBalance(): ApiResponse<WalletManagerBalanceResponse>
}

