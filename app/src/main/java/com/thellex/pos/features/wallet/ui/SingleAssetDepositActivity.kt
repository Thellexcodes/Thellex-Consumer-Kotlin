package com.thellex.pos.features.wallet.ui

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.thellex.pos.databinding.ActivitySingleAssetDepositBinding
import com.thellex.pos.features.pos.ui.PosAddressGeneratorActivity

class SingleAssetDepositActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySingleAssetDepositBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivitySingleAssetDepositBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { view, insets ->
            val systemBarsInsets = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(
                view.paddingLeft,
                systemBarsInsets.top,
                view.paddingRight,
                systemBarsInsets.bottom
            )
            insets
        }

        binding.sadCryptoDepositCard.setOnClickListener {
            startActivity(Intent(this, PosAddressGeneratorActivity::class.java))
        }
    }
}
