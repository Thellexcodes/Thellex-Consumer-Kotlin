package com.thellex.pay.features.wallet.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.asFlow
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.thellex.pay.R
import com.thellex.pay.core.decorators.ItemSpacingDecoration
import com.thellex.pay.core.utils.ActivityTracker
import com.thellex.pay.core.utils.Helpers
import com.thellex.pay.core.utils.Helpers.formatDecimal
import com.thellex.pay.databinding.ActivityWalletAssetsBinding
import com.thellex.pay.features.auth.viewModel.UserViewModel
import com.thellex.pay.features.auth.viewModel.UserViewModelFactory
import com.thellex.pay.features.pos.adapters.Asset
import com.thellex.pay.features.pos.adapters.AssetAdapter
import com.thellex.pay.features.wallet.utils.WalletManagerModelFactory
import com.thellex.pay.features.wallet.utils.WalletManagerViewModel
import com.thellex.pay.features.wallet.prefrences.WalletManagerPreferences
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch

class WalletAssetsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityWalletAssetsBinding
    private lateinit var topBar: Helpers.TopAppBarController
    private lateinit var userViewModel: UserViewModel
    private lateinit var walletViewModel: WalletManagerViewModel
    private lateinit var walletPreferences: WalletManagerPreferences
    private lateinit var assetAdapter: AssetAdapter

    private val assetsList = mutableListOf<Asset>()
    private var isBalanceVisible = false
    private var actualBalance = "0.00 USD"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityWalletAssetsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ActivityTracker.add(this)

        setupViewModels()
        setupToolbar()
        setupBalanceVisibilityToggle()
        setupRecyclerView()
        observeWalletData()
    }

    private fun setupViewModels() {
        walletViewModel = ViewModelProvider(
            this, WalletManagerModelFactory(applicationContext)
        )[WalletManagerViewModel::class.java]

        userViewModel = ViewModelProvider(
            this, UserViewModelFactory(applicationContext)
        )[UserViewModel::class.java]

        walletPreferences = walletViewModel.getWalletPreferences()
    }

    private fun setupToolbar() {
        topBar = Helpers.setupTopAppBar(
            activity = this,
            rootView = findViewById(R.id.walletAssetsTopAppBar),
            title = ""
        )
    }

    private fun setupBalanceVisibilityToggle() {
        binding.activityWalletBalanceAmount.text = "****"
        binding.activityWalletToggleBalanceVisibility.setImageResource(R.drawable.icon_eye_closed_svg)

        binding.activityWalletToggleBalanceVisibility.setOnClickListener {
            toggleBalanceVisibility()
        }
    }

    private fun toggleBalanceVisibility() {
        isBalanceVisible = !isBalanceVisible
        binding.activityWalletToggleBalanceVisibility.setImageResource(
            if (isBalanceVisible) R.drawable.icon_eye_open else R.drawable.icon_eye_closed_svg
        )
        binding.activityWalletBalanceAmount.text = if (isBalanceVisible) actualBalance else "****"

        // Update asset adapter with toggled visibility
        assetAdapter.setBalanceVisibility(isBalanceVisible)
    }

    private fun setupRecyclerView() {
        assetAdapter = AssetAdapter(
            assets = assetsList,
            isBalanceVisible = isBalanceVisible,
            onItemClick = { asset -> },
            onActivateWalletClick = { asset, onComplete ->
                activateWalletForAsset(asset) { success ->
                    onComplete(success)
                }
            }
        )

        binding.activityWalletAssetsRecycler.apply {
            layoutManager = LinearLayoutManager(this@WalletAssetsActivity)
            adapter = assetAdapter
            val spacing = resources.getDimensionPixelSize(R.dimen.margin_2dp)
            addItemDecoration(ItemSpacingDecoration(spacing))
        }
    }

    private fun activateWalletForAsset(asset: Asset, onComplete: (Boolean) -> Unit) {
        loadWalletData("activate")
        onComplete(true)
    }

    private fun loadWalletData(action: String?) {
        lifecycleScope.launch {
            val token = userViewModel.token.asFlow().firstOrNull { !it.isNullOrBlank() }
            token?.let {
                walletViewModel.loadWallet(tokenProvider = { it }, loadNow = true, action)
            }
        }
    }

    private fun observeWalletData() {
        walletViewModel.walletBalance.observe(this) { walletDto ->
            val formattedBalance = formatDecimal((walletDto?.totalInUsd ?: "0").toString())
            actualBalance = "$formattedBalance USD"

            binding.activityWalletBalanceAmount.text = if (isBalanceVisible) actualBalance else "****"

            val updatedAssets = walletDto?.wallets?.values?.map { wallet ->
                val symbol = wallet.assetCode.name.uppercase() ?: "N/A"
                val amount = formatDecimal("${wallet.totalBalance}")
                val usdValue = formatDecimal("${wallet.totalBalance}")
                val valueInLocal = formatDecimal("${wallet.valueInLocal}")
//
                Asset(
                    symbol = symbol,
                    amount = amount,
                    usdValue = usdValue,
                    valueInLocal = valueInLocal,
                    iconResId = Helpers.getIconResIdForToken(wallet.assetCode.toString() ?: "unknown"),
                    address = wallet.address ?: "N/A"
                )
            } ?: emptyList()

            assetAdapter.updateData(updatedAssets)
        }
    }

    companion object {
        private const val TAG = "WalletAssetsActivity"
    }
}
