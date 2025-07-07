package com.thellex.payments.network.services

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.thellex.payments.core.utils.Constants
import com.thellex.payments.data.model.ApiResponse
import com.thellex.payments.data.model.INotificationConsumeDto
import com.thellex.payments.data.model.NotificationKindEnum
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path
import java.lang.reflect.Type

interface NotificationService {
    @PATCH(Constants.NOTIFICATION_CONSUME_ENDPOINT)
    suspend fun consume(@Path("id") id: String): Response<ApiResponse<INotificationConsumeDto>>
}

class NotificationKindEnumDeserializer : JsonDeserializer<NotificationKindEnum> {
    override fun deserialize(
        json: JsonElement?,
        typeOfT: Type?,
        context: JsonDeserializationContext?
    ): NotificationKindEnum? {
        val value = json?.asString
        return value?.let { NotificationKindEnum.fromValue(it) }
    }
}