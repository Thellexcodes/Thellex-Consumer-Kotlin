package com.thellex.pay.core.utils

import com.thellex.pay.data.model.AppVersionState
import com.thellex.pay.data.repository.AppVersionRepository
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch


object AppUpdateHelper {

    fun handleAppUpdate(
        activity: AppCompatActivity,
        repository: AppVersionRepository
    ) {
        activity.lifecycleScope.launch {
            val state = repository.checkAppVersion()

            when (state) {
                is AppVersionState.ForceUpdate,
                is AppVersionState.OptionalUpdate -> {
                    showDialog(activity, state)
                }
                else -> Unit
            }
        }
    }

    private fun showDialog(activity: AppCompatActivity, state: AppVersionState) {
        // Only show for force updates
        if (state !is AppVersionState.ForceUpdate) return

        val dialog = ComposeDialogFragment {
            AppUpdateScreen(
                latestVersion = state.latestVersion,
                downloadUrl = state.downloadUrl,
                updateType = state.updateType,
                releaseNotes = state.releaseNotes
            )
        }

        dialog.isCancelable = false
        dialog.show(activity.supportFragmentManager, "AppUpdateDialog")
    }

}