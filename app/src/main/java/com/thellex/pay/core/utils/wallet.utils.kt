package com.thellex.pay.core.utils

import com.thellex.pay.features.wallet.model.GroupedWalletAssetDto
import com.thellex.pay.features.wallet.model.WalletBalanceDto

fun WalletBalanceDto.assetsForChain(chainKey: String): List<GroupedWalletAssetDto> {
    return wallets[chainKey]?.assets.orEmpty()
}