package com.thellex.pos.network.services

import com.thellex.pos.data.model.ApiResponse
import com.thellex.pos.core.utils.Constants
import com.thellex.pos.data.model.CreateRequestPaymentDto
import com.thellex.pos.data.model.RequestPaymentResponse
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.Response

interface PaymentRequestService {
    @POST(Constants.REQUEST_CRYPTO_PAYMENT_ENDPOINT)
    suspend fun requestCryptoPayment(@Body request: CreateRequestPaymentDto): ApiResponse<RequestPaymentResponse>

    @POST(Constants.REQUEST_FIAT_PAYMENT_ENDPOINT)
    suspend fun requestFiatPayment(@Body request: CreateRequestPaymentDto): Response<RequestPaymentResponse>
}

