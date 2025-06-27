package com.thellex.payments.features.wallet.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.thellex.payments.data.model.UserPreferences
import com.thellex.payments.databinding.ActivitySingleAssetBalanceBinding
import com.thellex.payments.features.auth.viewModel.UserViewModel
import com.thellex.payments.features.auth.viewModel.UserViewModelFactory
import com.thellex.payments.features.wallet.fragments.SendBottomSheetFragment
import com.thellex.payments.features.wallet.utils.WalletManagerModelFactory
import com.thellex.payments.features.wallet.utils.WalletManagerViewModel
import com.thellex.payments.features.wallet.prefrences.WalletManagerPreferences
import kotlinx.coroutines.launch

class AssetBalanceActivity : AppCompatActivity() {

    private lateinit var userViewModel: UserViewModel
    private lateinit var walletManagerViewModel: WalletManagerViewModel
    private lateinit var walletPreferences: WalletManagerPreferences
    private lateinit var binding: ActivitySingleAssetBalanceBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySingleAssetBalanceBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupViewModels()
        walletPreferences = walletManagerViewModel.getWalletPreferences()

        setupWindowInsets()
        setupSendButton()
        loadTransactions("usdt")
    }

    private fun setupViewModels() {
        walletManagerViewModel = ViewModelProvider(
            this,
            WalletManagerModelFactory(applicationContext)
        )[WalletManagerViewModel::class.java]

        userViewModel = ViewModelProvider(
            this,
            UserViewModelFactory(applicationContext)
        )[UserViewModel::class.java]
    }

    private fun setupWindowInsets() {
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
    }

    private fun setupSendButton() {
        binding.activitySingleAssetBalanceSendButton.setOnClickListener {
            val bottomSheet = SendBottomSheetFragment()
            bottomSheet.show(supportFragmentManager, bottomSheet.tag)
        }
    }

    private fun loadTransactions(currencyFilter: String) {
        lifecycleScope.launch {
            UserPreferences.getAuthResult(applicationContext).collect { userEntity ->
                val transactions = userEntity?.transactionHistory ?: emptyList()
//                val sortedTransactions = transactions.sortedByDescending { parseDate(it.createdAt) }

//                withContext(Dispatchers.Main) {
//                    val adapter = SingleAssetTransactionAdapter(currencyFilter, sortedTransactions)
//                    binding.singleAssetBalanceTransactionRecyclerView.layoutManager = LinearLayoutManager(this@AssetBalanceActivity)
//                    binding.singleAssetBalanceTransactionRecyclerView.adapter = adapter
//                }
            }
        }
    }
}
