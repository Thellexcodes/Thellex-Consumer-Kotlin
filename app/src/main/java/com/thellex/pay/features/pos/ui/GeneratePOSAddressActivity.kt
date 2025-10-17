package com.thellex.pay.features.pos.ui

import com.thellex.pay.features.auth.viewModel.UserViewModel
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.qrcode.QRCodeWriter
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel
import com.thellex.pay.R
import com.thellex.pay.core.utils.ActivityTracker
import com.thellex.pay.data.model.BlockchainItem
import com.thellex.pay.settings.PaymentType
import com.thellex.pay.features.auth.viewModel.UserViewModelFactory
import com.thellex.pay.core.utils.Helpers
import com.thellex.pay.core.utils.Helpers.applyAdvancedSystemBarInsets
import com.thellex.pay.core.utils.Helpers.disableDecorFitsSystemWindows
import com.thellex.pay.core.utils.Helpers.setTransparentStatusBarWithWhiteIcons
import com.thellex.pay.databinding.ActivityPosAddressGeneratorBinding
import com.thellex.pay.features.wallet.utils.WalletManagerModelFactory
import com.thellex.pay.features.wallet.utils.WalletManagerViewModel
import com.thellex.pay.settings.SupportedBlockchainEnum
import java.util.Locale
import android.graphics.Path
import android.graphics.RectF
import android.graphics.Paint

class GeneratePOSAddressActivity : AppCompatActivity() {
    private lateinit var binding: ActivityPosAddressGeneratorBinding
    private lateinit var topBar: Helpers.TopAppBarController
    private lateinit var paymentType: PaymentType
    private var selectedBlockchain: SupportedBlockchainEnum? = null
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
        ActivityTracker.add(this)
        disableDecorFitsSystemWindows()
        setTransparentStatusBarWithWhiteIcons()
        binding.posAddressGeneratorLayout.applyAdvancedSystemBarInsets()


        // Initialize data from intent
        assetCode = intent.getStringExtra("assetCode") ?: ""
        topBar = Helpers.setupTopAppBar(
            activity = this,
            rootView = findViewById(R.id.pos_address_top_app_bar),
            title = "Deposit ${assetCode.uppercase()}"
        )
        assetCodeChainName = intent.getStringExtra("assetCodeChain") ?: ""
        val typeString = intent.getStringExtra("type")
        paymentType = typeString?.let { PaymentType.valueOf(it) } ?: PaymentType.REQUEST_CRYPTO

        // Setup UI
//        topBar = UiSetupHelper.setupTopBar(this, binding, assetCode)
//        binding.iconQrCodeAsset.setImageResource(Helpers.getIconResIdForToken(assetCode))

        // Initialize ViewModels
        userModel = ViewModelProvider(this, UserViewModelFactory(applicationContext))[UserViewModel::class.java]
        walletManagerViewModel = ViewModelProvider(this, WalletManagerModelFactory(applicationContext))[WalletManagerViewModel::class.java]


        // Observe wallet data and update UI
        walletManagerViewModel.walletBalance.observe(this) { walletDto ->
            val wallet = walletDto?.wallets?.get(assetCode)
            val rawNetworkName = wallet?.network?.name?.lowercase(Locale.getDefault())

            val displayNetworkName = Helpers.formatNetworkName(rawNetworkName)
            binding.networkLabel.text = displayNetworkName.uppercase()

            supportedBlockchains = rawNetworkName?.let {
                try {
                    val chain = SupportedBlockchainEnum.valueOf(it)
                    listOf(BlockchainItem(chain, Helpers.getIconResIdForBlockchain(rawNetworkName)))
                } catch (e: IllegalArgumentException) {
                    emptyList()
                }
            } ?: emptyList()

            walletAddress = wallet?.address ?: "No address found"
            binding.walletAddressText.text = walletAddress

            // Generate and display QR code
            val qrBitmap = QrCodeGenerator.generateCustomQRCode(
                context = this,
                data = walletAddress ?: "no-address",
                widthDp = 323, // Match @dimen/dp_323
                heightDp = 323,
                logoResId = Helpers.getIconResIdForToken(assetCode),
            )
            binding.qrCodeImage.setImageBitmap(qrBitmap)
        }

        // Copy address action
        binding.copyAddressActionLayout.setOnClickListener {
            walletAddress?.takeIf { it != "No address found" }?.let { address ->
                val clipboard = getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
                clipboard.setPrimaryClip(ClipData.newPlainText("Wallet Address", address))
                Toast.makeText(this, "Address copied to clipboard", Toast.LENGTH_SHORT).show()
            } ?: Toast.makeText(this, "Address not available yet", Toast.LENGTH_SHORT).show()
        }
    }
}

object QrCodeGenerator {
    fun generateCustomQRCode(
        context: Context,
        data: String,
        widthDp: Int,
        heightDp: Int,
        logoResId: Int
    ): Bitmap? {
        try {
            // Convert dp to pixels
            val density = context.resources.displayMetrics.density
            val qrSize = (widthDp * density).toInt()
            val logoSize = (55 * density).toInt() // Match @dimen/dp_55

            // Set QR code hints for high error correction
            val hints = hashMapOf<EncodeHintType, Any>()
            hints[EncodeHintType.ERROR_CORRECTION] = ErrorCorrectionLevel.H // High error correction
            hints[EncodeHintType.MARGIN] = 2 // Quiet zone

            // Generate QR code
            val qrCodeWriter = QRCodeWriter()
            val bitMatrix = qrCodeWriter.encode(data, BarcodeFormat.QR_CODE, qrSize, qrSize, hints)
            val qrBitmap = Bitmap.createBitmap(qrSize, qrSize, Bitmap.Config.ARGB_8888)

            // Draw QR code with white lines and transparent background
            for (x in 0 until qrSize) {
                for (y in 0 until qrSize) {
                    qrBitmap.setPixel(x, y, if (bitMatrix[x, y]) Color.WHITE else Color.TRANSPARENT)
                }
            }

            // Load and scale logo
            val logoBitmap = BitmapFactory.decodeResource(context.resources, logoResId)
            val scaledLogo = Bitmap.createScaledBitmap(logoBitmap, logoSize, logoSize, true)

            // Create final bitmap with transparent background
            val finalBitmap = Bitmap.createBitmap(qrSize, qrSize, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(finalBitmap)
            val paint = Paint().apply {
                isAntiAlias = true // Smooth edges
            }

            // Create a rounded rectangle path for clipping
            val cornerRadius = 12 * density // Match @dimen/border_radius (12dp)
            val path = Path()
            val rect = RectF(0f, 0f, qrSize.toFloat(), qrSize.toFloat())
            path.addRoundRect(rect, cornerRadius, cornerRadius, Path.Direction.CW)

            // Clip the canvas to the rounded rectangle
            canvas.clipPath(path)

            // Draw QR code (transparent background)
            canvas.drawBitmap(qrBitmap, 0f, 0f, paint)

            // Draw logo in center
            val logoX = (qrSize - logoSize) / 2f
            val logoY = (qrSize - logoSize) / 2f
            canvas.drawBitmap(scaledLogo, logoX, logoY, paint)

            return finalBitmap
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }
}
