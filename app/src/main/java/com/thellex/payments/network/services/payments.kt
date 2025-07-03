package com.thellex.payments.network.services

import com.thellex.payments.core.utils.Constants
import com.thellex.payments.data.model.ApiResponse
import com.thellex.payments.data.model.CreateRequestPaymentDto
import com.thellex.payments.data.model.ITransactionHistoryEntity
import retrofit2.http.Body
import retrofit2.http.POST

interface PaymentRequestService {
    @POST(Constants.WITHDRAW_CRYPTO_PAYMENT_ENDPOINT)
    suspend fun withdrawCrypto(@Body request: CreateRequestPaymentDto): ApiResponse<ITransactionHistoryEntity>
}