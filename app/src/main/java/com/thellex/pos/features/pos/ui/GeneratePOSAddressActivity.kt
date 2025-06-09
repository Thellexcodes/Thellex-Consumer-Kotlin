package com.thellex.pos.features.pos.ui

import com.thellex.pos.features.auth.viewModel.UserViewModel
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.graphics.Bitmap
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.google.zxing.BarcodeFormat
import com.journeyapps.barcodescanner.BarcodeEncoder
import com.thellex.pos.R
import com.thellex.pos.features.pos.adapters.CryptoSpinnerAdapter
import com.thellex.pos.data.model.BlockchainItem
import com.thellex.pos.data.model.CreateRequestPaymentDto
import com.thellex.pos.network.services.ApiClient
import com.thellex.pos.settings.PaymentType
import com.thellex.pos.settings.SupportedBlockchain
import com.thellex.pos.settings.Token
import com.thellex.pos.features.auth.viewModel.UserViewModelFactory
import com.thellex.pos.core.utils.Helpers
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import okhttp3.OkHttpClient

class GeneratePOSAddressActivity : AppCompatActivity() {
    private lateinit var paymentType: PaymentType
    private lateinit var client: OkHttpClient
    private lateinit var selectedBlockchain: SupportedBlockchain
    private lateinit var viewModel: UserViewModel
    private var walletAddress: String? = null

    private val supportedBlockchains = listOf(
        BlockchainItem(SupportedBlockchain.bep20, R.drawable.icon_bnb_chain)
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pos_address_generator)

        viewModel = ViewModelProvider(
            this,
            UserViewModelFactory(applicationContext)
        )[UserViewModel::class.java]

        client = OkHttpClient.Builder()
            .hostnameVerifier { _, _ -> true }
            .sslSocketFactory(Helpers.createUnsafeSslSocketFactory(), Helpers.createUnsafeTrustManager())
            .build()

        val qrImageView = findViewById<ImageView>(R.id.imageViewDynamicQr)

        val typeString = intent.getStringExtra("type")
        paymentType = typeString?.let { PaymentType.valueOf(it) } ?: PaymentType.REQUEST_CRYPTO

        val qrBitmap = generateQrCode("address", Token.usdt)
        qrImageView.setImageBitmap(qrBitmap)

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

        val backButton = findViewById<ImageView>(R.id.activity_wallet_back_button)
        backButton.setOnClickListener {
            finish()
        }


        val spinner = findViewById<Spinner>(R.id.cryptoAssetSpinner)
        val adapter = CryptoSpinnerAdapter(this, supportedBlockchains)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = adapter

        spinner.setSelection(0)
        selectedBlockchain = supportedBlockchains[0].chain

        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                selectedBlockchain = supportedBlockchains[position].chain
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }

        lifecycleScope.launch {
            val token = withTimeoutOrNull(5000) {
                viewModel.token.first { !it.isNullOrBlank() }
            }
            makeRequestCryptoPayment(token!!, Token.usdt, SupportedBlockchain.bep20)
        }

        val copyAddressLayout = findViewById<LinearLayout>(R.id.copyAddressLayout)
        copyAddressLayout.setOnClickListener {
            walletAddress?.let {
                val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                val clip = ClipData.newPlainText("Wallet Address", it)
                clipboard.setPrimaryClip(clip)

                // Vibrate for a short duration
                val vibrator = getSystemService(Vibrator::class.java)
                vibrator?.let { vib ->
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        vib.vibrate(VibrationEffect.createOneShot(50, VibrationEffect.DEFAULT_AMPLITUDE))
                    } else {
                        vib.vibrate(5)
                    }
                }

                Toast.makeText(this, "Address copied to clipboard", Toast.LENGTH_SHORT).show()
            } ?: Toast.makeText(this, "Address not available yet", Toast.LENGTH_SHORT).show()
        }
    }

    private fun makeRequestCryptoPayment(
        authToken: String,
        assetCode: Token,
        network: SupportedBlockchain,
    ) {
        val paymentRequest = CreateRequestPaymentDto(
            paymentType = PaymentType.REQUEST_CRYPTO,
            assetCode = assetCode,
            network = network
        )

        lifecycleScope.launch {
            try {
                val paymentService = ApiClient.getAuthenticatedPaymentApi(authToken)
                val response = paymentService.requestCryptoPayment(paymentRequest)

                if (response.status) {
                    val result = response.result
                    walletAddress = result?.wallet?.address ?: "Invalid Address"
                    Log.d("WalletAddress", "Received wallet address: $walletAddress")

                    withContext(Dispatchers.Main) {
                        val qrImageView = findViewById<ImageView>(R.id.imageViewDynamicQr)
                        val qrBitmap = generateQrCode(walletAddress ?: "no-address", assetCode)
                        qrImageView.setImageBitmap(qrBitmap)
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(this@GeneratePOSAddressActivity, "Request failed", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@GeneratePOSAddressActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun generateQrCode(data: String, assetType: Token?): Bitmap? {
        val size = 800
        val barcodeEncoder = BarcodeEncoder()
        return barcodeEncoder.encodeBitmap(data, BarcodeFormat.QR_CODE, size, size)
    }
}
