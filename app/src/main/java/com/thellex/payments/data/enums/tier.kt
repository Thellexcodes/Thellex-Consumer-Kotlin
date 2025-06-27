package com.thellex.payments.data.enums

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
