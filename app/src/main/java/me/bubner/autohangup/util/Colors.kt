package me.bubner.autohangup.util

import androidx.compose.ui.graphics.Color

/**
 * Scales a colour by all RGB values.
 */
fun Color.scaleBrightness(scale: Float) =
    copy(red = red * scale, blue = blue * scale, green = green * scale)