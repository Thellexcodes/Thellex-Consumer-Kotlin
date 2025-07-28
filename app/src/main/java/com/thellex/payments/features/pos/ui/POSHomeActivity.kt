package com.thellex.payments.features.pos.ui

import com.thellex.payments.features.auth.viewModel.UserViewModel
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.asFlow
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.thellex.payments.core.decorators.ItemSpacingDecoration
import com.thellex.payments.features.pos.adapters.POSTransactionAdapter
import com.thellex.payments.R
import com.thellex.payments.core.utils.ActivityTracker
import com.thellex.payments.core.utils.Helpers.applyAdvancedSystemBarInsets
import com.thellex.payments.core.utils.Helpers.disableDecorFitsSystemWindows
import com.thellex.payments.core.utils.Helpers.setTransparentStatusBarWithWhiteIcons
import com.thellex.payments.data.model.UserPreferences
import com.thellex.payments.databinding.ActivityPOSBinding
import com.thellex.payments.features.auth.ui.AuthVerificationActivity
import com.thellex.payments.features.kyc.ui.basic.KycSuccessActivity
import com.thellex.payments.features.auth.ui.LoginActivity
import com.thellex.payments.features.auth.viewModel.UserViewModelFactory
import com.thellex.payments.features.dashboard.ui.MainActivity
import com.thellex.payments.features.fiat.CryptoToFiatOffRampActivity
import com.thellex.payments.features.fiat.FiatDepositActivity
import com.thellex.payments.features.fiat.FiatRampTransactionsActivity
import com.thellex.payments.features.fiat.FiatToCryptoOnRampActivity
import com.thellex.payments.features.fiat.FiatWithdrawActivity
import com.thellex.payments.features.kyc.ui.StartKycActivity
import com.thellex.payments.features.notifications.ui.NotificationsActivity
import com.thellex.payments.features.onboarding.LauncherActivity
import com.thellex.payments.features.onboarding.OnboardingActivity
import com.thellex.payments.features.pos.fragments.RequestOptionsModalFragment
import com.thellex.payments.features.pos.fragments.WithdrawalOptionsModalFragment
import com.thellex.payments.features.profile.ProfileActivity
import com.thellex.payments.features.wallet.ui.TransactionSuccessActivity
import com.thellex.payments.features.wallet.utils.WalletManagerModelFactory
import com.thellex.payments.features.wallet.utils.WalletManagerViewModel
import com.thellex.payments.features.wallet.ui.WalletAssetsActivity
import com.thellex.payments.features.wallet.ui.WithdrawToCryptoWalletActivity
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
    private var isBalanceVisible = true
    private var currentBalance = "0.00"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPOSBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ActivityTracker.add(this)
        disableDecorFitsSystemWindows()
        setTransparentStatusBarWithWhiteIcons()
        binding.posMain.applyAdvancedSystemBarInsets(fixedHorizontalPaddingDp = 0)

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
        observeNotification()
        closeAllOtherActivities()
    }

    private fun setupRecyclerView() {
        transactionRecyclerView = binding.recyclerRecentTransactions
        transactionRecyclerView.layoutManager = LinearLayoutManager(this)

        transactionAdapter = POSTransactionAdapter() {}
        transactionRecyclerView.adapter = transactionAdapter

        val itemSpacing = resources.getDimensionPixelSize(R.dimen.txn_margin)
        transactionRecyclerView.addItemDecoration(ItemSpacingDecoration(itemSpacing))
    }

    private fun loadWalletData() {
        walletManagerViewModel.loadWallet(
            tokenProvider = { userViewModel.token.asFlow().first { !it.isNullOrBlank() } },
            loadNow = false
        )
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
            binding.activityPosUserUidText.text = upperUid
        }
    }

    private fun updateBalanceText(balance: String) {
        binding.tvBalance.text = if (isBalanceVisible) "$$balance" else "•••••"
    }

    private fun observeUserTransactions() {
        lifecycleScope.launch {
            UserPreferences.getAuthResult(applicationContext).collect { userEntity ->
                val transactions = userEntity?.transactionHistory ?: emptyList()
                val sortedTransactions = transactions.sortedByDescending { it.createdAt }
                withContext(Dispatchers.Main) {
                    transactionAdapter.updateList(sortedTransactions)

                    if (sortedTransactions.isEmpty()) {
                        binding.recyclerRecentTransactions.visibility = View.GONE
                        binding.titleRecentTransactions.visibility = View.GONE
                        binding.emptyTransactionsView.visibility = View.VISIBLE
                    } else {
                        binding.recyclerRecentTransactions.visibility = View.VISIBLE
                        binding.titleRecentTransactions.visibility = View.VISIBLE
                        binding.emptyTransactionsView.visibility = View.GONE
                    }
                }
            }
        }
    }

    private fun observeNotification() {
        userViewModel.authResult.observe(this) { dto ->
            dto?.notifications?.let { notifications ->
                val unconsumedCount = notifications.count { !it.consumed }
                updateNotificationBadge(unconsumedCount)
            }
        }
    }

    private fun updateNotificationBadge(count: Int) {
        if (count > 0) {
            binding.activityPosNotificationBadge.visibility = View.VISIBLE
            binding.activityPosNotificationBadge.text = "$count"
        } else {
            binding.activityPosNotificationBadge.visibility = View.GONE
        }
    }

    private fun setupClickListeners() {
        binding.posWithdrawButton.setOnClickListener {
            showWithdrawalOptionsModal()
        }

        binding.activityPosRequestButton.setOnClickListener {
            showRequestOptionsModal()
        }

        binding.posViewAssetsButton.setOnClickListener {
            startActivity(Intent(this, WalletAssetsActivity::class.java))
        }

        binding.activityPosBellContainer.setOnClickListener{
            startActivity(Intent(this, NotificationsActivity::class.java))
        }

        binding.activityPosAvatarIcon.setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))
        }
    }

    private fun showRequestOptionsModal() {
        val modal = RequestOptionsModalFragment.newInstance()

        modal.setListener(object : RequestOptionsModalFragment.ReceiveOptionsListener {
            override fun onChainDepositClick() {
                startActivity(Intent(this@POSHomeActivity, POSChooseCryptoActivity::class.java))
            }
            override fun onCryptoToFiatOnRampClick() {
                startActivity(Intent(this@POSHomeActivity, FiatToCryptoOnRampActivity::class.java))
            }

            override fun onFiatDepositClick() {
                startActivity(Intent(this@POSHomeActivity, FiatDepositActivity::class.java))
            }

            override fun onStartKyc() {
                modal.dismiss()
                startActivity(Intent(this@POSHomeActivity, StartKycActivity::class.java))
            }
        })

        modal.show(supportFragmentManager, "RequestOptionsModal")
    }

    private fun showWithdrawalOptionsModal() {
        val modal = WithdrawalOptionsModalFragment.newInstance()

        modal.setListener(object : WithdrawalOptionsModalFragment.WithdrawalOptionsListener {
            override fun onCryptoToFiatOffRamp() {
                startActivity(Intent(this@POSHomeActivity, CryptoToFiatOffRampActivity::class.java))
            }

            override fun onWithdrawToBank() {
                startActivity(Intent(this@POSHomeActivity, FiatWithdrawActivity::class.java))
            }

            override fun onChainWithdraw() {
                startActivity(Intent(this@POSHomeActivity, WithdrawToCryptoWalletActivity::class.java))
            }

            override fun onStartKyc() {
                modal.dismiss()
                startActivity(Intent(this@POSHomeActivity, StartKycActivity::class.java))
            }
        })

        modal.show(supportFragmentManager, "WithdrawalOptionsModal")
    }

    private fun closeAllOtherActivities() {
        ActivityTracker.finishActivity(MainActivity::class.java)
        ActivityTracker.finishActivity(LauncherActivity::class.java)
        ActivityTracker.finishActivity(OnboardingActivity::class.java)
        ActivityTracker.finishActivity(LoginActivity::class.java)
        ActivityTracker.finishActivity(AuthVerificationActivity::class.java)
        ActivityTracker.finishActivity(TransactionSuccessActivity::class.java)
        ActivityTracker.finishActivity(KycSuccessActivity::class.java)
        ActivityTracker.finishActivity(FiatRampTransactionsActivity::class.java)
    }

}
