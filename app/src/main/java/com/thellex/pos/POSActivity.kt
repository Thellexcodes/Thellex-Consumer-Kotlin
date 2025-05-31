package com.thellex.pos

import UserViewModel
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.thellex.pos.data.model.PaymentType

class POSActivity : AppCompatActivity() {
    private lateinit var viewModel: UserViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_p_o_s)

        viewModel = UserViewModel(applicationContext)

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

        // Show system bars
        WindowCompat.setDecorFitsSystemWindows(window, true)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            // Reduce status and nav bar opacity
            window.statusBarColor = Color.parseColor("#212C2C3A") // 13% alpha
//            window.navigationBarColor = Color.parseColor("#ffffff") // same
        }

        val businessNameTextView = findViewById<TextView>(R.id.pos_business_name)
        businessNameTextView.text = businessNameTextView.text.toString().uppercase()

        val transactionRecyclerView = findViewById<RecyclerView>(R.id.transaction_recycler)
        transactionRecyclerView.layoutManager = LinearLayoutManager(this)

        val transactionList = listOf(
            PosTransaction(R.drawable.icon_usdt, "USDT", "FARIDA ABDUL", "Today, 10:09 AM", "20 USDT", "$3,890.0938", R.drawable.icon_receive_status),
            PosTransaction(R.drawable.icon_usdc, "USDC", "FARIDA ABDUL", "Today, 10:09 AM", "10 USDC", "$3,890.0938", R.drawable.icon_receive_status),
            PosTransaction(R.drawable.icon_usdt, "USDT", "ALARA Moyo", "Today, 10:09 AM", "20 USDT", "$3,890.0938", R.drawable.icon_send_status),
            PosTransaction(R.drawable.icon_usdc, "USDC", "FARIDA ABDUL", "Today, 10:09 AM", "20 USDC", "$3,890.0938", R.drawable.icon_send_status)
        )
        transactionRecyclerView.adapter = POSTransactionAdapter(transactionList){}

        val itemSpacing = resources.getDimensionPixelSize(R.dimen.txn_margin)
        transactionRecyclerView.addItemDecoration(ItemSpacingDecoration(itemSpacing))

        val withdrawFundsButton = findViewById<LinearLayout>(R.id.pos_withdraw_button)
        withdrawFundsButton.setOnClickListener {
            val intent = Intent(this, RequestAmountActivity::class.java)
            intent.putExtra("type", PaymentType.WITHDRAW_FIAT)
            startActivity(intent)
        }

        val requestAccessButton = findViewById<LinearLayout>(R.id.pos_request_button)
        requestAccessButton.setOnClickListener {
            startActivity(Intent(this, RequestAssetsActivity::class.java))
        }

        val posQuickRequestBtn = findViewById<LinearLayout>(R.id.pos_quick_request_button)
        posQuickRequestBtn.setOnClickListener {
            startActivity(Intent(this, WalletAddressGeneratorDepositorActivity::class.java))
        }

        val viewAssetsContainer = findViewById<ConstraintLayout>(R.id.pos_view_assets_button)
        viewAssetsContainer.setOnClickListener {
            startActivity(Intent(this, WalletAssetsActivity::class.java))
        }

        //TODO: trigger create account for user
    }
}