package com.thellex.pay.data.enums

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import java.lang.reflect.Type

enum class RoleEnum(val value: String) {
    SUPER_ADMIN("SUPER_ADMIN"),
    COMPANY_ADMIN("COMPANY_ADMIN"),
    MODERATOR("MODERATOR"),
    USER("USER");

    companion object {
        fun fromValue(value: String): RoleEnum? {
            return entries.firstOrNull { it.value.equals(value, ignoreCase = true) }
        }
    }
}

class RoleTypeDeserializer : JsonDeserializer<RoleEnum> {
    override fun deserialize(
        json: com.google.gson.JsonElement?,
        typeOfT: Type?,
        context: JsonDeserializationContext?
    ): RoleEnum? {
        val value = json?.asString
        return value?.let { RoleEnum.fromValue(it) }
    }
}