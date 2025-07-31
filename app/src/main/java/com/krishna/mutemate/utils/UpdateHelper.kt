package com.krishna.mutemate.utils

import android.app.Activity
import android.widget.Toast
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.InstallStatus
import com.google.android.play.core.install.model.UpdateAvailability

class UpdateHelper(private val activity: Activity) {

    private val appUpdateManager: AppUpdateManager = AppUpdateManagerFactory.create(activity)
    private val UPDATE_REQUEST_CODE = 1234

    /**
     * Checks for updates and starts the update flow.
     * @param immediate true = Immediate update, false = Flexible update
     */
    fun checkForAppUpdate(immediate: Boolean = false) {
        appUpdateManager.appUpdateInfo.addOnSuccessListener { appUpdateInfo ->

            if (appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE) {
                val updateType = if (immediate) AppUpdateType.IMMEDIATE else AppUpdateType.FLEXIBLE

                if (appUpdateInfo.isUpdateTypeAllowed(updateType)) {
                    appUpdateManager.startUpdateFlowForResult(
                        appUpdateInfo,
                        updateType,
                        activity,
                        UPDATE_REQUEST_CODE
                    )
                }
            }
        }
    }

    /**
     * Resumes update if it was already started (for Flexible updates).
     */
    fun onResumeCheck() {
        appUpdateManager.appUpdateInfo.addOnSuccessListener { appUpdateInfo ->
            // If update is downloaded, ask user to restart
            if (appUpdateInfo.installStatus() == InstallStatus.DOWNLOADED) {
                Toast.makeText(activity, "Update downloaded. Restarting...", Toast.LENGTH_SHORT).show()
                appUpdateManager.completeUpdate()
            }
        }
    }
}