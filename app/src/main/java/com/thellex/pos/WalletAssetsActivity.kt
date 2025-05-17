package com.thellex.pos

import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class WalletAssetsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_wallet_assets)

        val rootView = findViewById<View>(android.R.id.content)
        val statusBarHeight = resources.getIdentifier("status_bar_height", "dimen", "android").let { resId ->
            if (resId > 0) resources.getDimensionPixelSize(resId) else 0
        }
        val navBarHeight = resources.getIdentifier("navigation_bar_height", "dimen", "android").let { resId ->
            if (resId > 0) resources.getDimensionPixelSize(resId) else 0
        }

        // Apply bottom padding equal to navigation bar height
        rootView.setPadding(
            rootView.paddingLeft,
            statusBarHeight,
            rootView.paddingRight,
            navBarHeight
        )

        val sampleAssets = listOf(
            Asset("USDT", "3.874", "17,267.07", "24,000.00", R.drawable.icon_usdt),
            Asset("USDC", "12.450", "11,390.50", "15,800.00", R.drawable.icon_usdc),
            Asset("PYUSD", "0.500", "500.00", "750.00", R.drawable.icon_pyusd),
            Asset("USD", "100.00", "100.00", "150,000.00", R.drawable.icon_usd),
        )

        // Initialize RecyclerView
        val recyclerViewWalletAssets = findViewById<RecyclerView>(R.id.activity_wallet_assets_recycler)
        val walletAssetsAdapter = AssetAdapter(sampleAssets)
        recyclerViewWalletAssets.layoutManager = LinearLayoutManager(this)
        recyclerViewWalletAssets.adapter = walletAssetsAdapter

        val itemSpacing = resources.getDimensionPixelSize(R.dimen.margin_2dp)
        recyclerViewWalletAssets.addItemDecoration(ItemSpacingDecoration(itemSpacing))
    }
}