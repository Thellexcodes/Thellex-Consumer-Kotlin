package com.thellex.payments.network.services

import com.thellex.payments.core.utils.Constants
import com.thellex.payments.data.model.ApiResponse
import com.thellex.payments.data.model.CreateBankAccountDto
import com.thellex.payments.data.model.CreateRequestPaymentDto
import com.thellex.payments.data.model.CryptoToFiatOffRampRequestDto
import com.thellex.payments.data.model.FiatToCryptoOnRampRequestDto
import com.thellex.payments.data.model.IBankAccountDto
import com.thellex.payments.data.model.IFiatCryptoRampTransactionsDto
import com.thellex.payments.data.model.ITransactionHistoryEntity
import com.thellex.payments.features.wallet.model.IRatesResponseDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface PaymentRequestService {
    @POST(Constants.WITHDRAW_CRYPTO_PAYMENT_ENDPOINT)
    suspend fun withdrawCrypto(@Body request: CreateRequestPaymentDto): Response<ApiResponse<ITransactionHistoryEntity>>

    @GET(Constants.WALLET_MANAGER_RATES_ENDPOINT)
    suspend fun getRates(): ApiResponse<IRatesResponseDto>

    @POST(Constants.FIAT_TO_CRYPTO_ONRAMP_ENDPOINT)
    suspend fun fiatToCryptoOnRamp(@Body request: FiatToCryptoOnRampRequestDto): Response<ApiResponse<IFiatCryptoRampTransactionsDto>>

    @POST(Constants.CRYPTO_TO_FIAT_OFFRAMP_ENDPOINT)
    suspend fun cryptoToFiatOffRamp(@Body request: CryptoToFiatOffRampRequestDto): Response<ApiResponse<IFiatCryptoRampTransactionsDto>>

    @POST(Constants.ADD_BANK_ACCOUNT_ENDPOINT)
    suspend fun addBankAccount(@Body request: CreateBankAccountDto): Response<ApiResponse<IBankAccountDto>>
}