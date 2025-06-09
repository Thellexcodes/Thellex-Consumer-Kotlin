package com.thellex.pos.network.services

import com.thellex.pos.data.model.ApiResponse
import com.thellex.pos.core.utils.Constants
import com.thellex.pos.data.model.LoginRequestDto
import com.thellex.pos.data.model.UserEntity
import com.thellex.pos.data.model.VerifyUserDto
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.Response

interface AuthService {
    @POST(Constants.LOGIN_ENDPOINT)
    suspend fun loginUser(@Body request: LoginRequestDto): Response<ApiResponse<String>>

    @POST(Constants.VERIFY_CODE_ENDPOINT)
    suspend fun verifyCode(@Body request: VerifyUserDto): Response<ApiResponse<Boolean>>

    @POST(Constants.AUTH_LOGIN_ENDPOINT)
    suspend fun checkAuthStatus(): Response<ApiResponse<UserEntity>>
}
