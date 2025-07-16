package com.thellex.payments.features.fiat

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.thellex.payments.core.utils.ActivityTracker
import com.thellex.payments.core.utils.CustomToast
import com.thellex.payments.core.utils.Helpers.applyAdvancedSystemBarInsets
import com.thellex.payments.core.utils.Helpers.copyToClipboard
import com.thellex.payments.core.utils.Helpers.disableDecorFitsSystemWindows
import com.thellex.payments.core.utils.Helpers.setTransparentStatusBarWithWhiteIcons
import com.thellex.payments.databinding.ActivityFiatDepositBinding
import com.thellex.payments.features.auth.viewModel.UserViewModel
import com.thellex.payments.features.auth.viewModel.UserViewModelFactory
import com.thellex.payments.features.wallet.utils.WalletManagerModelFactory
import com.thellex.payments.features.wallet.utils.WalletManagerViewModel

class FiatDepositActivity : AppCompatActivity() {
    private lateinit var userViewModel: UserViewModel
    private lateinit var walletManagerViewModel: WalletManagerViewModel

    private lateinit var binding: ActivityFiatDepositBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFiatDepositBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ActivityTracker.add(this)
        disableDecorFitsSystemWindows()
        setTransparentStatusBarWithWhiteIcons()
        binding.main.applyAdvancedSystemBarInsets()

        userViewModel = ViewModelProvider(
            this,
            UserViewModelFactory(applicationContext)
        )[UserViewModel::class.java]

        walletManagerViewModel = ViewModelProvider(
            this,
            WalletManagerModelFactory(applicationContext)
        )[WalletManagerViewModel::class.java]

        binding.buttonBack.setOnClickListener { finish() }

        userViewModel.authResult.observe(this) { userDto ->
            val accountDetails = userDto?.bankAccounts?.getOrNull(0)
            binding.accountNumber.text = accountDetails?.accountNumber
            binding.accountName.text = accountDetails?.accountName
            binding.bankName.text = accountDetails?.bankName

            binding.copyAccountDetails.setOnClickListener {
                val accountNumber = accountDetails?.accountNumber
                if (!accountNumber.isNullOrEmpty()) {
                    copyToClipboard("Account Number", accountNumber)
                } else {
                    CustomToast.show(this, "Empty", "No account number to copy")
                }
            }
        }

    }

    companion object {
        private val TAG = "TAGY"
    }
}
