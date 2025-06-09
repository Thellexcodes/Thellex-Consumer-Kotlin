package com.thellex.pos.features.wallet.ui

import android.os.Bundle
import android.util.Log
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.thellex.pos.R
import com.thellex.pos.core.utils.Helpers.parseDate
import com.thellex.pos.data.model.UserPreferences
import com.thellex.pos.databinding.ActivitySingleAssetBalanceBinding
import com.thellex.pos.features.auth.viewModel.UserViewModel
import com.thellex.pos.features.auth.viewModel.UserViewModelFactory
import com.thellex.pos.features.wallet.adapters.SingleAssetTransactionAdapter
import com.thellex.pos.features.wallet.fragments.SendBottomSheetFragment
import com.thellex.pos.features.wallet.model.WalletManagerModelFactory
import com.thellex.pos.features.wallet.model.WalletManagerViewModel
import com.thellex.pos.features.wallet.prefrences.WalletManagerPreferences
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

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
                val sortedTransactions = transactions.sortedByDescending { parseDate(it.createdAt) }

                withContext(Dispatchers.Main) {
                    val adapter = SingleAssetTransactionAdapter(currencyFilter, sortedTransactions)
                    binding.singleAssetBalanceTransactionRecyclerView.layoutManager = LinearLayoutManager(this@AssetBalanceActivity)
                    binding.singleAssetBalanceTransactionRecyclerView.adapter = adapter
                }
            }
        }
    }
}
