package com.thellex.payments.core.utils.deserializers

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.thellex.payments.data.model.NotificationKindEnum
import java.lang.reflect.Type

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