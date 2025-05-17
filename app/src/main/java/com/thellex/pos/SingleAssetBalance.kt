package com.thellex.pos

import android.content.Intent
import android.os.Bundle
import android.widget.LinearLayout
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.thellex.pos.databinding.ActivitySingleAssetBalanceBinding

class SingleAssetBalance : AppCompatActivity() {

    private lateinit var binding: ActivitySingleAssetBalanceBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivitySingleAssetBalanceBinding.inflate(layoutInflater)
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

        val transactionList = listOf(
            PosTransaction(R.drawable.icon_txn, "USDT", "FARIDA ABDUL", "Today, 10:09 AM", "20 USDT", "$3,890.0938", R.drawable.icon_receive_status),
            PosTransaction(R.drawable.icon_txn, "USDC", "FARIDA ABDUL", "Today, 10:09 AM", "10 USDC", "$3,890.0938", R.drawable.icon_receive_status),
            PosTransaction(R.drawable.icon_txn, "USDT", "ALARA Moyo", "Today, 10:09 AM", "20 USDT", "$3,890.0938", R.drawable.icon_send_status),
            PosTransaction(R.drawable.icon_txn, "USDC", "FARIDA ABDUL", "Today, 10:09 AM", "20 USDC", "$3,890.0938", R.drawable.icon_send_status)
        )

        binding.singleAssetBalanceTransactionRecyclerView.layoutManager = LinearLayoutManager(this)

        binding.singleAssetBalanceTransactionRecyclerView.adapter = POSTransactionAdapter(transactionList) { }

        val itemSpacing = resources.getDimensionPixelSize(R.dimen.txn_margin)
        binding.singleAssetBalanceTransactionRecyclerView.addItemDecoration(ItemSpacingDecoration(itemSpacing))

        binding.singleAssetBalanceDepositBtn.setOnClickListener {
            startActivity(Intent(this, SingleAssetDeposit::class.java))
        }
    }
}
