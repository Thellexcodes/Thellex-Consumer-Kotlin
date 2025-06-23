package com.thellex.payments.features.wallet.ui

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.thellex.payments.R
import com.thellex.payments.core.decorators.ItemSpacingDecoration
import com.thellex.payments.features.auth.viewModel.UserViewModel
import com.thellex.payments.features.auth.viewModel.UserViewModelFactory
import com.thellex.payments.features.pos.adapters.Asset
import com.thellex.payments.features.pos.adapters.AssetAdapter
import com.thellex.payments.features.wallet.model.WalletEntry
import com.thellex.payments.features.wallet.model.WalletManagerBalanceResponse
import com.thellex.payments.features.wallet.model.WalletManagerModelFactory
import com.thellex.payments.features.wallet.model.WalletManagerViewModel
import com.thellex.payments.features.wallet.prefrences.WalletManagerPreferences
import com.thellex.payments.network.services.ApiClient
import com.thellex.payments.core.utils.Helpers
import com.thellex.payments.core.utils.Helpers.formatDecimal
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull

class WalletAssetsActivity : AppCompatActivity() {
    private lateinit var userViewModel: UserViewModel
    private lateinit var walletManagerViewModel: WalletManagerViewModel
    private lateinit var walletPreferences: WalletManagerPreferences
    private lateinit var walletAssetsAdapter: AssetAdapter
    private lateinit var textTotalBalance: TextView
    private val assetsList = mutableListOf<Asset>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_wallet_assets)

        setupViewModels()
        walletPreferences = walletManagerViewModel.getWalletPreferences()

        setupRecyclerView()
        textTotalBalance = findViewById(R.id.activity_wallet_balance_amount)

        loadWalletData()
    }

    private fun setupViewModels() {
        walletManagerViewModel = ViewModelProvider(
            this,
            WalletManagerModelFactory(applicationContext)
        )[WalletManagerViewModel::class.java]

        userViewModel = ViewModelProvider(
            this,
            UserViewModelFactory(applicationContext)
        )[UserViewModel::class.java]
    }

    private fun setupRecyclerView() {
        walletAssetsAdapter = AssetAdapter(assetsList) { asset ->
            val intent = Intent(this, AssetBalanceActivity::class.java)
            intent.putExtra("asset_symbol", asset.symbol)
            startActivity(intent)
        }

        val recyclerViewWalletAssets = findViewById<RecyclerView>(R.id.activity_wallet_assets_recycler)
        recyclerViewWalletAssets.layoutManager = LinearLayoutManager(this)
        recyclerViewWalletAssets.adapter = walletAssetsAdapter

        val itemSpacing = resources.getDimensionPixelSize(R.dimen.margin_2dp)
        recyclerViewWalletAssets.addItemDecoration(ItemSpacingDecoration(itemSpacing))
    }

    private fun loadWalletData() {
        val cachedWallet = walletPreferences.getWalletBalance()
        if (cachedWallet != null) {
            updateUIFromResponse(cachedWallet)
        } else {
            fetchWalletBalances(forceRefresh = true)
        }
    }

    private fun updateUIFromResponse(response: WalletManagerBalanceResponse) {
        val currency = response.currency ?: "N/A"
        val totalBalance = response.totalBalance ?: "0"

        textTotalBalance.text = "$totalBalance $currency"

        val wallets = response.wallets
        if (wallets.isEmpty()) {
            walletAssetsAdapter.updateData(emptyList())
            return
        }

        val newAssets = wallets.map { wallet: WalletEntry ->
            Asset(
                symbol = wallet.assetCode?.uppercase() ?: "N/A",
                amount = formatDecimal(wallet.balanceInUsd ?: 0.0),
                usdValue = formatDecimal(wallet.balanceInUsd ?: 0.0),
                valueInLocal = formatDecimal(wallet.balanceInNgn ?: 0.0),
                iconResId = Helpers.getIconResIdForToken(wallet.assetCode ?: "")
            )
        }

        walletAssetsAdapter.updateData(newAssets)
    }

    private fun fetchWalletBalances(forceRefresh: Boolean) {
        lifecycleScope.launch {
            val token = withTimeoutOrNull(5000) {
                userViewModel.token.first { !it.isNullOrBlank() }
            }

            if (token.isNullOrBlank()) {
                return@launch
            }

            if (!forceRefresh) {
                // If not forced, maybe check last update time here (optional)
            }

            try {
                val api = ApiClient.getAuthenticatedWalletManagerApi(token)
                val response = api.fetchBalance()

                val walletBalanceResponse = response.result

                if (walletBalanceResponse == null || walletBalanceResponse.wallets.isEmpty()) {
                    return@launch
                }

                // Save fresh response to preferences
                walletPreferences.saveWalletBalance(walletBalanceResponse)

                withContext(Dispatchers.Main) {
                    updateUIFromResponse(walletBalanceResponse)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}