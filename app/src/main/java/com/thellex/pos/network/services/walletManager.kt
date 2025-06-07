package com.thellex.pos.network.services

import com.thellex.pos.data.model.Constants
import com.thellex.pos.data.model.WalletManagerBalanceResponse
import retrofit2.Response
import retrofit2.http.GET

interface WalletManagerService {
    @GET(Constants.WALLET_MANAGER_BALANCE_ENDPOINT)
    suspend fun balance(): Response<WalletManagerBalanceResponse>
}

