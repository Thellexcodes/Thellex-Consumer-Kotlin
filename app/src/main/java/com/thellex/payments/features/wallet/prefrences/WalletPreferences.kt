package com.thellex.payments.features.wallet.prefrences

import android.content.Context
import com.google.gson.Gson
import com.thellex.payments.features.wallet.model.WalletBalanceDto

class WalletManagerPreferences(context: Context) {
    private val prefs = context.getSharedPreferences("wallet_prefs", Context.MODE_PRIVATE)
    private val gson = Gson()

    companion object {
        private const val KEY_WALLET_BALANCE = "wallet_balance"
        private const val KEY_WALLET_BALANCE_SAVE_TIME = "wallet_balance_save_time"
    }

    fun saveWalletBalance(response: WalletBalanceDto) {
        val json = gson.toJson(response)
        prefs.edit()
            .putString(KEY_WALLET_BALANCE, json)
            .putLong(KEY_WALLET_BALANCE_SAVE_TIME, System.currentTimeMillis())
            .apply()
    }

    fun getWalletBalance(): WalletBalanceDto? {
        val json = prefs.getString("wallet_balance", null)
        return json?.let { gson.fromJson(it, WalletBalanceDto::class.java) }
    }

    fun getWalletBalanceSaveTime(): Long {
        return prefs.getLong(KEY_WALLET_BALANCE_SAVE_TIME, 0L)
    }

    fun hasFetchedWallet(): Boolean {
        return prefs.contains(KEY_WALLET_BALANCE)
    }

    fun clearWalletCache() {
        prefs.edit()
            .remove(KEY_WALLET_BALANCE)
            .remove(KEY_WALLET_BALANCE_SAVE_TIME)
            .apply()
    }
}
