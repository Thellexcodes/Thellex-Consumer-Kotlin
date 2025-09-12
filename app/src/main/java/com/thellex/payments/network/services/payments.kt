package com.thellex.payments.network.services

import com.thellex.payments.core.utils.Constants
import com.thellex.payments.data.model.ApiResponse
import com.thellex.payments.data.model.CreateBankAccountDto
import com.thellex.payments.data.model.CreateRequestPaymentDto
import com.thellex.payments.data.model.CryptoToFiatOffRampRequestDto
import com.thellex.payments.data.model.FiatToCryptoOnRampRequestDto
import com.thellex.payments.data.model.IBankAccountDto
import com.thellex.payments.data.model.IFiatCryptoRampTransactionsDto
import com.thellex.payments.data.model.ITransactionHistoryDto
import com.thellex.payments.features.wallet.model.IRatesResponseDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface PaymentRequestService {
    @POST(Constants.Endpoints.WITHDRAW_CRYPTO)
    suspend fun withdrawCrypto(@Body request: CreateRequestPaymentDto): Response<ApiResponse<ITransactionHistoryDto>>

    @GET(Constants.Endpoints.RATES)
    suspend fun getRates(): ApiResponse<IRatesResponseDto>

    @POST(Constants.Endpoints.FIAT_TO_CRYPTO_ONRAMP)
    suspend fun fiatToCryptoOnRamp(@Body request: FiatToCryptoOnRampRequestDto): Response<ApiResponse<IFiatCryptoRampTransactionsDto>>

    @POST(Constants.Endpoints.CRYPTO_TO_FIAT_OFFRAMP)
    suspend fun cryptoToFiatOffRamp(@Body request: CryptoToFiatOffRampRequestDto): Response<ApiResponse<IFiatCryptoRampTransactionsDto>>

    @POST(Constants.Endpoints.ADD_BANK_ACCOUNT)
    suspend fun addBankAccount(@Body request: CreateBankAccountDto): Response<ApiResponse<IBankAccountDto>>
}