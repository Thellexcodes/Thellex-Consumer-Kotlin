package com.thellex.payments.features.wallet.ui

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.asFlow
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.thellex.payments.R
import com.thellex.payments.core.decorators.ItemSpacingDecoration
import com.thellex.payments.core.utils.Helpers
import com.thellex.payments.core.utils.Helpers.formatDecimal
import com.thellex.payments.databinding.ActivityWalletAssetsBinding
import com.thellex.payments.features.auth.viewModel.UserViewModel
import com.thellex.payments.features.auth.viewModel.UserViewModelFactory
import com.thellex.payments.features.pos.adapters.Asset
import com.thellex.payments.features.pos.adapters.AssetAdapter
import com.thellex.payments.features.wallet.utils.WalletManagerModelFactory
import com.thellex.payments.features.wallet.utils.WalletManagerViewModel
import com.thellex.payments.features.wallet.prefrences.WalletManagerPreferences
import kotlinx.coroutines.flow.first
import java.util.Locale

class WalletAssetsActivity : AppCompatActivity() {
    private lateinit var binding: ActivityWalletAssetsBinding
    private lateinit var userViewModel: UserViewModel
    private lateinit var walletManagerViewModel: WalletManagerViewModel
    private lateinit var walletPreferences: WalletManagerPreferences
    private lateinit var walletAssetsAdapter: AssetAdapter
    private val assetsList = mutableListOf<Asset>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityWalletAssetsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupViewModels()
        walletPreferences = walletManagerViewModel.getWalletPreferences()

        setupRecyclerView()
        observeWalletData()

        binding.backButton.setOnClickListener{
            finish()
        }
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
        walletAssetsAdapter = AssetAdapter(
            assetsList,
            onItemClick = {
                // Handle normal asset click here
//                val intent = Intent(this, AssetBalanceActivity::class.java)
//                intent.putExtra("asset_symbol", asset.symbol)
//                startActivity(intent)
            },
            onActivateWalletClick = { asset ->
                activateWalletForAsset(asset)
            }
        )

        binding.activityWalletAssetsRecycler.apply {
            layoutManager = LinearLayoutManager(this@WalletAssetsActivity)
            adapter = walletAssetsAdapter
            val itemSpacing = resources.getDimensionPixelSize(R.dimen.margin_2dp)
            addItemDecoration(ItemSpacingDecoration(itemSpacing))
        }
    }

    private fun activateWalletForAsset(asset: Asset) {
        loadWalletData()
    }

    private fun loadWalletData() {
        walletManagerViewModel.loadWallet(
            tokenProvider = { userViewModel.token.asFlow().first { !it.isNullOrBlank() } },
            loadNow = true
        )
    }

    private fun observeWalletData() {
        walletManagerViewModel.walletBalance.observe(this) { walletDto ->
            binding.activityWalletBalanceAmount.text = formatDecimal((walletDto?.totalInUsd ?: "0").toString())

            val updatedAssets = walletDto?.wallets?.values?.map { wallet ->
                Asset(
                    symbol = wallet.assetCode.toString().uppercase() ?: "N/A",
                    amount = formatDecimal(wallet.totalBalance),
                    usdValue = formatDecimal(wallet.totalBalance),
                    valueInLocal = formatDecimal(wallet.valueInLocal),
                    iconResId = Helpers.getIconResIdForToken(wallet.assetCode.toString() ?: "unknown"),
                    address = wallet.address
                )
            } ?: emptyList()

            walletAssetsAdapter.updateData(updatedAssets)
        }
    }

    companion object {
        private val TAG = "TAGad"
    }
}
