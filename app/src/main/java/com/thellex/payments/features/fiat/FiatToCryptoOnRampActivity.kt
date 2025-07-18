package com.thellex.payments.features.fiat

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.thellex.payments.R
import com.thellex.payments.core.utils.ActivityTracker
import com.thellex.payments.core.utils.Helpers.applyAdvancedSystemBarInsets
import com.thellex.payments.core.utils.Helpers.disableDecorFitsSystemWindows
import com.thellex.payments.core.utils.Helpers.setTransparentStatusBarWithWhiteIcons
import com.thellex.payments.databinding.ActivityCryptoOnRampBinding
import com.thellex.payments.features.auth.viewModel.UserRepository
import com.thellex.payments.features.auth.viewModel.UserViewModel
import com.thellex.payments.features.auth.viewModel.UserViewModelFactory
import com.thellex.payments.features.fiat.adapters.TokenSelectionBottomSheet
import com.thellex.payments.features.wallet.model.WalletDto
import com.thellex.payments.features.wallet.utils.WalletManagerModelFactory
import com.thellex.payments.features.wallet.utils.WalletManagerViewModel

class FiatToCryptoOnRampActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCryptoOnRampBinding
    private lateinit var userViewModel: UserViewModel
    private val userRepository by lazy { UserRepository.getInstance(applicationContext) }
    private lateinit var walletManagerViewModel: WalletManagerViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityCryptoOnRampBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ActivityTracker.add(this)
        disableDecorFitsSystemWindows()
        setTransparentStatusBarWithWhiteIcons()
        binding.layoutCryptoToFiat.applyAdvancedSystemBarInsets()
        setupViewModel()
        observeUser()
        setupUiListener()

        walletManagerViewModel = ViewModelProvider(
            this,
            WalletManagerModelFactory(applicationContext)
        )[WalletManagerViewModel::class.java]

        binding.fiatSpinner.setOnClickListener {
            val walletBalance = walletManagerViewModel.walletBalance.value
            walletBalance?.let {
                TokenSelectionBottomSheet.newInstance(it)
                    .show(supportFragmentManager, TokenSelectionBottomSheet.TAG)
            }
        }
    }

    // ViewModel Setup
    private fun setupViewModel() {
        userViewModel = ViewModelProvider(
            this,
            UserViewModelFactory(applicationContext)
        )[UserViewModel::class.java]
    }

    private fun observeUser() {
//        userViewModel.authResult.observe(this) { userDto ->
//            userDto?.nextTier?.let {
//                binding.containerNextTierInfo.visibility = View.VISIBLE
//                binding.currentTierLevel.text = it.name.value
//                binding.currentTierLimitAmount.text = "NGN ${it.transactionLimits.dailyDebitLimit}"
//            }
//        }
    }

    private fun setupUiListener(){
        binding.nextButton.setOnClickListener{
            //make request to get transaction from user
        }
    }

    fun updateFiatSpinner(token: WalletDto) {
//        binding.textFiatTicker.text = token.currency
//        binding.fiatSpinnerIcon.setImageResource(
//            token.iconResId ?: R.drawable.icon_default_token
//        )
    }

    companion object {
        private val TAG = "TAG"
    }
}
