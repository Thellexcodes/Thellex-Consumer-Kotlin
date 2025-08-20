package com.thellex.payments.network.services

import com.thellex.payments.core.utils.Constants
import com.thellex.payments.data.model.ApiResponse
import com.thellex.payments.data.model.BasicKycFormModelDto
import com.thellex.payments.data.model.KycResponseDto
import com.thellex.payments.data.model.KycValidateBvnResponse
import com.thellex.payments.data.model.SubmitBvnDto
import com.thellex.payments.data.model.VerifySelfieWithPhotoIdDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface KycService {
    @POST(Constants.KYC_ENDPOINT)
    suspend fun verifyBasic(@Body request: BasicKycFormModelDto): Response<ApiResponse<KycResponseDto>>

    @POST(Constants.KYC_VERIFY_SELFIE_AND_DOCUMENT)
    suspend fun verifySelfieWithPhotoId(@Body request: VerifySelfieWithPhotoIdDto): Response<ApiResponse<KycResponseDto>>

    @POST(Constants.KYC_VERIFY_BVN)
    suspend fun  submitBvnAndPhone(@Body request: SubmitBvnDto): Response<ApiResponse<KycValidateBvnResponse>>
}