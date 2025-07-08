package com.thellex.payments.features.fiat

import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.thellex.payments.core.utils.Helpers.applyAdvancedSystemBarInsets
import com.thellex.payments.core.utils.Helpers.disableDecorFitsSystemWindows
import com.thellex.payments.core.utils.Helpers.setTransparentStatusBarWithWhiteIcons
import com.thellex.payments.databinding.ActivityCryptoOnRampBinding
import com.thellex.payments.features.auth.viewModel.UserViewModel
import com.thellex.payments.features.auth.viewModel.UserViewModelFactory

class CryptoToFiatOnRampActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCryptoOnRampBinding
    private lateinit var userViewModel: UserViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityCryptoOnRampBinding.inflate(layoutInflater)
        setContentView(binding.root)
        disableDecorFitsSystemWindows()
        setTransparentStatusBarWithWhiteIcons()
        binding.layoutCryptoToFiat.applyAdvancedSystemBarInsets()
        setupViewModel()
        observeUser()
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
            Log.d(TAG, "User current tier is ${userDto?.currentTier}")
//            userDto?.nextTier?.let {
//            }
        }
    }

    companion object {
        private val TAG = "TAG"
    }
}
