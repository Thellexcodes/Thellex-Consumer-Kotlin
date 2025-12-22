package com.thellex.pay.core.routes

object ComposeRoutes {

    val WalletHome = Route("wallet_home")

    val AssetDetail = Route("asset_detail")

    val PaymentLinks = Route("payment_links")

    val OnOffRamp = Route("on_off_ramp")

    val RampTransactions = Route("ramp_transactions")
    val RampTransactionDetail = Route("ramp_transaction_detail")

    val CryptoWithdrawal = Route("crypto_withdrawal")
    val CryptoWithdrawalReview = Route("crypto_withdrawal_review")

    val SecuritySettings = Route("security_settings")

    val WebView = Route("webview")

    data class Route(val route: String)
}
