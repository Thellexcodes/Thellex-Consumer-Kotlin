package com.thellex.pos.services

import com.thellex.pos.data.model.ApiResponse
import com.thellex.pos.data.model.Constants
import com.thellex.pos.data.model.LoginRequestDto
import com.thellex.pos.data.model.LoginResponse
import com.thellex.pos.data.model.UserEntity
import com.thellex.pos.data.model.VerifyCodeResponse
import com.thellex.pos.data.model.VerifyUserDto
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.Response

interface AuthService {
    @POST(Constants.LOGIN_ENDPOINT)
    suspend fun loginUser(@Body request: LoginRequestDto): Response<LoginResponse>

    @POST(Constants.VERIFY_CODE_ENDPOINT)
    suspend fun verifyCode(
        @Body request: VerifyUserDto
    ): Response<VerifyCodeResponse>

    @POST(Constants.AUTH_LOGIN_ENDPOINT)
    suspend fun checkAuthStatus(): Response<ApiResponse<UserEntity>>
}
