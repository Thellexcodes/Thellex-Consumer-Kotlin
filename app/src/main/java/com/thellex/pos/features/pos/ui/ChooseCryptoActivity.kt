package com.thellex.pos.features.pos.ui

import com.thellex.pos.features.auth.viewModel.UserViewModel
import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.thellex.pos.core.decorators.ItemSpacingDecoration
import com.thellex.pos.R
import com.thellex.pos.features.pos.adapters.CryptoAdapter
import com.thellex.pos.data.model.Crypto
import com.thellex.pos.settings.PaymentType
import com.thellex.pos.settings.Token
import com.thellex.pos.features.auth.viewModel.UserViewModelFactory
import kotlinx.coroutines.launch

class POSChooseCryptoActivity: AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var cryptoAdapter: CryptoAdapter
    private lateinit var viewModel: UserViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pos_choose_crypto)

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

        viewModel = ViewModelProvider(
            this,
            UserViewModelFactory(applicationContext)
        )[UserViewModel::class.java]

        recyclerView = findViewById(R.id.pos_crypto_list_selection)
        recyclerView.layoutManager = LinearLayoutManager(this)

        cryptoAdapter = CryptoAdapter(cryptoList) { selectedItem ->
            val intent = Intent(this, GeneratePOSAddressActivity::class.java)
            intent.putExtra("type", PaymentType.REQUEST_CRYPTO)
            startActivity(intent)
        }
        recyclerView.adapter = cryptoAdapter

        val spacing = resources.getDimensionPixelSize(R.dimen.txn_margin)
        recyclerView.addItemDecoration(ItemSpacingDecoration(spacing))

        // ✅ Use a coroutine to access DataStore (suspend function)
        lifecycleScope.launch {
            // Do something with qWallet, for example:
//            Log.d("QWALLET", "QID: ${qWallet?.id}, QSN: ${qWallet?.qsn}")
        }

        val backButton = findViewById<ImageView>(R.id.activity_wallet_back_button)
        backButton.setOnClickListener {
            finish()
        }
    }


    private val cryptoList = listOf(
        Crypto(Token.usdt, R.drawable.icon_usdt),
    )

}
