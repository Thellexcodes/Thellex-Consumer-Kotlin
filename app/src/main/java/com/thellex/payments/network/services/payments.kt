package com.thellex.payments.network.services

import com.thellex.payments.core.utils.Constants
import com.thellex.payments.data.model.ApiResponse
import com.thellex.payments.data.model.CreateRequestPaymentDto
import com.thellex.payments.data.model.FiatToCryptoOnRampRequestDto
import com.thellex.payments.data.model.IFiatCryptoRampTransactionsDto
import com.thellex.payments.data.model.ITransactionHistoryEntity
import com.thellex.payments.features.wallet.model.RatesResponseDto
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface PaymentRequestService {
    @POST(Constants.WITHDRAW_CRYPTO_PAYMENT_ENDPOINT)
    suspend fun withdrawCrypto(@Body request: CreateRequestPaymentDto): ApiResponse<ITransactionHistoryEntity>

    @GET(Constants.WALLET_MANAGER_RATES_ENDPOINT)
    suspend fun getRates(): ApiResponse<RatesResponseDto>

    @POST(Constants.FIAT_TO_CRYPTO_ONRAMP_ENDPOINT)
    suspend fun fiatToCryptoOnRamp(@Body request: FiatToCryptoOnRampRequestDto): ApiResponse<IFiatCryptoRampTransactionsDto>
}