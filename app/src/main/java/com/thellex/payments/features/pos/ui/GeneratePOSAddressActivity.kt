package com.thellex.payments.features.pos.ui

import com.thellex.payments.features.auth.viewModel.UserViewModel
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.graphics.Bitmap
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import com.google.zxing.BarcodeFormat
import com.journeyapps.barcodescanner.BarcodeEncoder
import com.thellex.payments.R
import com.thellex.payments.features.pos.adapters.CryptoSpinnerAdapter
import com.thellex.payments.data.model.BlockchainItem
import com.thellex.payments.settings.PaymentType
import com.thellex.payments.settings.SupportedBlockchain
import com.thellex.payments.features.auth.viewModel.UserViewModelFactory
import com.thellex.payments.core.utils.Helpers
import com.thellex.payments.features.wallet.utils.WalletManagerModelFactory
import com.thellex.payments.features.wallet.utils.WalletManagerViewModel
import java.util.Locale

class GeneratePOSAddressActivity : AppCompatActivity() {

    private lateinit var paymentType: PaymentType
    private lateinit var selectedBlockchain: SupportedBlockchain
    private lateinit var userModel: UserViewModel
    private lateinit var walletManagerViewModel: WalletManagerViewModel
    private lateinit var supportedBlockchains: List<BlockchainItem>

    private var walletAddress: String? = null
    private var assetCode: String = ""
    private var assetCodeChainName: String = ""

    private lateinit var qrImageView: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pos_address_generator)

        // Get references
        qrImageView = findViewById(R.id.imageViewDynamicQr)
        val backButton = findViewById<ImageView>(R.id.activity_wallet_back_button)
        val spinner = findViewById<Spinner>(R.id.cryptoAssetSpinner)
        val copyAddressLayout = findViewById<LinearLayout>(R.id.copyAddressLayout)
        val assetCodeTextView = findViewById<TextView>(R.id.qr_code_asset_code_name)
        val assetCodeIconView = findViewById<ImageView>(R.id.icon_qr_code_asset)

        // Get intent data safely
        assetCode = intent.getStringExtra("assetCode") ?: ""
        assetCodeChainName = intent.getStringExtra("assetCodeChain") ?: ""
        val typeString = intent.getStringExtra("type")

        // Set text/icon
        assetCodeTextView.text = assetCode.uppercase()
        val assetCodeIconResId = Helpers.getIconResIdForToken(assetCode)
        assetCodeIconView.setImageResource(assetCodeIconResId)

        // ViewModels
        userModel = ViewModelProvider(this, UserViewModelFactory(applicationContext))[UserViewModel::class.java]
        walletManagerViewModel = ViewModelProvider(this, WalletManagerModelFactory(applicationContext))[WalletManagerViewModel::class.java]

        // Payment type
        paymentType = typeString?.let { PaymentType.valueOf(it) } ?: PaymentType.REQUEST_CRYPTO

        // Back button
        backButton.setOnClickListener { finish() }

        // Initialize empty supportedBlockchains list
        supportedBlockchains = emptyList()

        // Observe wallet balance and dynamically build supportedBlockchains
        walletManagerViewModel.walletBalance.observe(this) { walletDto ->
            // Extract unique chain names from wallet.network (string, not array anymore)
            val chainNames = walletDto.wallets.values
                .map { it.network.lowercase(Locale.getDefault()) }
                .distinct()

            Log.d("TAG", "ChainNames $chainNames")

            // Map to BlockchainItem list, filtering unsupported chains gracefully
//            supportedBlockchains = chainNames.mapNotNull { chainName ->
//                try {
//                    val chainEnum = SupportedBlockchain.valueOf(chainName.uppercase())
//                    BlockchainItem(chainEnum, Helpers.getIconResIdForBlockchain(chainName))
//                } catch (e: IllegalArgumentException) {
//                    null
//                }
//            }
//
//            // Update spinner adapter with new supported blockchains
//            val adapter = CryptoSpinnerAdapter(this, supportedBlockchains)
//            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
//            spinner.adapter = adapter
//
//            // Select the blockchain matching assetCodeChainName or fallback to first
//            val defaultIndex = supportedBlockchains.indexOfFirst {
//                it.chain.name.equals(assetCodeChainName, ignoreCase = true)
//            }.takeIf { it >= 0 } ?: 0
//
//            spinner.setSelection(defaultIndex)
//            selectedBlockchain = supportedBlockchains[defaultIndex].chain
//
//            // Spinner selection listener
//            spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
//                override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
//                    selectedBlockchain = supportedBlockchains[position].chain
//                }
//
//                override fun onNothingSelected(parent: AdapterView<*>) {}
//            }
//
//            // Update wallet address and QR code for the selected assetCode
//            val wallet = walletDto.wallets[assetCode]
//            walletAddress = wallet?.address ?: "No address found"
//            val qrBitmap = generateQrCode(walletAddress!!)
//            qrImageView.setImageBitmap(qrBitmap)
        }


        // Copy address logic
        copyAddressLayout.setOnClickListener {
            walletAddress?.let { address ->
                val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                clipboard.setPrimaryClip(ClipData.newPlainText("Wallet Address", address))
                Toast.makeText(this, "Address copied to clipboard", Toast.LENGTH_SHORT).show()
            } ?: Toast.makeText(this, "Address not available yet", Toast.LENGTH_SHORT).show()
        }

        // Handle system bar insets
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
    }

    private fun generateQrCode(data: String): Bitmap? {
        val size = 800
        return BarcodeEncoder().encodeBitmap(data, BarcodeFormat.QR_CODE, size, size)
    }
}

