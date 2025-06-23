package com.thellex.payments.features.wallet.ui

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.thellex.payments.R
import com.thellex.payments.core.decorators.ItemSpacingDecoration
import com.thellex.payments.core.utils.Helpers
import com.thellex.payments.core.utils.Helpers.formatDecimal
import com.thellex.payments.features.auth.viewModel.UserViewModel
import com.thellex.payments.features.auth.viewModel.UserViewModelFactory
import com.thellex.payments.features.pos.adapters.Asset
import com.thellex.payments.features.pos.adapters.AssetAdapter
import com.thellex.payments.features.wallet.model.WalletManagerModelFactory
import com.thellex.payments.features.wallet.model.WalletManagerViewModel
import com.thellex.payments.features.wallet.prefrences.WalletManagerPreferences
import java.util.Locale


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

        observeWalletData()
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

    private fun observeWalletData() {
        walletManagerViewModel.walletBalance.observe(this) { walletDto ->
            textTotalBalance.text = formatDecimal(walletDto.totalInUsd.toString())
            val updatedAssets = walletDto.wallets.values.map { wallet ->
                Asset(
                    symbol = wallet.assetCode.toString().uppercase(Locale.getDefault()) ?: "N/A",
                    amount = formatDecimal(wallet.totalBalance),
                    usdValue = formatDecimal(wallet.totalBalance),
                    valueInLocal = formatDecimal("0.00"),
                    iconResId = Helpers.getIconResIdForToken(wallet.assetCode.toString() ?: "unknown")
                )
            }
            walletAssetsAdapter.updateData(updatedAssets)
        }
    }
}
