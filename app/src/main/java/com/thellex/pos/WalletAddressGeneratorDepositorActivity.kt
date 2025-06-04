package com.thellex.pos

import UserViewModel
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.google.zxing.BarcodeFormat
import com.journeyapps.barcodescanner.BarcodeEncoder
import com.thellex.pos.data.model.BlockchainItem
import com.thellex.pos.data.model.CreateRequestPaymentDto
import com.thellex.pos.services.ApiClient
import com.thellex.pos.settings.PaymentType
import com.thellex.pos.settings.SupportedBlockchain
import com.thellex.pos.settings.Token
import com.thellex.pos.ui.login.UserViewModelFactory
import com.thellex.pos.utils.Helpers
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import okhttp3.OkHttpClient

class WalletAddressGeneratorDepositorActivity : AppCompatActivity() {
    private lateinit var paymentType: PaymentType
    private lateinit var client: OkHttpClient
    private lateinit var selectedBlockchain: SupportedBlockchain
    private lateinit var viewModel: UserViewModel

    private val SUPPORTED_BLOCKCHAINS = listOf(
        BlockchainItem(SupportedBlockchain.BEP20, R.drawable.icon_bnb_chain)
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

        val qrBitmap = generateQrCodeWithIcon("address", Token.USDT)
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
        val adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            SUPPORTED_BLOCKCHAINS
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = adapter

        // Set default selection and update the global variable
        spinner.setSelection(0)
        selectedBlockchain = SupportedBlockchain.BEP20

        // Handle selection changes
        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                selectedBlockchain = SUPPORTED_BLOCKCHAINS[position].chain
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }

        lifecycleScope.launch {
            val token = withTimeoutOrNull(5000) {
                viewModel.token.first { !it.isNullOrBlank() }
            }

            makeRequestCryptoPayment(token!!, Token.USDT, SupportedBlockchain.BEP20)
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
                    val responseBody = response.result
                    withContext(Dispatchers.Main) {
                        Toast.makeText(this@WalletAddressGeneratorDepositorActivity, "Payment request successful", Toast.LENGTH_SHORT).show()
                    }
                } else {
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@WalletAddressGeneratorDepositorActivity, "Network Error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun generateQrCodeWithIcon(data: String, assetType: Token): Bitmap? {
        val size = 800
        val barcodeEncoder = BarcodeEncoder()
        val qrBitmap = barcodeEncoder.encodeBitmap(data, BarcodeFormat.QR_CODE, size, size)

        val iconRes = when (assetType) {
            Token.USDT -> R.drawable.icon_usdt
            Token.USDC -> R.drawable.icon_usdc
            else -> R.drawable.icon_usdt
        }

        val overlayBitmap = BitmapFactory.decodeResource(resources, iconRes)
        return overlayCenterIcon(qrBitmap, overlayBitmap)
    }

    private fun overlayCenterIcon(qr: Bitmap, icon: Bitmap): Bitmap? {
        val qrSize = qr.width
        val overlaySize = qrSize / 5

        val resizedIcon = Bitmap.createScaledBitmap(icon, overlaySize, overlaySize, true)
        val combined = qr.config?.let { Bitmap.createBitmap(qrSize, qrSize, it) }

        val canvas = combined?.let { Canvas(it) }
        canvas?.drawBitmap(qr, 0f, 0f, null)

        val left = (qrSize - overlaySize) / 2f
        val top = (qrSize - overlaySize) / 2f
        canvas?.drawBitmap(resizedIcon, left, top, null)

        return combined
    }



}
