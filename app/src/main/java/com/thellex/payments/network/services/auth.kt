package com.thellex.payments.network.services

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.thellex.payments.data.model.ApiResponse
import com.thellex.payments.core.utils.Constants
import com.thellex.payments.data.enums.TierEnum
import com.thellex.payments.data.model.AccessResponse
import com.thellex.payments.data.model.DeviceRequestDto
import com.thellex.payments.data.model.LoginRequestDto
import com.thellex.payments.data.model.UserEntity
import com.thellex.payments.data.model.VerifyUserDto
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.Response
import java.lang.reflect.Type

interface AuthService {
    @POST(Constants.Endpoints.LOGIN)
    suspend fun loginUser(@Body request: LoginRequestDto): Response<ApiResponse<AccessResponse>>

    @POST(Constants.Endpoints.VERIFY_CODE)
    suspend fun verifyCode(@Body request: VerifyUserDto): Response<ApiResponse<UserEntity>>

    @POST(Constants.Endpoints.AUTH_LOGIN)
    suspend fun checkAuthStatus(): Response<ApiResponse<UserEntity>>

    @POST(Constants.Endpoints.SAVE_DEVICE_INFO)
    suspend fun updateFcmToken(@Body request: DeviceRequestDto): Response<ApiResponse<Unit>>
}

class TierEnumDeserializer : JsonDeserializer<TierEnum> {
    override fun deserialize(
        json: JsonElement?,
        typeOfT: Type?,
        context: JsonDeserializationContext?
    ): TierEnum? {
        val value = json?.asString
        return value?.let { TierEnum.fromValue(it) }
    }
}