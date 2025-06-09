package com.thellex.pos.features.pos.ui

import com.thellex.pos.features.auth.viewModel.UserViewModel
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.thellex.pos.core.decorators.ItemSpacingDecoration
import com.thellex.pos.features.pos.adapters.POSTransactionAdapter
import com.thellex.pos.R
import com.thellex.pos.core.utils.Helpers
import com.thellex.pos.core.utils.Helpers.parseDate
import com.thellex.pos.data.model.UserPreferences
import com.thellex.pos.settings.PaymentType
import com.thellex.pos.features.auth.viewModel.UserViewModelFactory
import com.thellex.pos.features.wallet.ui.WalletAssetsActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class POSHomeActivity : AppCompatActivity() {
    private lateinit var viewModel: UserViewModel
    private lateinit var transactionRecyclerView: RecyclerView
    private lateinit var transactionAdapter: POSTransactionAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_p_o_s)

        setupWindowInsetsAndBars()

        viewModel = ViewModelProvider(
            this,
            UserViewModelFactory(applicationContext)
        )[UserViewModel::class.java]

        val businessNameTextView = findViewById<TextView>(R.id.businessNameText)
        businessNameTextView.text = businessNameTextView.text.toString().uppercase()

        setupRecyclerView()

        observeUserTransactions()

        setupClickListeners()
    }

    private fun setupWindowInsetsAndBars() {
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

        val window = window
        WindowCompat.setDecorFitsSystemWindows(window, true)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.statusBarColor = Color.parseColor("#212C2C3A") // 13% alpha
        }
    }

    private fun setupRecyclerView() {
        transactionRecyclerView = findViewById(R.id.transaction_recycler)
        transactionRecyclerView.layoutManager = LinearLayoutManager(this)

        transactionAdapter = POSTransactionAdapter(emptyList()) {}
        transactionRecyclerView.adapter = transactionAdapter

        val itemSpacing = resources.getDimensionPixelSize(R.dimen.txn_margin)
        transactionRecyclerView.addItemDecoration(ItemSpacingDecoration(itemSpacing))
    }

    private fun observeUserTransactions() {
        lifecycleScope.launch {
            UserPreferences.getAuthResult(applicationContext).collect { userEntity ->
                val transactions = userEntity?.transactionHistory ?: emptyList()

                val sortedTransactions = transactions.sortedByDescending { parseDate(it.createdAt) }
                Log.d("RAW_JSON_BODY", sortedTransactions.toString())

                withContext(Dispatchers.Main) {
                    transactionAdapter.updateList(sortedTransactions)
                }
            }
        }
    }

    private fun setupClickListeners() {
        findViewById<LinearLayout>(R.id.pos_withdraw_button).setOnClickListener {
            startActivity(Intent(this, EnterTransactionAmountActivity::class.java).apply {
                putExtra("type", PaymentType.WITHDRAW_FIAT)
            })
        }

        findViewById<LinearLayout>(R.id.pos_request_button).setOnClickListener {
            startActivity(Intent(this, POSChooseCryptoActivity::class.java))
        }

        findViewById<LinearLayout>(R.id.pos_quick_request_button).setOnClickListener {
            startActivity(Intent(this, GeneratePOSAddressActivity::class.java))
        }

        findViewById<ConstraintLayout>(R.id.pos_view_assets_button).setOnClickListener {
            startActivity(Intent(this, WalletAssetsActivity::class.java))
        }
    }

}
