package com.thellex.pay.network.services

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.thellex.pay.data.model.ApiResponse
import com.thellex.pay.core.utils.Constants
import com.thellex.pay.data.enums.TierEnum
import com.thellex.pay.data.model.AccessResponse
import com.thellex.pay.data.model.DeviceRequestDto
import com.thellex.pay.data.model.LoginRequestDto
import com.thellex.pay.data.model.UserEntity
import com.thellex.pay.data.model.VerifyUserDto
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.GET
import retrofit2.Response
import java.lang.reflect.Type

interface AuthService {
    @POST(Constants.Endpoints.LOGIN)
    suspend fun loginUser(@Body request: LoginRequestDto): Response<ApiResponse<AccessResponse>>

    @POST(Constants.Endpoints.VERIFY_CODE)
    suspend fun verifyCode(@Body request: VerifyUserDto): Response<ApiResponse<UserEntity>>

    @POST(Constants.Endpoints.AUTH_LOGIN)
    suspend fun checkAuthStatus(): Response<ApiResponse<UserEntity>>

    @GET(Constants.Endpoints.SAVE_DEVICE_INFO)
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