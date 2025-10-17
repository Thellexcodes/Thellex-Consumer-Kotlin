package com.thellex.pay.network.services

import com.thellex.pay.core.utils.Constants
import com.thellex.pay.data.model.ApiResponse
import com.thellex.pay.data.model.BasicKycFormModelDto
import com.thellex.pay.data.model.KycResponseDto
import com.thellex.pay.data.model.KycValidateBvnResponse
import com.thellex.pay.data.model.SubmitBvnDto
import com.thellex.pay.data.model.VerifySelfieWithPhotoIdDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface KycService {
    @POST(Constants.Endpoints.KYC)
    suspend fun verifyBasic(@Body request: BasicKycFormModelDto): Response<ApiResponse<KycResponseDto>>

    @POST(Constants.Endpoints.KYC_VERIFY_SELFIE_AND_DOC)
    suspend fun verifySelfieWithPhotoId(@Body request: VerifySelfieWithPhotoIdDto): Response<ApiResponse<KycResponseDto>>

    @POST(Constants.Endpoints.KYC_VERIFY_BVN)
    suspend fun  submitBvnAndPhone(@Body request: SubmitBvnDto): Response<ApiResponse<KycValidateBvnResponse>>
}