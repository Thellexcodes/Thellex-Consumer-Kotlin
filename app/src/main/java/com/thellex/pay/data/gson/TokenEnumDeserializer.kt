package com.thellex.pay.data.gson

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.thellex.pay.settings.SupportedBlockchainEnum
import com.thellex.pay.settings.TokenEnum
import java.lang.reflect.Type

class TokenEnumDeserializer : JsonDeserializer<TokenEnum> {
    override fun deserialize(
        json: JsonElement?,
        typeOfT: Type?,
        context: JsonDeserializationContext?
    ): TokenEnum? {
        val value = json?.asString
        return value?.let { TokenEnum.fromValue(it) }
    }
}