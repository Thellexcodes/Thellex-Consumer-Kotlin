package com.thellex.payments.data.model

data class Country(
    val name: String,
    val code: String,
    val flagUrl: String,
    val phoneLength: Int,
    val iso2: String
)
