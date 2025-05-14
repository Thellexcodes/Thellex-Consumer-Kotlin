package com.thellex.pos

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class POSActivity : AppCompatActivity() {

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.fragment_p_o_s) // reuse the fragment layout

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
        transactionRecyclerView.adapter = POSTransactionAdapter(transactionList)

        val itemSpacing = resources.getDimensionPixelSize(R.dimen.txn_margin)
        transactionRecyclerView.addItemDecoration(ItemSpacingDecoration(itemSpacing))

        val requestAccessButton = findViewById<LinearLayout>(R.id.request_button)
        requestAccessButton.setOnClickListener {
            startActivity(Intent(this, RequestAssetsActivity::class.java))
        }

        val posQuickRequestBtn = findViewById<LinearLayout>(R.id.pos_quick_request_button)
        posQuickRequestBtn.setOnClickListener {
            startActivity(Intent(this, POSAddressGeneratorActivity::class.java))
        }
    }
}