package com.thellex.payments.core.utils

import android.content.Context
import android.os.Build
import android.util.Log
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.thellex.payments.data.model.CrashReportDto
import com.thellex.payments.network.services.ApiClient
import java.io.File

object CrashLogger {

    private const val CRASH_DIR = "crash_logs"

    fun init(context: Context) {
        Thread.setDefaultUncaughtExceptionHandler { _, throwable ->
            saveCrashToLocalFile(context, throwable)
            FirebaseCrashlytics.getInstance().recordException(throwable)
        }
    }

    fun logHandledException(throwable: Throwable) {
        FirebaseCrashlytics.getInstance().recordException(throwable)
    }

    private fun saveCrashToLocalFile(context: Context, throwable: Throwable) {
        val crashText = Log.getStackTraceString(throwable)
        val timestamp = System.currentTimeMillis()
        val fileName = "crash_$timestamp.txt"

        val dir = File(context.filesDir, CRASH_DIR)
        if (!dir.exists()) dir.mkdirs()

        val file = File(dir, fileName)
        file.writeText(crashText)
    }

    suspend fun sendStoredCrashes(context: Context) {
        val crashDir = File(context.filesDir, CRASH_DIR)
        if (!crashDir.exists()) return

        crashDir.listFiles()?.forEach { file ->
            val rawLog = file.readText()
            val formattedLog = extractCrashSummary(rawLog)

            val dto = CrashReportDto(
                timestamp = file.name.removePrefix("crash_").removeSuffix(".txt").toLongOrNull() ?: System.currentTimeMillis(),
                device = Build.MODEL,
                os = "Android ${Build.VERSION.RELEASE}",
                log = formattedLog
            )

            try {
                val response = ApiClient.getPublicCrashReportApi(context).sendCrashReport(dto)
                if (response.isSuccessful) file.delete()
            } catch (e: Exception) {
                // Keep log for next retry
            }
        }
    }

    private fun extractCrashSummary(rawLog: String): String {
        val lines = rawLog.lines()

        val exceptionLine = lines.firstOrNull { it.contains("Exception") || it.contains("Error") } ?: "UnknownException"
        val messageLine = lines.firstOrNull { it.contains(": ") && !it.contains("Caused by") } ?: ""
        val crashLine = lines.firstOrNull { it.trim().startsWith("at com.thellex") } ?: ""

        val exception = exceptionLine.trim().substringBefore(":").trim()
        val message = exceptionLine.substringAfter(":").trim().takeIf { it.isNotEmpty() } ?: messageLine.substringAfter(":").trim()
        val location = crashLine.trim()

        return buildString {
            appendLine("Exception: $exception")
            if (message.isNotBlank()) appendLine("Message: $message")
            if (location.isNotBlank()) appendLine("Location: $location")
        }.trim()
    }
}
