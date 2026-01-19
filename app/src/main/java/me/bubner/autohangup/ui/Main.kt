package me.bubner.autohangup.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import me.bubner.autohangup.ui.theme.AutoHangupTheme

@Composable
fun Main() {
    AutoHangupTheme {
        Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
            Column(
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxSize().padding(innerPadding)
            ) {
                Text("Hello world")
            }
        }
    }
}