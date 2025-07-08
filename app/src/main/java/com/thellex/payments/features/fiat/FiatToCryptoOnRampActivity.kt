package com.thellex.payments.features.fiat

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.thellex.payments.core.utils.ActivityTracker
import com.thellex.payments.core.utils.Helpers.applyAdvancedSystemBarInsets
import com.thellex.payments.core.utils.Helpers.disableDecorFitsSystemWindows
import com.thellex.payments.core.utils.Helpers.setTransparentStatusBarWithWhiteIcons
import com.thellex.payments.databinding.ActivityCryptoOnRampBinding
import com.thellex.payments.features.auth.viewModel.UserViewModel
import com.thellex.payments.features.auth.viewModel.UserViewModelFactory

class FiatToCryptoOnRampActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCryptoOnRampBinding
    private lateinit var userViewModel: UserViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityCryptoOnRampBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ActivityTracker.add(this)
        ActivityTracker.finishActivity(FiatToCryptoRequestAccountInfoActivity::class.java)
        disableDecorFitsSystemWindows()
        setTransparentStatusBarWithWhiteIcons()
        binding.layoutCryptoToFiat.applyAdvancedSystemBarInsets()
        setupViewModel()
        observeUser()
        setupUiListener()
    }

    // ViewModel Setup
    private fun setupViewModel() {
        userViewModel = ViewModelProvider(
            this,
            UserViewModelFactory(applicationContext)
        )[UserViewModel::class.java]
    }

    private fun observeUser() {
        userViewModel.authResult.observe(this) { userDto ->
            userDto?.nextTier?.let {
                binding.containerNextTierInfo.visibility = View.VISIBLE
                binding.currentTierLevel.text = it.name.value
                binding.currentTierLimitAmount.text = "NGN ${it.transactionLimits.dailyDebitLimit.toString()}"
            }
        }
    }

    private fun setupUiListener(){
        binding.nextButton.setOnClickListener{
            startActivity(Intent(this, FiatToCryptoRequestAccountInfoActivity::class.java))
        }
    }
    companion object {
        private val TAG = "TAG"
    }
}
