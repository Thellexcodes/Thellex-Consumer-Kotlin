package com.thellex.payments.network.services

import com.thellex.payments.data.model.ApiResponse
import com.thellex.payments.core.utils.Constants
import com.thellex.payments.features.wallet.model.WalletManagerBalanceResponse
import retrofit2.http.GET

interface WalletManagerService {
    @GET(Constants.WALLET_MANAGER_BALANCE_ENDPOINT)
    suspend fun fetchBalance(): ApiResponse<WalletManagerBalanceResponse>
}

