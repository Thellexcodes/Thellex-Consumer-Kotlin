package com.thellex.payments.core.routes

object ComposeRoutes {
    val PaymentLinks = Route("payment_links")
    val RampTransactions = Route("ramp_transactions")
    val RampTransactionDetail = Route("ramp_transaction_detail")
    val WebView = Route("webview")

    data class Route(val route: String)
}