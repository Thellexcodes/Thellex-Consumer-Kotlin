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
import com.thellex.payments.data.model.TokenListDto
import com.thellex.payments.databinding.ActivityPosChooseCryptoBinding
import com.thellex.payments.settings.PaymentType
import com.thellex.payments.features.auth.viewModel.UserViewModelFactory
import com.thellex.payments.features.wallet.utils.WalletManagerModelFactory
import com.thellex.payments.features.wallet.utils.WalletManagerViewModel

class POSChooseCryptoActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPosChooseCryptoBinding
    private lateinit var cryptoAdapter: CryptoAdapter
    private lateinit var viewModel: UserViewModel
    private lateinit var walletManagerViewModel: WalletManagerViewModel

    private var cryptoList = mutableListOf<TokenListDto>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPosChooseCryptoBinding.inflate(layoutInflater)
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

        viewModel = ViewModelProvider(
            this,
            UserViewModelFactory(applicationContext)
        )[UserViewModel::class.java]

        walletManagerViewModel = ViewModelProvider(
            this,
            WalletManagerModelFactory(applicationContext)
        )[WalletManagerViewModel::class.java]

        binding.posCryptoListSelection.layoutManager = LinearLayoutManager(this)

        cryptoAdapter = CryptoAdapter(cryptoList) { selectedItem ->
            val assetCode = selectedItem.assetCode
            val intent = Intent(this, GeneratePOSAddressActivity::class.java).apply {
                putExtra("type", PaymentType.REQUEST_CRYPTO.name)
                putExtra("assetCode", assetCode.name)
                putExtra("assetCodeChain", selectedItem.chainName)
            }
            startActivity(intent)
        }

        binding.posCryptoListSelection.adapter = cryptoAdapter

        val spacing = resources.getDimensionPixelSize(R.dimen.txn_margin)
        binding.posCryptoListSelection.addItemDecoration(ItemSpacingDecoration(spacing))

        observeWalletData()

        binding.backButton.setOnClickListener {
            finish()
        }
    }

    private fun observeWalletData() {
        walletManagerViewModel.walletBalance.observe(this) { walletDto ->
            val updatedCryptoList1 = walletDto?.wallets?.values
                ?.filter { wallet ->
                    wallet.address.isNotEmpty() && Helpers.isValidEvmAddress(wallet.address)
                }?.map { wallet ->
                    TokenListDto(
                        wallet.assetCode,
                        Helpers.getIconResIdForToken(wallet.assetCode.toString()),
                        chainName = wallet.network.name
                    )
                }
            updatedCryptoList1?.let {
                cryptoAdapter.updateData(it)
            }
        }
    }

    companion object {
        private val TAG = "TAG"
    }
}

