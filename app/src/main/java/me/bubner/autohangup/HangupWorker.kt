package me.bubner.autohangup

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.SystemClock
import android.telecom.TelecomManager
import android.util.Log
import android.widget.Toast
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.CoroutineWorker
import androidx.work.ForegroundInfo
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import kotlinx.coroutines.delay
import me.bubner.autohangup.util.toHrMinSec
import kotlin.time.Duration.Companion.seconds

/**
 * Worker that handles timer and phone call invocation.
 *
 * @author Lucas Bubner, 2026
 */
class HangupWorker(context: Context, parameters: WorkerParameters) :
    CoroutineWorker(context, parameters) {

    override suspend fun doWork(): Result {
        val channel = NotificationChannel(
            CHANNEL_ID,
            "Auto Hangup",
            NotificationManager.IMPORTANCE_LOW
        )
        NotificationManagerCompat.from(applicationContext)
            .createNotificationChannel(channel)
        try {
            setForeground(getForegroundInfo())
        } catch (e: IllegalStateException) {
            Log.e("AutoHangup", "Could not start app on foreground.", e)
        }
        val targetMs = inputData.getLong("targetMs", -1L)
        if (targetMs == -1L)
            return Result.failure()
        val notificationManager = NotificationManagerCompat.from(applicationContext)
        try {
            while (!isStopped) {
                val dt = targetMs - SystemClock.elapsedRealtime()
                if (dt <= 0) break
                setProgress(workDataOf(WORK_PROGRESS_ELAPSED_MILLIS to dt))
                try {
                    notificationManager.notify(NOTIFICATION_ID, buildNotification(dt))
                } catch (e: SecurityException) {
                    // Can't show the notification, doesn't matter too much so we ignore it
                    Log.w("AutoHangup", "Couldn't send notification", e)
                }
                delay(1.seconds)
            }
        } finally {
            // Ensure to not leave any stale residual messages (occurs when the worker ends early)
            notificationManager.cancel(NOTIFICATION_ID)
        }
        // Timer has elapsed, attempt to terminate a connection
        val telecomManager =
            applicationContext.getSystemService(Context.TELECOM_SERVICE) as TelecomManager
        var res: Boolean
        try {
            res = telecomManager.isInCall
            @Suppress("DEPRECATION")
            if (res)
                res = telecomManager.endCall()
        } catch (e: SecurityException) {
            Log.e("AutoHangup", "Failed to end call.", e)
            res = false
        }
        if (res)
            applicationContext.mainExecutor.execute {
                Toast.makeText(
                    applicationContext,
                    "Auto Hangup: Call terminated.",
                    Toast.LENGTH_SHORT
                )
                    .show()
            }
        else
            applicationContext.mainExecutor.execute {
                Toast.makeText(
                    applicationContext,
                    "Auto Hangup: No call running.",
                    Toast.LENGTH_SHORT
                )
                    .show()
            }

        return Result.success()
    }

    override suspend fun getForegroundInfo(): ForegroundInfo {
        return ForegroundInfo(
            NOTIFICATION_ID,
            buildNotification(),
            ServiceInfo.FOREGROUND_SERVICE_TYPE_PHONE_CALL
        )
    }

    private fun buildNotification(dtMs: Long = 0L) =
        NotificationCompat.Builder(applicationContext, CHANNEL_ID)
            .setContentTitle("Auto Hangup")
            .setContentText(toHrMinSec(dtMs).let {
                "%02d:%02d:%02d remaining".format(
                    it[0],
                    it[1],
                    it[2]
                )
            })
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentIntent(
                PendingIntent.getActivity(
                    applicationContext,
                    0,
                    Intent(applicationContext, MainActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                    },
                    PendingIntent.FLAG_IMMUTABLE
                )
            )
            .addAction(
                // Icon doesn't appear to matter
                R.drawable.ic_launcher_foreground,
                "Stop",
                WorkManager.getInstance(applicationContext).createCancelPendingIntent(id)
            )
            .build()

    companion object {
        // Arbitrary ID, don't know if this needs more dedicatedness
        const val NOTIFICATION_ID = 582836
        const val CHANNEL_ID = "AutoHangup"
        const val WORK_PROGRESS_ELAPSED_MILLIS = "dtMs"
    }
}