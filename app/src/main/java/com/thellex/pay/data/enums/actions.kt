package com.thellex.pay.data.enums

enum class OnOffRampAction(val routeValue: String) {
    FIAT_TO_CRYPTO_ON_RAMP("on_ramp"),
    CRYPTO_TO_FIAT_OFF_RAMP("off_ramp");

    companion object {
        fun fromRoute(value: String?): OnOffRampAction =
            entries.firstOrNull { it.routeValue == value }
                ?: FIAT_TO_CRYPTO_ON_RAMP
    }
}
