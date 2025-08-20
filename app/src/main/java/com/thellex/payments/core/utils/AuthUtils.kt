package com.thellex.payments.core.utils

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import java.nio.charset.StandardCharsets
import java.security.MessageDigest
import java.security.cert.CertificateFactory
import java.security.cert.X509Certificate
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

//object AuthUtils {
//    // Get SHA-256 fingerprint of the app's signing certificate
//    fun getCertificateFingerprint(context: Context): String? {
//        return try {
//            val packageInfo = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
//                context.packageManager.getPackageInfo(
//                    context.packageName,
//                    PackageManager.GET_SIGNING_CERTIFICATES
//                )
//            } else {
//                @Suppress("DEPRECATION")
//                context.packageManager.getPackageInfo(
//                    context.packageName,
//                    PackageManager.GET_SIGNATURES
//                )
//            }
//
//            val signatures = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
//                packageInfo.signingInfo?.apkContentsSigners
//            } else {
//                @Suppress("DEPRECATION")
//                packageInfo.signatures
//            }
//
//            val cert = signatures?.get(0)?.toByteArray()?.inputStream()
//            val certFactory = CertificateFactory.getInstance("X509")
//            val x509Cert = certFactory.generateCertificate(cert) as X509Certificate
//            val digest = MessageDigest.getInstance("SHA256")
//            val publicKey = x509Cert.encoded
//            digest.digest(publicKey).joinToString("") { "%02x".format(it) }
//
//        } catch (e: Exception) {
//            Log.e("AuthUtils", "Error getting fingerprint: $e")
//            null
//        }
//    }
//
//    // Generate HMAC signature using the fingerprint as the key
//    fun generateRequestSignature(context: Context, payload: String, timestamp: String): String? {
//        val fingerprint = getCertificateFingerprint(context) ?: return null
//        val data = "$timestamp:$payload"
//        return try {
//            val mac = Mac.getInstance("HmacSHA256")
//            val secretKeySpec = SecretKeySpec(fingerprint.toByteArray(StandardCharsets.UTF_8), "HmacSHA256")
//            mac.init(secretKeySpec)
//            val hash = mac.doFinal(data.toByteArray(StandardCharsets.UTF_8))
//            hash.joinToString("") { "%02x".format(it) }
//        } catch (e: Exception) {
//            Log.e("AuthUtils", "Error generating signature: $e")
//            null
//        }
//    }
//
//    // Enhanced root detection
//    fun isDeviceRooted(): Boolean {
//        val paths = arrayOf(
//            "/system/bin/su",
//            "/system/xbin/su",
//            "/sbin/su",
//            "/data/local/xbin/su",
//            "/system/app/Superuser.apk",
//            "/system/app/SuperSU.apk"
//        )
//        return paths.any { java.io.File(it).exists() } ||
//                System.getenv("PATH")?.contains("su") == true ||
//                try {
//                    Runtime.getRuntime().exec("which su").inputStream.read() != -1
//                } catch (e: Exception) {
//                    false
//                }
//    }
//}

object AuthUtils {
    fun getCertificateFingerprint(context: Context): String? {
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
                packageInfo.signingInfo?.apkContentsSigners
            } else {
                @Suppress("DEPRECATION")
                packageInfo.signatures
            }

            if (signatures.isNullOrEmpty()) {
                Log.e("AuthUtils", "No signatures found")
                return null
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P && packageInfo.signingInfo?.hasMultipleSigners() == true) {
                Log.d("AuthUtils", "Multiple signers detected")
                packageInfo.signingInfo?.signingCertificateHistory?.forEachIndexed { index, signature ->
                    val cert = signature.toByteArray().inputStream()
                    val certFactory = CertificateFactory.getInstance("X509")
                    val x509Cert = certFactory.generateCertificate(cert) as X509Certificate
                    val digest = MessageDigest.getInstance("SHA256")
                    val publicKey = x509Cert.encoded
                    val fingerprint = digest.digest(publicKey).joinToString("") { "%02x".format(it) }
                    Log.d("AuthUtils", "History [$index] fingerprint: $fingerprint")
                }
            }

            // Log all signatures
            signatures.forEachIndexed { index, signature ->
                val cert = signature.toByteArray().inputStream()
                val certFactory = CertificateFactory.getInstance("X509")
                val x509Cert = certFactory.generateCertificate(cert) as X509Certificate
                val digest = MessageDigest.getInstance("SHA256")
                val publicKey = x509Cert.encoded
                val fingerprint = digest.digest(publicKey).joinToString("") { "%02x".format(it) }
                Log.d("AuthUtils", "Signature [$index] fingerprint: $fingerprint")
            }

            // Select the first signature (adjust if needed to select the correct one)
            val cert = signatures[0].toByteArray().inputStream()
            val certFactory = CertificateFactory.getInstance("X509")
            val x509Cert = certFactory.generateCertificate(cert) as X509Certificate
            val digest = MessageDigest.getInstance("SHA256")
            val publicKey = x509Cert.encoded
            val fingerprint = digest.digest(publicKey).joinToString("") { "%02x".format(it) }
            Log.d("AuthUtils", "Selected fingerprint: $fingerprint")
            fingerprint
        } catch (e: Exception) {
            Log.e("AuthUtils", "Error getting fingerprint: $e")
            null
        }
    }

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
            Log.e("AuthUtils", "Error generating signature: $e")
            null
        }
    }

    fun isDeviceRooted(): Boolean {
        val paths = arrayOf(
            "/system/bin/su",
            "/system/xbin/su",
            "/sbin/su",
            "/data/local/xbin/su",
            "/system/app/Superuser.apk",
            "/system/app/SuperSU.apk"
        )
        return paths.any { java.io.File(it).exists() } ||
                System.getenv("PATH")?.contains("su") == true ||
                try {
                    Runtime.getRuntime().exec("which su").inputStream.read() != -1
                } catch (e: Exception) {
                    false
                }
    }
}
