package com.thellex.pay.core.routes

object ComposeRoutes {
    val PaymentLinks = Route("payment_links")
    val RampTransactions = Route("ramp_transactions")
    val RampTransactionDetail = Route("ramp_transaction_detail")
    val SecuritySettings= Route("security_settings")
    val WebView = Route("webview")

    data class Route(val route: String)
}