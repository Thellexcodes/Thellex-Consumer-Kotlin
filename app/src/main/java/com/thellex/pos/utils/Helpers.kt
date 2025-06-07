package com.thellex.pos.utils

import android.content.Context
import android.os.Looper
import android.widget.Toast
import com.google.android.material.textfield.TextInputLayout
import com.thellex.pos.R
import com.thellex.pos.data.enums.ERRORS
import com.thellex.pos.data.model.ErrorResponse
import com.thellex.pos.data.model.TransactionStatus
import java.security.SecureRandom
import java.security.cert.X509Certificate
import javax.net.ssl.*
import kotlinx.serialization.json.Json
import java.util.Locale

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

    fun Context.showLongToast(message: String, durationInMillis: Long = 10000L) {
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

    fun getStatusIconResId(status: String?): Int {
        return when (normalizeStatusForIcon(status)) {
            "received" -> R.drawable.icon_receive_status
            "sent" -> R.drawable.icon_send_status
            else -> R.drawable.icon_arrow_down
        }
    }

    fun formatTimestamp(timestamp: String): String {
        return "Time"
//        return try {
//            val parser = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault())
//            parser.timeZone = TimeZone.getTimeZone("UTC")
//            val date = parser.parse(timestamp)
//            val formatter = SimpleDateFormat("MMM dd, hh:mm a", Locale.getDefault())
//            formatter.format(date)
//        } catch (e: Exception) {
//            timestamp // fallback to raw string
//        }
    }

    fun mapToTransactionStatus(rawStatus: String): TransactionStatus {
        return when (rawStatus.lowercase(Locale.getDefault())) {
            "accepted" -> TransactionStatus.COMPLETED
            "completed" -> TransactionStatus.COMPLETED
            "rejected" -> TransactionStatus.REJECTED
            "pending" -> TransactionStatus.PENDING
            else -> TransactionStatus.PENDING // fallback
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

}

