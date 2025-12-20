package com.thellex.pay.features.pos.ui

import com.thellex.pay.features.auth.viewModel.UserViewModel
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.thellex.pay.core.decorators.ItemSpacingDecoration
import com.thellex.pay.R
import com.thellex.pay.core.utils.ActivityTracker
import com.thellex.pay.core.utils.Helpers
import com.thellex.pay.core.utils.Helpers.applyAdvancedSystemBarInsets
import com.thellex.pay.core.utils.Helpers.disableDecorFitsSystemWindows
import com.thellex.pay.core.utils.Helpers.setTransparentStatusBarWithWhiteIcons
import com.thellex.pay.features.pos.adapters.CryptoAdapter
import com.thellex.pay.data.model.TokenListDto
import com.thellex.pay.databinding.ActivityPosChooseCryptoBinding
import com.thellex.pay.settings.PaymentType
import com.thellex.pay.features.auth.viewModel.UserViewModelFactory
import com.thellex.pay.features.wallet.utils.WalletManagerModelFactory
import com.thellex.pay.features.wallet.utils.WalletManagerViewModel

class POSChooseCryptoActivity : AppCompatActivity() {
    private lateinit var topBar: Helpers.TopAppBarController
    private lateinit var binding: ActivityPosChooseCryptoBinding
    private lateinit var cryptoAdapter: CryptoAdapter
    private lateinit var viewModel: UserViewModel
    private lateinit var walletManagerViewModel: WalletManagerViewModel

    private var cryptoList = mutableListOf<TokenListDto>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPosChooseCryptoBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ActivityTracker.add(this)
        disableDecorFitsSystemWindows()
        setTransparentStatusBarWithWhiteIcons()
        binding.main.applyAdvancedSystemBarInsets()

        // Initialize data from intent
        topBar = Helpers.setupTopAppBar(
            activity = this,
            rootView = findViewById(R.id.requestPosChooseTopAppBAr),
            title = "CHOOSE CURRENCY"
        )

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
    }

    private fun observeWalletData() {
//        walletManagerViewModel.walletBalance.observe(this) { walletDto ->
//            val updatedCryptoList1 = walletDto?.wallets?.values
//                ?.filter { wallet ->
//                    wallet.address?.isNotEmpty() == true && Helpers.isValidEvmAddress(wallet.address ?: "")
//                }?.map { wallet ->
//                    TokenListDto(
//                        wallet.assetCode,
//                        Helpers.getIconResIdForToken(wallet.assetCode.toString()),
//                        chainName = wallet.network.name
//                    )
//                }
//
//                updatedCryptoList1?.let {
//                    cryptoAdapter.updateData(it)
//                }
//        }
    }
}

