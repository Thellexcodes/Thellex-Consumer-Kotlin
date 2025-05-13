package com.example.thellex

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
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

        recyclerView = findViewById(R.id.pos_crypto_list_selection)
        recyclerView.layoutManager = LinearLayoutManager(this)

        cryptoAdapter = CryptoAdapter(cryptoList) { selectedCrypto ->
                startActivity(Intent(this, POSAddressGeneratorActivity::class.java))
        }
        recyclerView.adapter = cryptoAdapter

        val spacing = resources.getDimensionPixelSize(R.dimen.txn_margin)
        recyclerView.addItemDecoration(ItemSpacingDecoration(spacing))
    }
}
