package com.thellex.pay.core.utils

import com.thellex.pay.features.wallet.model.GroupedWalletAssetDto
import com.thellex.pay.features.wallet.model.WalletState

fun WalletState.assetsForChain(chainKey: String): List<GroupedWalletAssetDto> {
    return wallets[chainKey]?.assets.orEmpty()
}