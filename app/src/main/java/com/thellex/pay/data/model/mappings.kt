package com.thellex.pay.data.model

import android.os.Build
import androidx.annotation.RequiresApi
import com.thellex.pay.core.utils.Helpers.formatTransactionTimeHumanReadable
import com.thellex.pay.features.notifications.ui.NotificationItem
import com.thellex.pay.features.wallet.ui.CryptoTransactionSummary
import com.thellex.pay.settings.SupportedBlockchainEnum
import com.thellex.pay.settings.TokenEnum
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

fun BaseSettingsCache.findChainIconForNetwork(
    network: SupportedBlockchainEnum
): String? {
    return chains.firstOrNull { it.id == network }?.iconUrl
}


data class ChainAndAssetIcons(
    val chainIconUrl: String?,
    val assetIconUrl: String?
)

fun BaseSettingsCache.findChainAndAssetIcons(
    network: SupportedBlockchainEnum,
    assetSymbol: String
): ChainAndAssetIcons {

    val chain = chains.firstOrNull { chain ->
        chain.id.name.equals(network.name, ignoreCase = true)
    }

    val assetIcon = chain
        ?.supportedTokens
        ?.firstOrNull { token ->
            token.symbol.name.equals(assetSymbol, ignoreCase = true)
        }
        ?.iconDisplay

    return ChainAndAssetIcons(
        chainIconUrl = chain?.iconUrl,
        assetIconUrl = assetIcon
    )
}

fun ITransactionHistoryDto.toCryptoTransactionSummary(transaction: ITransactionHistoryDto): CryptoTransactionSummary {
    return CryptoTransactionSummary(
        amount = transaction.amount,
        assetCode = TokenEnum.usdt,
        valueInUsd = transaction.valueInUsd,
        valueInLocal = transaction.valueInLocal,
        sourceAddress = transaction.sourceAddress,
        fundUid = transaction.destinationAddress,
        network = transaction.paymentNetwork,
        networkName = "Stellar",
        networkFee = 0.0,
        reason = transaction.reason
    )
}

@RequiresApi(Build.VERSION_CODES.O)
fun NotificationEntity.toNotificationItem(
    baseSettings: BaseSettingsCache?
): NotificationItem {

    val icons = baseSettings?.findChainAndAssetIcons(
        network = SupportedBlockchainEnum.stellar,
        assetSymbol = assetCode
    )

    return NotificationItem(
        id = id,
        title = title,
        message = message,
        date = formatTransactionTimeHumanReadable(createdAt),
        amount = amount,
        assetSymbol = assetCode,
        txnId = txnID,
        isConsumed = consumed,
        iconUrl = icons?.assetIconUrl
    )
}

