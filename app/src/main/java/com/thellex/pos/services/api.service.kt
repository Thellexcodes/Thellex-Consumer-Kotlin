package com.thellex.pos.services

import com.thellex.pos.data.model.Constants
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.Response

interface ApiService {
    @POST(Constants.LOGIN_ENDPOINT)
    suspend fun loginUser(@Body request: LoginRequestDto): Response<LoginResponse>

    @POST(Constants.VERIFY_CODE_ENDPOINT)
    suspend fun verifyCode(
        @Body request: VerifyUserDto
    ): Response<VerifyCodeResponse>

    @POST(Constants.AUTH_LOGIN_ENDPOINT)
    suspend fun checkAuthStatus(): Response<AuthResponse>
}
