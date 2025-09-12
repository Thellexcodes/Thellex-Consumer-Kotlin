package com.thellex.payments.app

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.thellex.payments.features.dashboard.ui.MainActivity

fun showForceUpdateDialog(
    context: Context,
    latestVersion: String,
    downloadUrl: String?,
    updateType: String
) {
    AlertDialog.Builder(context)
        .setTitle("$updateType Update Required")
        .setMessage("A new version ($latestVersion) is available. You must update to continue using the app.")
        .setCancelable(false)
        .setPositiveButton("Update Now") { _, _ ->
            downloadUrl?.let { url ->
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                context.startActivity(intent)
            } ?: Toast.makeText(context, "Update URL not available", Toast.LENGTH_SHORT).show()
        }
        .setNegativeButton("Exit") { _, _ -> (context as? MainActivity)?.finish() }
        .show()
}

fun showOptionalUpdateDialog(
    context: Context,
    latestVersion: String,
    downloadUrl: String?,
    updateType: String
) {
    AlertDialog.Builder(context)
        .setTitle("$updateType Update Available")
        .setMessage("A new version ($latestVersion) is available. Would you like to update?")
        .setCancelable(true)
        .setPositiveButton("Update Now") { _, _ ->
            downloadUrl?.let { url ->
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                context.startActivity(intent)
            } ?: Toast.makeText(context, "Update URL not available", Toast.LENGTH_SHORT).show()
        }
        .setNegativeButton("Later") { dialog, _ -> dialog.dismiss() }
        .show()
}