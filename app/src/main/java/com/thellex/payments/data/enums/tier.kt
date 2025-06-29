package com.thellex.payments.data.enums

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer

import com.google.gson.JsonElement
import java.lang.reflect.Type

enum class TierEnum(val value: String) {
    NONE("none"),
    BASIC("basic"),
    PERSONAL("personal"),
    PROFESSIONAL("professional"),
    BUSINESS("business"),
    ENTERPRISE("enterprise");

    companion object {
        fun fromValue(value: String): TierEnum {
            return entries.firstOrNull { it.value.equals(value, ignoreCase = true) } ?: NONE
        }
    }
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


enum class BasicKycRequirementsEnum(val displayName: String) {
    ID_TYPE("ID Type"),
    ADDITIONAL_ID_TYPE("Additional ID Type"),
    FIRST_NAME("First Name"),
    MIDDLE_NAME("Middle Name"),
    LAST_NAME("Last Name"),
    PHONE_NUMBER("Phone Number"),
    DATE_OF_BIRTH("Date of Birth"),
    NIN("NIN (National ID Number)"),
    BVN("BVN (Bank Verification Number)"),
    HOUSE_NUMBER("House Number"),
    STREET_NAME("Street Name"),
    STATE("State"),
    LOCAL_GOVERNMENT_AREA("Local Government Area (LGA)");
}
