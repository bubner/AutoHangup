package me.bubner.autohangup.util

import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat

fun String.isPermissionGranted(ctx: Context) =
    ContextCompat.checkSelfPermission(ctx, this) == PackageManager.PERMISSION_GRANTED