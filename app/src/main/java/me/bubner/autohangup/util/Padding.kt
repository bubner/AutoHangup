package me.bubner.autohangup.util

/**
 * Left-pads a single digit number to two digits if required.
 */
fun Int.leftPad() = if (this.toString().length == 1) "0$this" else this.toString()
