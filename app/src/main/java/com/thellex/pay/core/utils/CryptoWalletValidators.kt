package com.thellex.pay.core.utils

import com.thellex.pay.settings.SupportedBlockchainEnum

fun isValidWalletAddress(address: String, chain: SupportedBlockchainEnum?): Boolean {
    if (address.isBlank()) return false

    return when (chain) {
        SupportedBlockchainEnum.ethereum, SupportedBlockchainEnum.matic-> {
            // Ethereum-style addresses start with 0x and are 42 characters long
            address.startsWith("0x") && address.length == 42
        }
        SupportedBlockchainEnum.celo -> {
            // Celo addresses are similar to Ethereum
            address.startsWith("0x") && address.length == 42
        }
        SupportedBlockchainEnum.bep20 -> {
            // Binance Smart Chain addresses are also like Ethereum
            address.startsWith("0x") && address.length == 42
        }
        SupportedBlockchainEnum.starknet -> {
            // StarkNet addresses are hex but can be longer; adjust if needed
            address.startsWith("0x") && address.length >= 40
        }
        else -> false
    }
}
