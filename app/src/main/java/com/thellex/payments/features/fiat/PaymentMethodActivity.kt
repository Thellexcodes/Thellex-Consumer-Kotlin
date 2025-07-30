package com.thellex.payments.features.fiat

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.gson.Gson
import com.thellex.payments.R
import com.thellex.payments.core.utils.ActivityTracker
import com.thellex.payments.core.utils.Helpers
import com.thellex.payments.core.utils.Helpers.applyAdvancedSystemBarInsets
import com.thellex.payments.core.utils.Helpers.disableDecorFitsSystemWindows
import com.thellex.payments.core.utils.Helpers.setTransparentStatusBarWithWhiteIcons
import com.thellex.payments.data.model.IBankAccountDto
import com.thellex.payments.data.model.IBankInfoRequestDto
import com.thellex.payments.databinding.ActivityPaymentMethodBinding
import com.thellex.payments.features.auth.viewModel.UserViewModel
import com.thellex.payments.features.auth.viewModel.UserViewModelFactory
import com.thellex.payments.features.fiat.adapters.PaymentMethodAdapter
import com.thellex.payments.features.fiat.fragments.AddAccountBottomSheetFragment
import com.thellex.payments.features.fiat.model.CryptoToFiatViewModel
import com.thellex.payments.features.wallet.utils.WalletManagerModelFactory
import com.thellex.payments.features.wallet.utils.WalletManagerViewModel

class PaymentMethodActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPaymentMethodBinding
    private lateinit var adapter: PaymentMethodAdapter
    private lateinit var topBar: Helpers.TopAppBarController
    private lateinit var userViewModel: UserViewModel
    private lateinit var walletManagerViewModel: WalletManagerViewModel
    private lateinit var cryptoToFiatViewModel: CryptoToFiatViewModel

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityPaymentMethodBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ActivityTracker.add(this)
        disableDecorFitsSystemWindows()
        setTransparentStatusBarWithWhiteIcons()
        binding.main.applyAdvancedSystemBarInsets()

        topBar = Helpers.setupTopAppBar(
            activity = this,
            rootView = findViewById(R.id.included_top_app_bar),
            title = "PAYMENT METHOD"
        )

        adapter = PaymentMethodAdapter { selectedMethod ->
            Log.d("PaymentMethodActivity", "Selected payment method: $selectedMethod")
            val bankInfo = IBankInfoRequestDto(
                accountHolder = selectedMethod.accountName,
                accountNumber = selectedMethod.accountNumber,
                bankName = selectedMethod.bankName,
            )
            cryptoToFiatViewModel.bankInfo.value = bankInfo
            startActivity(Intent(this, RampSummaryActivity::class.java))
        }

        binding.paymentMethodsRecycler.apply {
            layoutManager = LinearLayoutManager(this@PaymentMethodActivity)
            adapter = this@PaymentMethodActivity.adapter
        }

        binding.addNewAccountButton.setOnClickListener { showAddAccountBottomSheet() }

        setupViewModel()
        observeUser()
    }

    private fun setupViewModel() {
        userViewModel = ViewModelProvider(
            this,
            UserViewModelFactory(applicationContext)
        )[UserViewModel::class.java]

        walletManagerViewModel = ViewModelProvider(
            this,
            WalletManagerModelFactory(applicationContext)
        )[WalletManagerViewModel::class.java]

        // Scope to CryptoToFiatOffRampActivity to share ViewModel
        cryptoToFiatViewModel = ViewModelProvider(
            owner = findActivity(CryptoToFiatOffRampActivity::class.java)
                ?: run {
                    startActivity(Intent(this, CryptoToFiatOffRampActivity::class.java))
                    finish()
                    return@setupViewModel
                },
            factory = ViewModelProvider.AndroidViewModelFactory.getInstance(application)
        )[CryptoToFiatViewModel::class.java]
    }

    private fun findActivity(activityClass: Class<out AppCompatActivity>): AppCompatActivity? {
        return ActivityTracker.getActivities().find { it::class.java == activityClass } as? AppCompatActivity
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun observeUser() {
        userViewModel.authResult.observe(this) { userDto ->
            if (userDto?.bankAccounts.isNullOrEmpty()) {
                showAddAccountBottomSheet()
            }
            updatePaymentMethods(userDto?.bankAccounts)
        }
    }

    private fun updatePaymentMethods(list: List<IBankAccountDto>?) {
        with(binding) {
            if (list.isNullOrEmpty()) {
                paymentMethodsRecycler.visibility = View.GONE
                emptyStateContainer.visibility = View.VISIBLE
                title.visibility = View.GONE
            } else {
                paymentMethodsRecycler.visibility = View.VISIBLE
                emptyStateContainer.visibility = View.GONE
                title.visibility = View.VISIBLE
                adapter.submitList(list)
            }
        }
    }

    private fun showAddAccountBottomSheet() {
        AddAccountBottomSheetFragment().show(supportFragmentManager, "AddAccountBottomSheet")
    }

    override fun onDestroy() {
        super.onDestroy()
        ActivityTracker.remove(this)
    }
}