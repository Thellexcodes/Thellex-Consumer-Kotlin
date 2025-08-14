package com.thellex.payments.core.utils

import android.annotation.SuppressLint
import android.app.Activity
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Build
import android.provider.Settings
import android.text.InputFilter
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.util.Log
import android.util.Patterns
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsAnimationCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.thellex.payments.R
import com.thellex.payments.data.model.PaymentStatusEnum
import com.thellex.payments.data.model.TransactionTypeEnum
import com.thellex.payments.features.dashboard.ui.MainActivity
import com.thellex.payments.settings.FiatTickers
import com.thellex.payments.settings.SupportedBlockchainEnum
import org.json.JSONException
import org.json.JSONObject
import retrofit2.HttpException
import java.math.BigDecimal
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import java.util.regex.Pattern
import kotlin.time.Duration

object Helpers {
    fun parseBackendErrorEnum(errorBody: String?): String? {
        return try {
            val json = JSONObject(errorBody ?: "{}")
            json.optString("message", null.toString())
        } catch (e: JSONException) {
            null
        }
    }

    fun Context.showSingleToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }

    fun getIconResIdForToken(token: String): Int {
        return when (token.lowercase(Locale.getDefault())) {
            "usdt" -> R.drawable.icon_usdt
            "usdc" -> R.drawable.icon_usdc
            "xlm" -> R.drawable.icon_stellar
            else -> R.drawable.icon_txn
        }
    }

    fun getStatusColor(context: Context, status: PaymentStatusEnum): Int {
        return when (status) {
            PaymentStatusEnum.Complete -> ContextCompat.getColor(context, R.color.green)
            PaymentStatusEnum.None -> ContextCompat.getColor(context, R.color.darkBlue)
            PaymentStatusEnum.Confirmed -> ContextCompat.getColor(context, R.color.blue)
            PaymentStatusEnum.Accepted -> ContextCompat.getColor(context, R.color.green)
            PaymentStatusEnum.Done -> ContextCompat.getColor(context, R.color.green)
            PaymentStatusEnum.Processing -> ContextCompat.getColor(context, R.color.orange)
            PaymentStatusEnum.Outbound -> ContextCompat.getColor(context, R.color.purple)
            PaymentStatusEnum.Inbound -> ContextCompat.getColor(context, R.color.darkBlue)
            PaymentStatusEnum.PendingRiskScreening -> ContextCompat.getColor(context, R.color.darkBlue)
            PaymentStatusEnum.Queued -> ContextCompat.getColor(context, R.color.darkBlue)
            PaymentStatusEnum.Sent -> ContextCompat.getColor(context, R.color.green)
            PaymentStatusEnum.Rejected -> ContextCompat.getColor(context, R.color.pinkRed)
            PaymentStatusEnum.Failed -> ContextCompat.getColor(context, R.color.pinkRed)
            PaymentStatusEnum.Unknown -> ContextCompat.getColor(context, R.color.orange)
        }
    }

    fun getIconResIdForBlockchain(chain: String): Int {
        return when (chain.lowercase(Locale.getDefault())) {
            "matic" -> R.drawable.icon_polygon
            else -> R.drawable.icon_bnb_chain
        }
    }

    private fun normalizeStatusForIcon(status: String?): String {
        val normalized = status?.lowercase(Locale.getDefault()) ?: ""
        Log.d("POSTransactionAdaptr", "normalizeStatusForIcon: input=$status, normalized=$normalized")
        return when (normalized) {
            "accepted", "completed", "received",
            TransactionTypeEnum.CRYPTO_TO_FIAT_DEPOSIT.value.lowercase(Locale.getDefault()),
            TransactionTypeEnum.FIAT_TO_CRYPTO_DEPOSIT.value.lowercase(Locale.getDefault()),
            TransactionTypeEnum.CRYPTO_DEPOSIT.value.lowercase(Locale.getDefault()),
            TransactionTypeEnum.FIAT_TO_FIAT_DEPOSIT.value.lowercase(Locale.getDefault()) -> "received"
            TransactionTypeEnum.CRYPTO_TO_FIAT_WITHDRAWAL.value.lowercase(Locale.getDefault()),
            TransactionTypeEnum.FIAT_TO_CRYPTO_WITHDRAWAL.value.lowercase(Locale.getDefault()),
            TransactionTypeEnum.CRYPTO_WITHDRAWAL.value.lowercase(Locale.getDefault()),
            TransactionTypeEnum.FIAT_TO_FIAT_WITHDRAWAL.value.lowercase(Locale.getDefault()) -> "sent"
            else -> {
                "sent"
            }
        }
    }

    fun getStatusIconResId(status: String?): Int {
        val normalizedStatus = normalizeStatusForIcon(status?.lowercase(Locale.getDefault()) ?: "")

        return when (normalizedStatus) {
            "received" -> {
                R.drawable.icon_receive_status
            }
            "sent" -> {
                R.drawable.icon_send_status
            }
            else -> {
                R.drawable.icon_send_status
            }
        }
    }

    fun formatTimestamp(timestamp: String): String {
        return try {
            val parser = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSX", Locale.getDefault())
            parser.timeZone = TimeZone.getTimeZone("UTC")
            val date = parser.parse(timestamp)
            val formatter = SimpleDateFormat("MMM dd, hh:mm a", Locale.getDefault())
            formatter.format(date!!)
        } catch (e: Exception) {
            timestamp
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun formatToDeviceTimeZone(isoUtcString: String): String {
        return try {
            val zonedDateTime = ZonedDateTime.parse(isoUtcString)
                .withZoneSameInstant(ZoneId.systemDefault())
            val formatter = DateTimeFormatter.ofPattern("h:mm a")
            zonedDateTime.format(formatter)
        } catch (e: Exception) { "" }
    }

    fun mapToTransactionStatus(rawStatus: String): PaymentStatusEnum {
        return when (rawStatus.trim().lowercase(Locale.getDefault())) {
            "accepted" -> PaymentStatusEnum.Complete
            "complete" -> PaymentStatusEnum.Complete
            "done" -> PaymentStatusEnum.Complete
            "rejected" -> PaymentStatusEnum.Rejected
            "pending" -> PaymentStatusEnum.Processing
            "failed" -> PaymentStatusEnum.Failed
            else -> PaymentStatusEnum.Processing
        }
    }

    fun formatDecimal(value: String): String {
        // You can customize decimal places or formatting here
        return BigDecimal(value).setScale(2, BigDecimal.ROUND_HALF_UP).toPlainString()
    }

    fun formatAmountWithSymbol(amountStr: String, symbol: String? = null, decimals: Int = 2): String {
        val amount = amountStr.toDoubleOrNull() ?: 0.0
        val symbolToUse = symbol ?: ""
        val formattedAmount = "%.${decimals}f".format(amount)
        return "$symbolToUse$formattedAmount"
    }


    fun parseDate(dateString: String?): Date? {
        return try {
            val formatter = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault())
            formatter.parse(dateString ?: "")
        } catch (e: Exception) {
            null
        }
    }

    @SuppressLint("DefaultLocale")
    fun convertToUsd(currency: String, amountStr: String): String {
        return try {
            val amount = amountStr.toDouble()
            String.format("%.2f", amount)
        } catch (e: Exception) {
            "0.00"
        }
    }

    fun highlightCurrency(textView: TextView, inputText: String, highlightColor: Int) {
        val spannable = SpannableString(inputText)

        val pattern = Pattern.compile("[\\p{Sc}][\\d,]+(?:\\.\\d{2})?")
        val matcher = pattern.matcher(inputText)

        while (matcher.find()) {
            val start = matcher.start()
            val end = matcher.end()

            spannable.setSpan(
                ForegroundColorSpan(highlightColor),
                start,
                end,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
        }
        textView.text = spannable
    }

    fun formatNumberWithCommas(number: Int?): String {
        return if (number != null) {
            NumberFormat.getNumberInstance(Locale.US).format(number)
        } else {
            "N/A"
        }
    }

    fun getErrorMessageFromException(e: Exception): String {
        return if (e is HttpException) {
            val errorBody = e.response()?.errorBody()?.string()
            parseErrorMessage(errorBody) ?: "Network error"
        } else {
            "Network error"
        }
    }

    fun getPaymentStatusColor(status: PaymentStatusEnum): Int {
        return when (status) {
            PaymentStatusEnum.Complete,
            PaymentStatusEnum.Confirmed,
            PaymentStatusEnum.Accepted,
            PaymentStatusEnum.Done,
            PaymentStatusEnum.Sent -> R.color.green

            PaymentStatusEnum.Processing,
            PaymentStatusEnum.Outbound,
            PaymentStatusEnum.Inbound,
            PaymentStatusEnum.PendingRiskScreening,
            PaymentStatusEnum.Queued -> R.color.goldenYellow

            PaymentStatusEnum.Rejected -> R.color.pinkRed
            PaymentStatusEnum.Failed -> R.color.pinkRed

            PaymentStatusEnum.Unknown,
            PaymentStatusEnum.None -> R.color.gray
        }
    }

    private fun parseErrorMessage(errorBody: String?): String? {
        if (errorBody == null) return null
        return try {
            val json = JSONObject(errorBody)
            json.optString("message", null.toString())
        } catch (ex: Exception) {
            null
        }
    }

    fun formatCurrencyWithNGN(number: Int?): String {
        val currencySymbol = FiatTickers.getByCodeOrCountry("ngn")?.symbol ?: "NGN"
        return if (number != null) {
            "$currencySymbol${NumberFormat.getNumberInstance(Locale.US).format(number)}"
        } else {
            "$currencySymbol N/A"
        }
    }

    fun isValidEmail(email: String): Boolean {
        return Patterns.EMAIL_ADDRESS.matcher(email.trim()).matches()
    }

    fun applyEmailCharacterFilter(editText: EditText) {
        val allowedPattern = "[a-zA-Z0-9@._-]+"
        val emailFilter = InputFilter { source, _, _, _, _, _ ->
            if (source.toString().matches(allowedPattern.toRegex())) source else ""
        }
        editText.filters = arrayOf(emailFilter)
    }

    fun isValidEvmAddress(address: String): Boolean {
        val regex = Regex("^0x[a-fA-F0-9]{40}$")
        return regex.matches(address)
    }

    fun formatBalance(amount: String?): String {
        return try {
            val value = amount?.toDoubleOrNull() ?: 0.0
            String.format("%.2f", value)
        } catch (e: Exception) {
            "0.00"
        }
    }

    fun getFriendlyLabel(kind: String): String {
        return when (kind) {
            "CRYPTO_DEPOSIT_SUCCESSFUL" -> "DEPOSIT"
            "CRYPTO_WITHDRAWAL_SUCCESSFUL" -> "WITHDRAWAL"
            "CRYPTO_WITHDRAWAL_FAILED" -> "WITHDRAWAL FAILED"
            "PAYMENT_RECEIVED" -> "PAYMENT RECEIVED"
            "PAYMENT_FAILED" -> "PAYMENT FAILED"
            "PAYMENT_PENDING" -> "PAYMENT PENDING"
            "ORDER_CREATED" -> "ORDER CREATED"
            "ORDER_COMPLETED" -> "ORDER COMPLETED"
            "ORDER_CANCELLED" -> "ORDER CANCELLED"
            "POS_SESSION_STARTED" -> "POS SESSION STARTED"
            "POS_SESSION_ENDED" -> "POS SESSION ENDED"
            "POS_DEVICE_CONNECTED" -> "POS CONNECTED"
            "POS_DEVICE_DISCONNECTED" -> "POS DISCONNECTED"
            "NEW_DEVICE_REGISTERED" -> "NEW DEVICE"
            "SYSTEM_MAINTENANCE" -> "MAINTENANCE"
            else -> kind.replace('_', ' ').uppercase()
        }
    }

    fun String.truncateMiddle(startChars: Int = 4, endChars: Int = 4, delimiter: String = "....."): String {
        if (this.length <= startChars + endChars) return this
        return this.take(startChars) + delimiter + this.takeLast(endChars)
    }

    fun showSystemNotification(context: Context, title: String, message: String) {
        val channelId = "my_channel_id"
        val notificationId = 1001

        // Intent to open the app when notification is tapped
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Create NotificationChannel (Android 8.0+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Notifications"
            val descriptionText = "Shows important app updates"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(channelId, name, importance).apply {
                description = descriptionText
            }
            notificationManager.createNotificationChannel(channel)
        }

        // Optional: setLargeIcon for legacy support (Android < 5.0)
        val largeIcon = BitmapFactory.decodeResource(context.resources, R.drawable.ic_thellex_logo_x)

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_thellex_logo_x)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setLargeIcon(largeIcon)
            .build()

        notificationManager.notify(notificationId, notification)
    }

    fun Button.setSubmitting(
        submitting: Boolean,
        loadingText: String = "Submitting...",
        defaultText: String = "Submit",
        submittingBackgroundRes: Int = R.drawable.rounded_border_button_darkblue,
        defaultBackgroundRes: Int = R.drawable.rounded_border_button_golden,
        submittingTextColor: Int = R.color.white,
        defaultTextColor: Int = R.color.darkBlue
    ) {
        isEnabled = !submitting
        text = if (submitting) loadingText else defaultText
        setBackgroundResource(if (submitting) submittingBackgroundRes else defaultBackgroundRes)
        setTextColor(ContextCompat.getColor(context, if (submitting) submittingTextColor else defaultTextColor))
    }

    fun Button.setLoading(
        isLoading: Boolean,
        loadingBackgroundRes: Int = R.drawable.rounded_border_button_darkblue,
        defaultBackgroundRes: Int = R.drawable.rounded_border_button_golden
    ) {
        isEnabled = !isLoading
        setBackgroundResource(if (isLoading) loadingBackgroundRes else defaultBackgroundRes)
    }

    /**
     * Applies advanced system bar insets padding to this View (top + bottom).
     *
     * @param extraTopPaddingDp Extra fallback padding in dp to add to top inset (default 12dp)
     * @param extraBottomPaddingDp Extra fallback padding in dp to add to bottom inset (default 12dp)
     * @param fixedHorizontalPaddingDp Fixed horizontal padding in dp (default 20dp)
     */
    fun View.applyAdvancedSystemBarInsets(
        extraTopPaddingDp: Int = 12,
        extraBottomPaddingDp: Int = 12,
        fixedHorizontalPaddingDp: Int = 15,
    ) {
        val density = resources.displayMetrics.density
        val extraTopPaddingPx = (extraTopPaddingDp * density).toInt()
        val extraBottomPaddingPx = (extraBottomPaddingDp * density).toInt()
        val fixedHorizontalPaddingPx = (fixedHorizontalPaddingDp * density).toInt()

        val windowInsetsType = WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.navigationBars()

        ViewCompat.setOnApplyWindowInsetsListener(this) { v, insets ->
            val systemBarsInsets = insets.getInsets(windowInsetsType)

            val paddingTop = systemBarsInsets.top + extraTopPaddingPx
            val paddingBottom = systemBarsInsets.bottom + extraBottomPaddingPx

            v.setPadding(
                fixedHorizontalPaddingPx,
                paddingTop,
                fixedHorizontalPaddingPx,
                paddingBottom
            )
            insets
        }

        ViewCompat.setWindowInsetsAnimationCallback(this, object : WindowInsetsAnimationCompat.Callback(
            WindowInsetsAnimationCompat.Callback.DISPATCH_MODE_CONTINUE_ON_SUBTREE
        ) {
            override fun onProgress(
                insets: WindowInsetsCompat,
                runningAnimations: List<WindowInsetsAnimationCompat>
            ): WindowInsetsCompat {
                val systemBarsInsets = insets.getInsets(windowInsetsType)

                val paddingTop = systemBarsInsets.top + extraTopPaddingPx
                val paddingBottom = systemBarsInsets.bottom + extraBottomPaddingPx

                setPadding(
                    fixedHorizontalPaddingPx,
                    paddingTop,
                    fixedHorizontalPaddingPx,
                    paddingBottom
                )
                return insets
            }
        })
    }


    fun Activity.disableDecorFitsSystemWindows() {
        WindowCompat.setDecorFitsSystemWindows(window, false)
    }

    fun Activity.setTransparentStatusBarWithWhiteIcons() {
        // Allow content to draw behind system bars
        WindowCompat.setDecorFitsSystemWindows(window, false)

        // Use WindowInsetsControllerCompat to set icon colors
        WindowInsetsControllerCompat(window, window.decorView).apply {
            isAppearanceLightStatusBars = false   // false = white status bar icons
            isAppearanceLightNavigationBars = false  // false = white nav bar icons
        }
    }

    fun Context.copyToClipboard(label: String, text: String) {
        if (text.isNotEmpty()) {
            val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText(label, text)
            clipboard.setPrimaryClip(clip)
            CustomToast.show(this, label, "$label copied to clipboard")
        } else {
            CustomToast.show(this, "Empty", "Nothing to copy")
        }
    }

    // Reusable Top App Bar Setup
    fun setupTopAppBar(
        activity: Activity,
        rootView: View,
        title: String,
        onBackClick: (() -> Unit)? = null
    ): TopAppBarController {
        val titleTextView = rootView.findViewById<TextView>(R.id.text_title)
        val backButton = rootView.findViewById<ImageView>(R.id.button_back)

        titleTextView.text = title
        backButton.setOnClickListener {
            onBackClick?.invoke() ?: activity.finish()
        }

        return TopAppBarController(titleTextView)
    }

    class TopAppBarController(private val titleTextView: TextView) {
        fun updateTitle(newTitle: String) {
            titleTextView.text = newTitle
        }
    }

    fun getTransactionAction(transactionType: TransactionTypeEnum): String {
        return when (transactionType) {
            TransactionTypeEnum.FIAT_TO_CRYPTO_DEPOSIT -> "BUY"
            TransactionTypeEnum.CRYPTO_TO_FIAT_WITHDRAWAL -> "SELL"
            else -> transactionType.name
                .replace("_", " ")
                .lowercase()
                .replaceFirstChar { it.uppercase() }
        }
    }

    fun formatToTwoDecimalPlaces(value: Double?): String {
        return if (value != null) String.format("%.2f", value) else "0.00"
    }

    fun Double?.toTwoDecimalString(): String {
        return if (this != null) String.format("%.2f", this) else "0.00"
    }

    fun String.capitalizeFirst(): String = replaceFirstChar { it.uppercase() }

    fun <T> firstMatchingEnum(event: String?, vararg resolvers: (String) -> T?): T? {
        if (event.isNullOrBlank()) {
            println("Error: Event string is null or blank")
            return null
        }
        for (resolver in resolvers) {
            val result = resolver(event)
            if (result != null) {
                println("Resolved event '$event' to $result")
                return result
            } else {
                println("Resolver $resolver returned null for event '$event'")
            }
        }
        println("No resolver matched event '$event'")
        return null
    }

    fun formatNetworkName(rawNetworkName: String?): String {
        return when (rawNetworkName?.lowercase(Locale.getDefault())) {
            "matic" -> "Polygon PoS"
            "bep20" -> "Binance Smart Chain"
            "eth" -> "Ethereum"
            "sol" -> "Solana"
            "avax" -> "Avalanche"
            "tron" -> "Tron"
            else -> rawNetworkName?.replaceFirstChar { it.uppercase() } ?: "Unknown"
        }
    }

    // Optional: You can customize display names here
    fun getDisplayNameForNetwork(network: SupportedBlockchainEnum): String {
        return when (network) {
            SupportedBlockchainEnum.bep20 -> "Binance Smart Chain"
            SupportedBlockchainEnum.matic -> "Polygon PoS"
            SupportedBlockchainEnum.stellar -> "Stellar"
            SupportedBlockchainEnum.base -> "Base"
            SupportedBlockchainEnum.lisk -> "Lisk"
        }
    }

    fun abbreviateAddress(address: String?, startLength: Int = 4, endLength: Int = 4): String {
        if (address.isNullOrBlank()) return ""
        if (address.length <= startLength + endLength) return address
        val start = address.take(startLength)
        val end = address.takeLast(endLength)
        return "$start...$end"
    }

    @SuppressLint("DefaultLocale")
    fun Double.roundToTwoDecimals(): Double {
        return String.format("%.2f", this).toDouble()
    }

    @SuppressLint("HardwareIds")
    fun deviceId(context: Context): String =
        Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID) ?: "unknown"
}

