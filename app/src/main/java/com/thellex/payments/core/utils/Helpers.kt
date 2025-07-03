package com.thellex.payments.core.utils

import android.annotation.SuppressLint
import android.content.Context
import android.text.InputFilter
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.util.Patterns
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import com.thellex.payments.R
import com.thellex.payments.data.model.PaymentStatus
import org.json.JSONException
import org.json.JSONObject
import retrofit2.HttpException
import java.math.BigDecimal
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import java.util.regex.Pattern

object Helpers {
    public fun getNavigationBarHeight(context: Context): Int {
        val resources = context.resources
        val resourceId = resources.getIdentifier("navigation_bar_height", "dimen", "android")
        return if (resourceId > 0) resources.getDimensionPixelSize(resourceId) else 0
    }

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

    fun getIconResIdForBlockchain(chain: String): Int {
        return when (chain.lowercase(Locale.getDefault())) {
            "matic" -> R.drawable.icon_polygon
            else -> R.drawable.icon_bnb_chain
        }
    }

    fun getDisplayNameForNetwork(network: String): String {
        return when (network.lowercase(Locale.getDefault())) {
            "matic" -> "Polygon"
            "bep20" -> "Binance"
            else -> network.replaceFirstChar { it.uppercase() }
        }
    }


    fun getStatusIconResId(status: String?): Int {
        return when (normalizeStatusForIcon(status)) {
            "received" -> R.drawable.icon_receive_status
            else -> R.drawable.icon_send_status
        }
    }

    fun formatTimestamp(timestamp: String): String {
        return try {
            val parser = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSX", Locale.getDefault())
            parser.timeZone = TimeZone.getTimeZone("UTC")
            val date = parser.parse(timestamp)
            val formatter = SimpleDateFormat("MMM dd, hh:mm a", Locale.getDefault())
            formatter.format(date)
        } catch (e: Exception) {
            timestamp
        }
    }

    fun mapToTransactionStatus(rawStatus: String): PaymentStatus {
        return when (rawStatus.trim().lowercase(Locale.getDefault())) {
            "accepted" -> PaymentStatus.Complete
            "done" -> PaymentStatus.Complete
            "rejected" -> PaymentStatus.Rejected
            "pending" -> PaymentStatus.Processing
            else -> PaymentStatus.Processing
        }
    }

    private fun normalizeStatusForIcon(status: String?): String {
        return when (status?.lowercase(Locale.getDefault())) {
            "accepted" -> "received"
            "completed" -> "received"
            "received" -> "received"
            else -> "sent"
        }
    }

    fun formatDecimal(value: String): String {
        // You can customize decimal places or formatting here
        return BigDecimal(value).setScale(2, BigDecimal.ROUND_HALF_UP).toPlainString()
    }

    fun formatAmountWithSymbol(amountStr: String, symbol: String? = null, decimals: Int = 4): String {
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

    private fun parseErrorMessage(errorBody: String?): String? {
        if (errorBody == null) return null
        return try {
            val json = JSONObject(errorBody)
            json.optString("message", null)
        } catch (ex: Exception) {
            null
        }
    }

    fun formatCurrencyWithNGN(number: Int?): String {
        return if (number != null) {
            "NGN " + NumberFormat.getNumberInstance(Locale.US).format(number)
        } else {
            "NGN N/A"
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
}

