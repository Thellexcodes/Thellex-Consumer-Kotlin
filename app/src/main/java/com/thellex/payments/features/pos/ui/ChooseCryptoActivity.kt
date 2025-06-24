package com.thellex.payments.features.pos.ui

import com.thellex.payments.features.auth.viewModel.UserViewModel
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.thellex.payments.core.decorators.ItemSpacingDecoration
import com.thellex.payments.R
import com.thellex.payments.core.utils.Helpers
import com.thellex.payments.features.pos.adapters.CryptoAdapter
import com.thellex.payments.data.model.Crypto
import com.thellex.payments.data.model.TokenListDto
import com.thellex.payments.settings.PaymentType
import com.thellex.payments.features.auth.viewModel.UserViewModelFactory
import com.thellex.payments.features.wallet.model.WalletManagerModelFactory
import com.thellex.payments.features.wallet.model.WalletManagerViewModel

class POSChooseCryptoActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var cryptoAdapter: CryptoAdapter
    private lateinit var viewModel: UserViewModel
    private lateinit var walletManagerViewModel: WalletManagerViewModel

    private var cryptoList = mutableListOf<TokenListDto>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pos_choose_crypto)

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

        viewModel = ViewModelProvider(
            this,
            UserViewModelFactory(applicationContext)
        )[UserViewModel::class.java]

        walletManagerViewModel = ViewModelProvider(
            this,
            WalletManagerModelFactory(applicationContext)
        )[WalletManagerViewModel::class.java]

        recyclerView = findViewById(R.id.pos_crypto_list_selection)
        recyclerView.layoutManager = LinearLayoutManager(this)

        cryptoAdapter = CryptoAdapter(cryptoList) { selectedItem ->
            val assetCode = selectedItem.assetCode
            val intent = Intent(this, GeneratePOSAddressActivity::class.java)
            intent.putExtra("type", PaymentType.REQUEST_CRYPTO.name)
            intent.putExtra("assetCode", assetCode.name)
            intent.putExtra("assetCodeChain", selectedItem.chainName)
            startActivity(intent)
        }

        recyclerView.adapter = cryptoAdapter

        val spacing = resources.getDimensionPixelSize(R.dimen.txn_margin)
        recyclerView.addItemDecoration(ItemSpacingDecoration(spacing))

        observeWalletData()

        val backButton = findViewById<ImageView>(R.id.activity_wallet_back_button)
        backButton.setOnClickListener {
            finish()
        }
    }

    private fun observeWalletData() {
        walletManagerViewModel.walletBalance.observe(this) { walletDto ->
            val updatedCryptoList = walletDto.wallets.values.map { wallet ->
                TokenListDto(wallet.assetCode,
                    Helpers.getIconResIdForToken(wallet.assetCode.toString()),
                    chainName = wallet.networks[0] )
            }
            cryptoAdapter.updateData(updatedCryptoList)
        }
    }
}
