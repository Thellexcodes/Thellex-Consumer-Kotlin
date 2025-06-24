package com.thellex.payments.core.utils

import android.content.Context
import android.os.Looper
import android.widget.Toast
import com.google.android.material.textfield.TextInputLayout
import com.thellex.payments.R
import com.thellex.payments.data.enums.ERRORS
import com.thellex.payments.data.model.BlockchainItem
import com.thellex.payments.data.model.ErrorResponse
import com.thellex.payments.data.model.PaymentStatus
import com.thellex.payments.settings.SupportedBlockchain
import java.security.SecureRandom
import java.security.cert.X509Certificate
import javax.net.ssl.*
import kotlinx.serialization.json.Json
import java.math.BigDecimal
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

object Helpers {
    public fun getNavigationBarHeight(context: Context): Int {
        val resources = context.resources
        val resourceId = resources.getIdentifier("navigation_bar_height", "dimen", "android")
        return if (resourceId > 0) resources.getDimensionPixelSize(resourceId) else 0
    }

    fun createUnsafeSslSocketFactory(): SSLSocketFactory {
        val trustAllCerts = arrayOf<TrustManager>(object : X509TrustManager {
            override fun checkClientTrusted(chain: Array<X509Certificate>, authType: String) {}
            override fun checkServerTrusted(chain: Array<X509Certificate>, authType: String) {}
            override fun getAcceptedIssuers(): Array<X509Certificate> = arrayOf()
        })

        val sslContext = SSLContext.getInstance("SSL")
        sslContext.init(null, trustAllCerts, SecureRandom())
        return sslContext.socketFactory
    }

    // Create an unsafe trust manager
    fun createUnsafeTrustManager(): X509TrustManager {
        return object : X509TrustManager {
            override fun checkClientTrusted(chain: Array<X509Certificate>, authType: String) {}
            override fun checkServerTrusted(chain: Array<X509Certificate>, authType: String) {}
            override fun getAcceptedIssuers(): Array<X509Certificate> = arrayOf()
        }
    }

    public fun parseErrorMessage(errorResponse: String?): Pair<ERRORS?, String> {
        return errorResponse?.let {
            try {
                val jsonObject = Json.decodeFromString<ErrorResponse>(it)

                val errorType = when (jsonObject.message) {
                    "EMAIL_EXISTS" -> ERRORS.EMAIL_EXISTS
                    "BRAND_EXISTS" -> ERRORS.BRAND_EXISTS
                    "REJECTED_AGREEMENT" -> ERRORS.REJECTED_AGREEMENT
                    "INVALID_CREDENTIAL" -> ERRORS.INVALID_CREDENTIAL
                    "EMAIL_NOT_VERIFIED" -> ERRORS.EMAIL_NOT_VERIFIED
                    "PHONE_NOT_VERIFIED" -> ERRORS.PHONE_NOT_VERIFIED
                    "UNKNOWN_ERROR" -> ERRORS.UNKNOWN_ERROR
                    else -> null
                }

                Pair(errorType, jsonObject.message)
            } catch (e: Exception) {
                e.printStackTrace()
                Pair(null, "Error parsing error response: ${e.message}")
            }
        } ?: Pair(null, "No error message available")
    }

    fun displayError(errorType: ERRORS, targetLayout: TextInputLayout) {
        targetLayout.helperText = errorType.message
    }

    fun Context.showLongToast(message: String, durationInMillis: Long = 390000L) {
        val interval = 3500L // Toast.LENGTH_LONG duration
        val toast = Toast.makeText(this, message, Toast.LENGTH_LONG)

        val handler = android.os.Handler(Looper.getMainLooper())
        val endTime = System.currentTimeMillis() + durationInMillis

        val showToastRunnable = object : Runnable {
            override fun run() {
                if (System.currentTimeMillis() < endTime) {
                    toast.show()
                    handler.postDelayed(this, interval)
                }
            }
        }

        handler.post(showToastRunnable)
    }

    fun getIconResIdForToken(token: String): Int {
        return when (token.lowercase(Locale.getDefault())) {
            "usdt" -> R.drawable.icon_usdt
            "trx" -> R.drawable.icon_usdt
            "usdc" -> R.drawable.icon_usdc
            "xlm" -> R.drawable.icon_stellar
            else -> R.drawable.icon_txn
        }
    }

    fun getIconResIdForBlockchain(chain: String): Int {
        return when (chain.lowercase(Locale.getDefault())) {
            "bep20" -> R.drawable.icon_bnb_chain
            "matic" -> R.drawable.icon_polygon
            else -> R.drawable.icon_bnb_chain
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
            "sent" -> "sent"
            else -> "unknown"
        }
    }

    fun formatDecimal(value: String): String {
        // You can customize decimal places or formatting here
        return BigDecimal(value).setScale(2, BigDecimal.ROUND_HALF_UP).toPlainString()
    }

    fun formatAmountWithSymbol(amountStr: String, symbol: String? = null, decimals: Int = 4): String {
        val amount = amountStr.toDoubleOrNull() ?: 0.0
        val symbolToUse = symbol ?: "$"
        val formattedAmount = "%.${decimals}f".format(amount)
        return "$symbolToUse$formattedAmount"
    }


    fun parseDate(dateString: String?): Date? {
        return try {
            // Adjust the pattern to your actual created_at format
            val formatter = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault())
            formatter.parse(dateString ?: "")
        } catch (e: Exception) {
            null
        }
    }

    fun convertToUsd(currency: String, amountStr: String): String {
        return try {
            val amount = amountStr.toDouble()
            String.format("%.2f", amount)  // For demo, just return amount formatted to 2 decimals
        } catch (e: Exception) {
            "0.00"
        }
    }
}

