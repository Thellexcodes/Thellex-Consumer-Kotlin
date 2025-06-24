package com.thellex.payments.network.services

import com.thellex.payments.data.model.ApiResponse
import com.thellex.payments.core.utils.Constants
import com.thellex.payments.data.model.AccessResponse
import com.thellex.payments.data.model.LoginRequestDto
import com.thellex.payments.data.model.UserEntity
import com.thellex.payments.data.model.VerifyUserDto
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.Response

interface AuthService {
    @POST(Constants.LOGIN_ENDPOINT)
    suspend fun loginUser(@Body request: LoginRequestDto): Response<ApiResponse<AccessResponse>>

    @POST(Constants.VERIFY_CODE_ENDPOINT)
    suspend fun verifyCode(@Body request: VerifyUserDto): Response<ApiResponse<UserEntity>>

    @POST(Constants.AUTH_LOGIN_ENDPOINT)
    suspend fun checkAuthStatus(): Response<ApiResponse<UserEntity>>
}
