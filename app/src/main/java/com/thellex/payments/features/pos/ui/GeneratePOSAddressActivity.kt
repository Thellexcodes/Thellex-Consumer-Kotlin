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
import com.thellex.payments.databinding.ActivityPosAddressGeneratorBinding
import com.thellex.payments.features.wallet.utils.WalletManagerModelFactory
import com.thellex.payments.features.wallet.utils.WalletManagerViewModel
import java.util.Locale

class GeneratePOSAddressActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPosAddressGeneratorBinding

    private lateinit var paymentType: PaymentType
    private var selectedBlockchain: SupportedBlockchain? = null
    private lateinit var userModel: UserViewModel
    private lateinit var walletManagerViewModel: WalletManagerViewModel
    private var supportedBlockchains: List<BlockchainItem> = emptyList()

    private var walletAddress: String? = null
    private var assetCode: String = ""
    private var assetCodeChainName: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPosAddressGeneratorBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Get intent data safely
        assetCode = intent.getStringExtra("assetCode") ?: ""
        assetCodeChainName = intent.getStringExtra("assetCodeChain") ?: ""
        val typeString = intent.getStringExtra("type")

        // Set text/icon
        binding.qrCodeAssetCodeName.text = assetCode.uppercase()
        val assetCodeIconResId = Helpers.getIconResIdForToken(assetCode)
        binding.iconQrCodeAsset.setImageResource(assetCodeIconResId)

        // ViewModels
        userModel = ViewModelProvider(this, UserViewModelFactory(applicationContext))[UserViewModel::class.java]
        walletManagerViewModel = ViewModelProvider(this, WalletManagerModelFactory(applicationContext))[WalletManagerViewModel::class.java]

        // Payment type
        paymentType = typeString?.let { PaymentType.valueOf(it) } ?: PaymentType.REQUEST_CRYPTO

        // Back button
        binding.activityWalletBackButton.setOnClickListener { finish() }

        // Setup spinner listener once
        setupSpinner()

        // Observe wallet balance and update UI accordingly
        walletManagerViewModel.walletBalance.observe(this) { walletDto ->
            val wallet = walletDto.wallets[assetCode]
            val networkName = wallet?.network?.lowercase(Locale.getDefault())


            val supportedChain = networkName?.let {
                try {
                    SupportedBlockchain.valueOf(it.lowercase())
                } catch (e: IllegalArgumentException) { null }
            }

            supportedChain?.let {
                supportedBlockchains = listOf(
                    BlockchainItem(it, Helpers.getIconResIdForBlockchain(networkName))
                )
            } ?: run {
                supportedBlockchains = emptyList()
            }

            // Update spinner adapter
            val adapter = CryptoSpinnerAdapter(this, supportedBlockchains)
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            binding.cryptoAssetSpinner.adapter = adapter

            if (supportedBlockchains.isNotEmpty()) {
                binding.cryptoAssetSpinner.setSelection(0)
                selectedBlockchain = supportedBlockchains[0].chain
            } else {
                selectedBlockchain = null
            }

            // Update wallet address and QR code safely
            walletAddress = wallet?.address
            if (walletAddress.isNullOrEmpty()) {
                walletAddress = "No address found"
            }

            val qrBitmap = generateQrCode(walletAddress ?: "no-address")
            binding.imageViewDynamicQr.setImageBitmap(qrBitmap)
        }

        // Copy address logic
        binding.copyAddressLayout.setOnClickListener {
            walletAddress?.takeIf { it != "No address found" }?.let { address ->
                val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                clipboard.setPrimaryClip(ClipData.newPlainText("Wallet Address", address))
                Toast.makeText(this, "Address copied to clipboard", Toast.LENGTH_SHORT).show()
            } ?: run {
                Toast.makeText(this, "Address not available yet", Toast.LENGTH_SHORT).show()
            }
        }

        // Handle system bar insets for better UI
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

    private fun setupSpinner() {
        binding.cryptoAssetSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                selectedBlockchain = supportedBlockchains.getOrNull(position)?.chain
                Log.d("GeneratePOSAddress", "Spinner selected blockchain: $selectedBlockchain")
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                // Optional: handle no selection case
            }
        }
    }

    private fun generateQrCode(data: String): Bitmap? {
        val size = 800
        return try {
            BarcodeEncoder().encodeBitmap(data, BarcodeFormat.QR_CODE, size, size)
        } catch (e: Exception) {
            Log.e("GeneratePOSAddress", "Failed to generate QR code", e)
            null
        }
    }
}
