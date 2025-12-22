package com.thellex.pay.data.model

data class ChainInfo(
    val id: String,
    val displayName: String,
    val fee: String,
    val minimumWithdrawal: String,
    val arrivalTime: String
)