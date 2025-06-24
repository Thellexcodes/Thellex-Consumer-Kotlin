package com.thellex.payments.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class Fee {
    @SerialName("flat")
    FLAT,

    @SerialName("percentage")
    PERCENTAGE
}
