package com.thellex.pos

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ImageView
import android.widget.Spinner
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.zxing.BarcodeFormat
import com.journeyapps.barcodescanner.BarcodeEncoder


class WalletAddressGeneratorDepositorActivity : AppCompatActivity() {
    private lateinit var qrImageView: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pos_address_generator)

        val qrImageView = findViewById<ImageView>(R.id.imageViewDynamicQr)

        val address = "your-wallet-address-or-text-here"
        val assetType = "usdt"

        val qrBitmap = generateQrCodeWithIcon(address, assetType)
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

        val cryptoChains = listOf(
            CryptoAsset("STELLAR", R.drawable.icon_lumens),
            CryptoAsset("BNB CHAIN", R.drawable.icon_bnb_chain),
        )

        val spinner = findViewById<Spinner>(R.id.cryptoAssetSpinner)
        val adapter = CryptoAssetAdapter(this, cryptoChains)
        spinner.adapter = adapter

        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
//                val selectedAsset = cryptoAssets[position]
                // Use selectedAsset.name and selectedAsset.iconRes
                // For example, regenerate your QR code here with the selected asset icon
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }

    }

    private fun generateQrCodeWithIcon(data: String, assetType: String): Bitmap? {
        val size = 800
        val barcodeEncoder = BarcodeEncoder()
        val qrBitmap = barcodeEncoder.encodeBitmap(data, BarcodeFormat.QR_CODE, size, size)

        val iconRes = when (assetType.lowercase()) {
            "usdt" -> R.drawable.icon_usdt
            "usdc" -> R.drawable.icon_usdc
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