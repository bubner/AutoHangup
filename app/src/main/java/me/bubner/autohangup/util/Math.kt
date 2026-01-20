package me.bubner.autohangup.util

/**
 * Converts hours, minutes and seconds into a relative amount in milliseconds.
 */
fun toMs(hour: Int, minute: Int, second: Int) =
    hour.toLong() * 3600000L + minute * 60000L + second * 1000L

/**
 * Converts a relative amount in milliseconds to an array of hours, minutes and seconds.
 */
fun toHrMinSec(ms: Long) = ms
    .coerceAtLeast(0L)
    .let {
        val secs = it / 1000
        intArrayOf(
            (secs / 3600).toInt(),
            ((secs % 3600) / 60).toInt(),
            (secs % 60).toInt()
        )
    }