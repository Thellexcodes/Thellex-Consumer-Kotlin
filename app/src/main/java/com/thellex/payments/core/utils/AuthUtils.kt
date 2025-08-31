package com.thellex.payments.core.utils

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import java.io.File
import java.nio.charset.StandardCharsets
import java.security.MessageDigest
import java.security.cert.CertificateFactory
import java.security.cert.X509Certificate
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

object AuthUtils {
    private const val TAG = "AuthUtils"
    private const val EXPECTED_FINGERPRINT = ""
    private var cachedFingerprint: String? = null

    /**
     * Retrieves the SHA-256 fingerprint of the app's signing certificate.
     * Caches the result to avoid recomputing.
     */
    fun getCertificateFingerprint(context: Context): String? {
        cachedFingerprint?.let { return it }

        return try {
            val packageInfo = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                context.packageManager.getPackageInfo(
                    context.packageName,
                    PackageManager.GET_SIGNING_CERTIFICATES
                )
            } else {
                @Suppress("DEPRECATION")
                context.packageManager.getPackageInfo(
                    context.packageName,
                    PackageManager.GET_SIGNATURES
                )
            }

            val signatures = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                packageInfo.signingInfo?.let { signingInfo ->
                    if (signingInfo.hasMultipleSigners()) {
                        signingInfo.signingCertificateHistory
                    } else {
                        signingInfo.apkContentsSigners
                    }
                } ?: emptyArray()
            } else {
                @Suppress("DEPRECATION")
                packageInfo.signatures
            }

            if (signatures != null) {
                if (signatures.isEmpty()) {
                    Log.e(TAG, "No signatures found")
                    return null
                }
            }

            // Log all signatures for debugging
            signatures?.forEachIndexed { index, signature ->
                val fingerprint = computeFingerprint(signature.toByteArray())
                Log.d(TAG, "Signature [$index] fingerprint: $fingerprint")
            }

            // Select the expected fingerprint
            val selectedSignature = signatures?.firstOrNull { signature ->
                computeFingerprint(signature.toByteArray()) == EXPECTED_FINGERPRINT
            } ?: signatures?.firstOrNull() // Fallback to first if expected not found

            if (selectedSignature == null) {
                Log.e(TAG, "No valid signature found")
                return null
            }

            val fingerprint = computeFingerprint(selectedSignature.toByteArray())
            if (fingerprint != EXPECTED_FINGERPRINT) {
                Log.w(TAG, "Selected fingerprint ($fingerprint) does not match expected ($EXPECTED_FINGERPRINT)")
            }
            Log.d(TAG, "Selected fingerprint: $fingerprint")
            cachedFingerprint = fingerprint
            fingerprint
        } catch (e: Exception) {
            Log.e(TAG, "Error getting fingerprint", e)
            null
        }
    }

    /**
     * Computes SHA-256 fingerprint from a signature byte array.
     */
    private fun computeFingerprint(signatureBytes: ByteArray): String? {
        return try {
            val certFactory = CertificateFactory.getInstance("X509")
            val cert = certFactory.generateCertificate(signatureBytes.inputStream()) as X509Certificate
            val digest = MessageDigest.getInstance("SHA256")
            digest.digest(cert.encoded).joinToString("") { "%02x".format(it) }
        } catch (e: Exception) {
            Log.e(TAG, "Error computing fingerprint", e)
            null
        }
    }

    /**
     * Generates an HMAC-SHA256 signature using the certificate fingerprint.
     */
    fun generateRequestSignature(context: Context, payload: String, timestamp: String): String? {
        val fingerprint = getCertificateFingerprint(context) ?: return null
        val data = "$timestamp:$payload"
        return try {
            val mac = Mac.getInstance("HmacSHA256")
            val secretKeySpec = SecretKeySpec(fingerprint.toByteArray(StandardCharsets.UTF_8), "HmacSHA256")
            mac.init(secretKeySpec)
            val hash = mac.doFinal(data.toByteArray(StandardCharsets.UTF_8))
            hash.joinToString("") { "%02x".format(it) }
        } catch (e: Exception) {
            Log.e(TAG, "Error generating signature", e)
            null
        }
    }

    /**
     * Checks if the device is rooted or running a potentially insecure environment.
     */
    fun isDeviceRooted(): Boolean {
        // Common root paths
        val rootPaths = arrayOf(
            "/system/bin/su",
            "/system/xbin/su",
            "/sbin/su",
            "/data/local/xbin/su",
            "/system/app/Superuser.apk",
            "/system/app/SuperSU.apk",
            "/data/data/com.topjohnwu.magisk" // Magisk
        )

        // Check for root binaries or packages
        val hasRootBinary = rootPaths.any { File(it).exists() }

        // Check for su in PATH
        val hasSuInPath = System.getenv("PATH")?.split(":")?.any { path ->
            File("$path/su").exists()
        } == true

        // Check for test keys (common in custom ROMs)
        val isTestKeyBuild = Build.TAGS.contains("test-keys")

        // Check Magisk-specific properties
        val isMagiskPresent = try {
            Runtime.getRuntime().exec("getprop ro.magisk.version").inputStream.bufferedReader().use {
                it.readLine()?.isNotEmpty() == true
            }
        } catch (e: Exception) {
            false
        }

        return hasRootBinary || hasSuInPath || isTestKeyBuild || isMagiskPresent
    }
}