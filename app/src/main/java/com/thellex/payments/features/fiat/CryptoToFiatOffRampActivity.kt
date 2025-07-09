package com.thellex.payments.features.fiat

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.thellex.payments.R
import com.thellex.payments.core.utils.Helpers.applyAdvancedSystemBarInsets
import com.thellex.payments.core.utils.Helpers.disableDecorFitsSystemWindows
import com.thellex.payments.core.utils.Helpers.setTransparentStatusBarWithWhiteIcons
import com.thellex.payments.databinding.ActivityCryptoToFiatOffRampBinding

class CryptoToFiatOffRampActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCryptoToFiatOffRampBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityCryptoToFiatOffRampBinding.inflate(layoutInflater)
        setContentView(binding.root)
        disableDecorFitsSystemWindows()
        setTransparentStatusBarWithWhiteIcons()
        binding.main.applyAdvancedSystemBarInsets()
    }
}
