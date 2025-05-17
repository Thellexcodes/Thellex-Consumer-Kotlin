package com.thellex.pos

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class POSChooseCryptoActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var cryptoAdapter: CryptoAdapter


    private val cryptoList = listOf(
        Crypto("Bitcoin", R.drawable.icon_bitcoin),
        Crypto("USDC", R.drawable.icon_usdc),
        Crypto("USDT", R.drawable.icon_usdt),
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pos_choose_crypto) // Use activity layout instead of fragment layout

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

        recyclerView = findViewById(R.id.pos_crypto_list_selection)
        recyclerView.layoutManager = LinearLayoutManager(this)

        cryptoAdapter = CryptoAdapter(cryptoList) { selectedCrypto ->
                startActivity(Intent(this, WalletAddressGeneratorDepositorActivity::class.java))
        }
        recyclerView.adapter = cryptoAdapter

        val spacing = resources.getDimensionPixelSize(R.dimen.txn_margin)
        recyclerView.addItemDecoration(ItemSpacingDecoration(spacing))
    }
}
