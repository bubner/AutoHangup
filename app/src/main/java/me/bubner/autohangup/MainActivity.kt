package me.bubner.autohangup

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import me.bubner.autohangup.ui.Main

/**
 * Auto Hangup
 *
 * @author Lucas Bubner, 2026
 */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent { Main() }
    }
}
