package me.bubner.autohangup

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.core.app.ActivityCompat
import androidx.core.content.edit
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.work.workDataOf
import kotlinx.coroutines.flow.flowOf
import me.bubner.autohangup.ui.Main
import me.bubner.autohangup.util.isPermissionGranted
import java.util.UUID

/**
 * Auto Hangup
 *
 * @author Lucas Bubner, 2026
 */
class MainActivity : ComponentActivity() {
    // Worker under this UUID controls all timer and phone callback operations
    private var workId by mutableStateOf<UUID?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val sharedPrefs = getSharedPreferences(PREFERENCES_NAME, MODE_PRIVATE)
        sharedPrefs.getString(PREV_WORK_ID, null)?.let {
            // Found a possibly already running job
            val wid = UUID.fromString(it)
            if (WorkManager.getInstance(this).getWorkInfoById(wid)
                    .get()?.state == WorkInfo.State.RUNNING
            )
                workId = wid
        }

        if (!Manifest.permission.ANSWER_PHONE_CALLS.isPermissionGranted(this)
            || !Manifest.permission.READ_PHONE_STATE.isPermissionGranted(this)
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(
                    Manifest.permission.ANSWER_PHONE_CALLS,
                    Manifest.permission.READ_PHONE_STATE
                ),
                0
            )
        }
        // Optional permissions
        if (!Manifest.permission.POST_NOTIFICATIONS.isPermissionGranted(this)) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                1
            )
        }

        setContent {
            val workInfo by remember(workId) {
                if (workId != null) {
                    WorkManager.getInstance(this).getWorkInfoByIdFlow(workId!!)
                } else {
                    flowOf(null)
                }
            }.collectAsState(initial = null)
            val dt =
                workInfo?.progress?.getLong(HangupWorker.WORK_PROGRESS_ELAPSED_MILLIS, 0L) ?: 0L

            Main(
                active = workInfo != null && workInfo!!.state == WorkInfo.State.RUNNING,
                dtMs = dt,
                setTargetMs = {
                    // Presence of a workInfo implies workId is valid, but it could be in a finished
                    // state. This properly handles both cases and cleans up workId to prevent
                    // unnecessary lookup later on.
                    if (workInfo == null || workInfo!!.state != WorkInfo.State.RUNNING) {
                        val work = OneTimeWorkRequestBuilder<HangupWorker>()
                            .setInputData(workDataOf(WORK_UNIX_TARGET_MS to it))
                            .build()
                        // Timer functions should restart on existing conflict
                        WorkManager.getInstance(this).enqueueUniqueWork(
                            HangupWorker.CHANNEL_ID,
                            ExistingWorkPolicy.REPLACE,
                            work
                        )
                        workId = work.id
                        // For Activity re-creation
                        workId?.let { id ->
                            sharedPrefs.edit(commit = true) {
                                putString(PREV_WORK_ID, id.toString())
                            }
                        }
                    } else {
                        // We only let the Worker terminate itself OR by the stop buttons, not by
                        // Activity destruction since we want this to persist as a service
                        workId?.let { id ->
                            WorkManager.getInstance(this).cancelWorkById(id)
                        }
                        workId = null
                    }
                },
            )
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String?>,
        grantResults: IntArray,
        deviceId: Int
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults, deviceId)
        if (requestCode != 0)
            return
        // Application is useless without phone permissions, just bail if we don't have them
        if (grantResults.any { it == PackageManager.PERMISSION_DENIED }) {
            Toast.makeText(
                this,
                "Cannot use Auto Hangup with insufficient permissions!",
                Toast.LENGTH_LONG
            )
                .show()
            finishAffinity()
        }
    }

    companion object {
        const val PREFERENCES_NAME = "settings"
        const val PREV_WORK_ID = "workId"
        const val WORK_UNIX_TARGET_MS = "targetMs"
    }
}
