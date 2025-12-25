package com.thellex.pay.features.pos.ui

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import com.thellex.pay.features.auth.viewModel.UserViewModel
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.asFlow
import androidx.lifecycle.lifecycleScope
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayoutMediator
import com.thellex.pay.R
import com.thellex.pay.core.routes.ComposeRoutes
import com.thellex.pay.core.utils.ActivityTracker
import com.thellex.pay.core.utils.ComposeHostActivity
import com.thellex.pay.core.utils.Helpers.applyAdvancedSystemBarInsets
import com.thellex.pay.core.utils.Helpers.disableDecorFitsSystemWindows
import com.thellex.pay.core.utils.Helpers.setTransparentStatusBarWithWhiteIcons
import com.thellex.pay.data.enums.RoleEnum
import com.thellex.pay.data.model.AdminData
import com.thellex.pay.data.model.AdminRampTransactionsResponse
import com.thellex.pay.data.model.ApiResponse
import com.thellex.pay.data.model.ITransactionHistoryDto
import com.thellex.pay.data.model.TransactionTypeEnum
import com.thellex.pay.data.repository.BaseSettingsRepository
import com.thellex.pay.databinding.ActivityPOSBinding
import com.thellex.pay.features.auth.ui.AuthVerificationActivity
import com.thellex.pay.features.kyc.ui.basic.KycSuccessActivity
import com.thellex.pay.features.auth.ui.LoginActivity
import com.thellex.pay.features.auth.viewModel.UserViewModelFactory
import com.thellex.pay.features.dashboard.ui.MainActivity
import com.thellex.pay.features.fiat.OnRampFiatSummaryActivity
import com.thellex.pay.features.fiat.FiatWithdrawActivity
import com.thellex.pay.features.kyc.ui.StartKycActivity
import com.thellex.pay.features.notifications.ui.NotificationsActivity
import com.thellex.pay.features.onboarding.LauncherActivity
import com.thellex.pay.features.onboarding.OnboardingActivity
import com.thellex.pay.features.pos.fragments.RequestOptionsModalFragment
import com.thellex.pay.features.pos.fragments.WithdrawalOptionsModalFragment
import com.thellex.pay.features.profile.ProfileActivity
import com.thellex.pay.features.wallet.ui.TransactionSuccessActivity
import com.thellex.pay.features.wallet.utils.WalletManagerModelFactory
import com.thellex.pay.features.wallet.utils.WalletManagerViewModel
import com.thellex.pay.network.services.ApiClient
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class POSHomeActivity : AppCompatActivity() {
    lateinit var binding: ActivityPOSBinding
    private lateinit var userViewModel: UserViewModel
    private lateinit var walletManagerViewModel: WalletManagerViewModel
    private var isBalanceVisible = true
    private var currentBalance = "0.00"
    private val emptyStateMediator = MediatorLiveData<Pair<Int, List<ITransactionHistoryDto>?>>()
    private lateinit var baseSettingsRepo: BaseSettingsRepository


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

        baseSettingsRepo = BaseSettingsRepository(this)

        setupClickListeners()
        setupWalletBalanceObserver()
        loadAppData()
        loadWalletData()

        observeUserUid()
        observeNotification()
        closeAllOtherActivities()
        try {
            setupViewPager()
            setupEmptyState()
        } catch (e: Exception) {
            Log.e(TAG, "Error inflating layout: ${e.message}", e)
            throw e
        }
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onResume() {
        super.onResume()
        userViewModel.refreshNotificationsStatus()
        updateAttentionGrabber()
    }


    @SuppressLint("UseCompatLoadingForDrawables")
    private fun setupViewPager() {
        val adapter = ViewPagerAdapter(this, listOf(DepositsFragment(), WithdrawalsFragment()))
        binding.viewPager.adapter = adapter
        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            tab.text = if (position == 0) "DEPOSIT" else "WITHDRAW"
            tab.view.background = resources.getDrawable(
                if (position == 0) R.drawable.tab_deposits_background
                else R.drawable.tab_withdrawals_background, theme
            )
            val horizontalPadding = (16 * resources.displayMetrics.density).toInt()
            val verticalPadding = (8 * resources.displayMetrics.density).toInt()
            tab.view.setPadding(horizontalPadding, verticalPadding, horizontalPadding, verticalPadding)
        }.attach()

        binding.viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                userViewModel.setSelectedTab(position)
                emptyStateMediator.value = Pair(position, userViewModel.authResult.value?.transactionHistory)
            }
        })
    }

    private fun setupEmptyState() {
        emptyStateMediator.addSource(userViewModel.selectedTab) { tab ->
            emptyStateMediator.value = Pair(tab, userViewModel.authResult.value?.transactionHistory)
        }
        emptyStateMediator.addSource(userViewModel.authResult) { dto ->
            emptyStateMediator.value = Pair(userViewModel.selectedTab.value ?: 0, dto?.transactionHistory)
        }

        emptyStateMediator.observe(this) { (tabPosition, transactions) ->
            val isDepositsTab = tabPosition == 0
            val filteredTransactions = if (isDepositsTab) {
                transactions?.filter {
                    it.transactionType == TransactionTypeEnum.CRYPTO_TO_FIAT_DEPOSIT ||
                    it.transactionType == TransactionTypeEnum.FIAT_TO_CRYPTO_DEPOSIT ||
                    it.transactionType == TransactionTypeEnum.CRYPTO_DEPOSIT
                } ?: emptyList()
            } else {
                transactions?.filter {
                    it.transactionType == TransactionTypeEnum.CRYPTO_TO_FIAT_WITHDRAWAL ||
                    it.transactionType == TransactionTypeEnum.FIAT_TO_CRYPTO_WITHDRAWAL ||
                    it.transactionType == TransactionTypeEnum.CRYPTO_WITHDRAWAL
                } ?: emptyList()
            }
            val isEmpty = filteredTransactions.isEmpty()
            binding.titleRecentTransactions.visibility = if (isEmpty) View.GONE else View.VISIBLE
//            binding.buttonViewAll.visibility = if (isEmpty) View.GONE else View.VISIBLE
            binding.emptyTransactionsView.visibility = if (isEmpty) View.VISIBLE else View.GONE
        }
    }

    private fun loadAppData() {
        lifecycleScope.launch {
            try {
                // Wait for a valid token
                val token = userViewModel.token.asFlow().first { !it.isNullOrBlank() }
                    ?: run {
                        Log.e(TAG, "No valid token found")
                        return@launch
                    }

                // Initialize APIs
                val adminApi = ApiClient.getAuthenticatedAdminApi(this@POSHomeActivity, token)
                val userApi = ApiClient.getAuthenticatedUserApi(this@POSHomeActivity, token)
                val settingsApi = ApiClient.getAppApi(this@POSHomeActivity, token)

                // Get current user and admin data
                val currentUser = userViewModel.authResult.asFlow().filterNotNull().first()
                val currentAdminData = userViewModel.adminData.value ?: AdminData()

                // Launch concurrent API calls
                val adminRampDeferred: Deferred<ApiResponse<AdminRampTransactionsResponse>>? =
                    if (currentUser.role == RoleEnum.SUPER_ADMIN) {
                        async { adminApi.fetchAllRampTransactions() }
                    } else null
                val userRampDeferred = async { userApi.fetchRampTransactions() }
                val userTxnHistoryDeferred = async { userApi.fetchTransactionHistory() }
                val userNotificationsDeferred = async { userApi.fetchNotifications() }

                // Collect admin data
                val updatedAdminData = try {
                    val adminRampTransactions = adminRampDeferred?.await()?.result
                    currentAdminData.copy(rampTransactions = adminRampTransactions)
                } catch (e: Exception) {
                    Log.e(TAG, "Error fetching admin ramp transactions", e)
                    currentAdminData
                }

                // Collect user data with emptyList() fallback
                val userRampTransactions = try {
                    userRampDeferred.await().result?.data ?: emptyList()
                } catch (e: Exception) {
                    Log.e(TAG, "Error fetching user ramp transactions", e)
                    emptyList()
                }
                val userTxnHistory = try {
                    userTxnHistoryDeferred.await().result?.data ?: emptyList()
                } catch (e: Exception) {
                    Log.e(TAG, "Error fetching transaction history", e)
                    emptyList()
                }
                val userNotifications = try {
                    userNotificationsDeferred.await().result?.data ?: emptyList()
                } catch (e: Exception) {
                    Log.e(TAG, "Error fetching notifications", e)
                    emptyList()
                }

                lifecycleScope.launch {
                    val chains = baseSettingsRepo.getBaseSettings(token)
                    Log.d("ChainList", "Base settings response: $chains")
                }

                var mergedUser = currentUser
                mergedUser = mergedUser.copy(fiatCryptoRampTransactions = userRampTransactions)
                mergedUser = mergedUser.copy(transactionHistory = userTxnHistory)
                mergedUser = mergedUser.copy(notifications = userNotifications)

               // Save results to ViewModel
                userViewModel.saveAdminResult(updatedAdminData)
                userViewModel.saveAuthResult(mergedUser)
            } catch (e: Exception) {
                Log.e(TAG, "Error in loadAppData", e)
            }
        }
    }

    private var walletObserverAttached = false

    private fun loadWalletData(action: String? = "") {

        // Attach observer ONCE
        if (!walletObserverAttached) {
            walletObserverAttached = true

            walletManagerViewModel.walletBalance.observe(this) { balance ->
                val isLoaded = balance != null
                binding.posViewAssetsButton.isEnabled = isLoaded
                binding.posViewAssetsButton.alpha = if (isLoaded) 1f else 0.5f
            }
        }

        // If already cached → enable immediately and EXIT
        if (walletManagerViewModel.walletBalance.value != null) {
            binding.posViewAssetsButton.isEnabled = true
            binding.posViewAssetsButton.alpha = 1f
            return
        }

        // Otherwise disable and fetch
        binding.posViewAssetsButton.isEnabled = false
        binding.posViewAssetsButton.alpha = 0.5f

        lifecycleScope.launch {
            val token = userViewModel.token
                .asFlow()
                .first { !it.isNullOrBlank() }

            walletManagerViewModel.loadWallet(
                tokenProvider = { token }
            )
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
            binding.activityPosUserUidText.text = upperUid
        }
    }

    private fun updateBalanceText(balance: String) {
        binding.tvBalance.text = if (isBalanceVisible) "$$balance" else "•••••"
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
            val balance = walletManagerViewModel.walletBalance.value
                ?: return@setOnClickListener

            val intent = ComposeHostActivity.newIntent(
                this@POSHomeActivity,
                ComposeRoutes.WalletHome.route
            )
            startActivity(intent)
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
                val intent = ComposeHostActivity.newIntent(
                    this@POSHomeActivity,
                    ComposeRoutes.CryptoDepositTokensSelection.route
                )
                startActivity(intent)
            }
            override fun onCryptoToFiatOnRampClick() {
//                startActivity(Intent(this@POSHomeActivity, FiatToCryptoOnRampActivity::class.java))
            }

            override fun onFiatDepositClick() {
                startActivity(Intent(this@POSHomeActivity, OnRampFiatSummaryActivity::class.java))
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
//                startActivity(Intent(this@POSHomeActivity, CryptoToFiatOffRampActivity::class.java))
            }

            override fun onWithdrawToBank() {
                startActivity(Intent(this@POSHomeActivity, FiatWithdrawActivity::class.java))
            }

            override fun onChainWithdraw() {
                val intent = ComposeHostActivity.newIntent(
                    this@POSHomeActivity,
                    ComposeRoutes.CryptoWithdrawal.route
                )
                startActivity(intent)
//                startActivity(Intent(this@POSHomeActivity, WithdrawToCryptoWalletActivity::class.java))
            }

            override fun onStartKyc() {
                modal.dismiss()
                startActivity(Intent(this@POSHomeActivity, StartKycActivity::class.java))
            }
        })

        modal.show(supportFragmentManager, "WithdrawalOptionsModal")
    }

    private fun areNotificationsEnabled(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context, Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            NotificationManagerCompat.from(context).areNotificationsEnabled()
        }
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private fun updateAttentionGrabber() {
        val notificationsEnabled = userViewModel.notificationsEnabled.value ?: false
        Log.d(TAG, "notificationsEnabled: $notificationsEnabled, Dismissed: ${userViewModel.isNotificationsDismissed()}")

        if (!notificationsEnabled && !userViewModel.isNotificationsDismissed()) {
            val permission = Manifest.permission.POST_NOTIFICATIONS
            binding.attentionGrabber.setAttentionGrabber(
                message = "Enable notifications to stay updated!",
                actionText = "Enable",
                iconResId = R.drawable.icon_notification_gray,
                onActionClick = {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        if (ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED) {
                            Log.d(TAG, "Notifications already granted")
                            userViewModel.setNotificationsEnabled(true)
                            userViewModel.setNotificationsDismissed(true)
                            userViewModel.refreshNotificationsStatus()
                            binding.attentionGrabber.hide()
                        } else {
                            Log.d(TAG, "Requesting notification permission")
                            ActivityCompat.requestPermissions(
                                this@POSHomeActivity,
                                arrayOf(permission),
                                REQUEST_CODE_NOTIFICATIONS
                            )
                        }
                    } else {
                        Log.d(TAG, "Notifications enabled by default (< Android 13)")
                        userViewModel.setNotificationsEnabled(true)
                        userViewModel.setNotificationsDismissed(true)
                        userViewModel.refreshNotificationsStatus()
                        binding.attentionGrabber.hide()
                    }
                },
                onCloseClick = {
                    Log.d(TAG, "Notification prompt closed without enabling")
                    userViewModel.setNotificationsDismissed(false)
                    userViewModel.refreshNotificationsStatus()
                    binding.attentionGrabber.hide()
                }
            )
        } else {
            Log.d(TAG, "Notifications already enabled or dismissed, hiding attention grabber")
            binding.attentionGrabber.hide()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE_NOTIFICATIONS) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.d(TAG, "Notification permission granted by user")
                userViewModel.setNotificationsEnabled(true)
                userViewModel.setNotificationsDismissed(true)
                userViewModel.refreshNotificationsStatus()
                binding.attentionGrabber.hide()
            } else {
                Log.d(TAG, "Notification permission denied by user")
                userViewModel.setNotificationsEnabled(false)
                userViewModel.setNotificationsDismissed(true)
                userViewModel.refreshNotificationsStatus()
                binding.attentionGrabber.hide()
            }
        }
    }

    private fun closeAllOtherActivities() {
        ActivityTracker.finishActivity(MainActivity::class.java)
        ActivityTracker.finishActivity(LauncherActivity::class.java)
        ActivityTracker.finishActivity(OnboardingActivity::class.java)
        ActivityTracker.finishActivity(LoginActivity::class.java)
        ActivityTracker.finishActivity(AuthVerificationActivity::class.java)
        ActivityTracker.finishActivity(TransactionSuccessActivity::class.java)
        ActivityTracker.finishActivity(KycSuccessActivity::class.java)
//        ActivityTracker.finishActivity(FiatRampTransactionsActivity::class.java)
    }

    companion object {
        private val TAG = "Dashboard"
        private const val REQUEST_CODE_NOTIFICATIONS = 1001
    }
}

class ViewPagerAdapter(
    fragmentActivity: FragmentActivity,
    private val fragments: List<Fragment>
) : FragmentStateAdapter(fragmentActivity) {
    override fun getItemCount(): Int = fragments.size
    override fun createFragment(position: Int): Fragment = fragments[position]
}
