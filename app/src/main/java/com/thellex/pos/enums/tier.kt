package com.thellex.pos.enums

enum class TierEnum(val value: String) {
    NONE("none"),
    BASIC("basic"),
    PERSONAL("personal"),
    PROFESSIONAL("professional"),
    BUSINESS("business"),
    ENTERPRISE("enterprise");

    companion object {
        fun fromValue(value: String): TierEnum {
            return values().find { it.value == value } ?: NONE
        }
    }
}
