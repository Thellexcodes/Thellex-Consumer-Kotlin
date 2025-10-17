package com.thellex.pay.core.utils.deserializers

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.thellex.pay.data.model.TransactionTypeEnum
import java.lang.reflect.Type

class TransactionTypeEnumDeserializer : JsonDeserializer<TransactionTypeEnum?> {
    override fun deserialize(json: JsonElement?, typeOfT: Type?, context: JsonDeserializationContext?): TransactionTypeEnum? {
        val value = json?.asString ?: return null
        return TransactionTypeEnum.fromValue(value)
    }
}