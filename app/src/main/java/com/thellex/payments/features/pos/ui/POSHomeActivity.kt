package com.thellex.payments.features.pos.ui

import com.thellex.payments.features.auth.viewModel.UserViewModel
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.asFlow
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.thellex.payments.core.decorators.ItemSpacingDecoration
import com.thellex.payments.features.pos.adapters.POSTransactionAdapter
import com.thellex.payments.R
import com.thellex.payments.core.utils.Helpers.parseDate
import com.thellex.payments.data.model.UserPreferences
import com.thellex.payments.databinding.ActivityPOSBinding
import com.thellex.payments.settings.PaymentType
import com.thellex.payments.features.auth.viewModel.UserViewModelFactory
import com.thellex.payments.features.wallet.model.WalletManagerModelFactory
import com.thellex.payments.features.wallet.model.WalletManagerViewModel
import com.thellex.payments.features.wallet.ui.WalletAssetsActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class POSHomeActivity : AppCompatActivity() {
    private lateinit var binding: ActivityPOSBinding
    private lateinit var userViewModel: UserViewModel
    private lateinit var walletManagerViewModel: WalletManagerViewModel
    private lateinit var transactionRecyclerView: RecyclerView
    private lateinit var transactionAdapter: POSTransactionAdapter
    private var isBalanceVisible = false
    private var currentBalance = "0.00"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPOSBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupWindowInsetsAndBars()

        userViewModel = ViewModelProvider(
            this,
            UserViewModelFactory(applicationContext)
        )[UserViewModel::class.java]

        walletManagerViewModel = ViewModelProvider(
            this,
            WalletManagerModelFactory(applicationContext)
        )[WalletManagerViewModel::class.java]

        setupRecyclerView()
        observeUserTransactions()
        setupClickListeners()
        setupWalletBalanceObserver()
        loadWalletData()
        observeUserUid()
    }

    private fun setupWindowInsetsAndBars() {
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content)) { view, insets ->
            val systemBarsInsets = insets.getInsets(WindowInsetsCompat.Type.systemBars())

            view.setPadding(
                view.paddingLeft,
                systemBarsInsets.top,
                view.paddingRight,
                systemBarsInsets.bottom
            )

            insets
        }

        val window = window
        WindowCompat.setDecorFitsSystemWindows(window, true)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.statusBarColor = Color.parseColor("#212C2C3A")
        }
    }

    private fun setupRecyclerView() {
        transactionRecyclerView = binding.transactionRecycler
        transactionRecyclerView.layoutManager = LinearLayoutManager(this)

        transactionAdapter = POSTransactionAdapter(emptyList()) {}
        transactionRecyclerView.adapter = transactionAdapter

        val itemSpacing = resources.getDimensionPixelSize(R.dimen.txn_margin)
        transactionRecyclerView.addItemDecoration(ItemSpacingDecoration(itemSpacing))
    }

    private fun loadWalletData() {
        walletManagerViewModel.loadWallet {
            userViewModel.token.asFlow().first { !it.isNullOrBlank() }
        }
    }

    private fun setupWalletBalanceObserver() {
        walletManagerViewModel.walletBalance.observe(this) { walletDto ->
            walletDto?.totalInUsd?.let { totalUsd ->
                currentBalance = totalUsd.toString()
                updateBalanceText(currentBalance)
            }
        }

        binding.ivToggleBalance.setOnClickListener {
            isBalanceVisible = !isBalanceVisible
            updateBalanceText(currentBalance)

            val iconRes = if (isBalanceVisible) R.drawable.icon_eye_open else R.drawable.icon_eye_open
            binding.ivToggleBalance.setImageResource(iconRes)
        }
    }

    private fun observeUserUid() {
        userViewModel.authResult.observe(this) { userDto ->
            val upperUid = userDto?.uid?.toString()?.uppercase() ?: "N/A"
            binding.activityPosUserUidText.text = "UID: $upperUid"
        }
    }


    private fun updateBalanceText(balance: String) {
        binding.tvBalance.text = if (isBalanceVisible) "$$balance" else "•••••"
    }

    private fun observeUserTransactions() {
        lifecycleScope.launch {
            UserPreferences.getAuthResult(applicationContext).collect { userEntity ->
                val transactions = userEntity?.transactionHistory ?: emptyList()
                val sortedTransactions = transactions.sortedByDescending { parseDate(it.createdAt) }
                withContext(Dispatchers.Main) {
                    transactionAdapter.updateList(sortedTransactions)
                }
            }
        }
    }

    private fun setupClickListeners() {
        binding.posWithdrawButton.setOnClickListener {
            startActivity(Intent(this, EnterTransactionAmountActivity::class.java).apply {
                putExtra("type", PaymentType.WITHDRAW_FIAT)
            })
        }

        binding.posRequestButton.setOnClickListener {
            startActivity(Intent(this, POSChooseCryptoActivity::class.java))
        }

        binding.posQuickRequestButton.setOnClickListener {
            startActivity(Intent(this, GeneratePOSAddressActivity::class.java))
        }

        binding.posViewAssetsButton.setOnClickListener {
            startActivity(Intent(this, WalletAssetsActivity::class.java))
        }
    }
}
