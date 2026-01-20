package me.bubner.autohangup.ui

import android.os.SystemClock
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.anhaki.picktime.PickHourMinuteSecond
import com.anhaki.picktime.utils.PickTimeFocusIndicator
import com.anhaki.picktime.utils.PickTimeTextStyle
import me.bubner.autohangup.ui.theme.AutoHangupTheme
import me.bubner.autohangup.util.leftPad
import me.bubner.autohangup.util.scaleBrightness
import me.bubner.autohangup.util.toHrMinSec
import me.bubner.autohangup.util.toMs

/**
 * Primary UI.
 *
 * @author Lucas Bubner, 2026
 */
@Preview
@Composable
fun Main(
    active: Boolean = false,
    dtMs: Long = 0L,
    setTargetMs: (Long) -> Unit = {}
) {
    var hour by rememberSaveable { mutableIntStateOf(0) }
    var minute by rememberSaveable { mutableIntStateOf(0) }
    var second by rememberSaveable { mutableIntStateOf(0) }
    val buttonColour by animateColorAsState(
        targetValue = if (active)
            MaterialTheme.colorScheme.error
        else
            MaterialTheme.colorScheme.primary,
        animationSpec = tween(durationMillis = 100),
    )

    LaunchedEffect(dtMs) {
        // Copy state so the stop button will serve as an effective pause button
        toHrMinSec(dtMs).let {
            hour = it[0]
            minute = it[1]
            second = it[2]
        }
    }

    AutoHangupTheme {
        Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
            Column(
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                Text("Auto Hangup", fontSize = 32.sp, fontWeight = FontWeight.Bold)

                if (!active)
                    PickHourMinuteSecond(
                        initialHour = hour,
                        onHourChange = { hour = it },
                        initialMinute = minute,
                        onMinuteChange = { minute = it },
                        initialSecond = second,
                        onSecondChange = { second = it },
                        selectedTextStyle = PickTimeTextStyle(
                            color = MaterialTheme.colorScheme.primary,
                            fontSize = 18.sp,
                            fontFamily = FontFamily.Default,
                            fontWeight = FontWeight.Bold,
                        ),
                        unselectedTextStyle = PickTimeTextStyle(
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                            fontSize = 12.sp,
                            fontFamily = FontFamily.Default,
                            fontWeight = FontWeight.Normal,
                        ),
                        verticalSpace = 20.dp,
                        horizontalSpace = 32.dp,
                        containerColor = MaterialTheme.colorScheme.background,
                        isLooping = true,
                        extraRow = 1,
                        focusIndicator = PickTimeFocusIndicator(
                            enabled = true,
                            widthFull = false,
                            background = MaterialTheme.colorScheme.background,
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(
                                2.dp,
                                MaterialTheme.colorScheme.background.scaleBrightness(0.9f)
                            ),
                        )
                    )
                else
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            toHrMinSec(dtMs)
                                .let { times ->
                                    if (dtMs <= 0) "--:--:--" else times
                                        .map { it.leftPad() }
                                        .let { "${it[0]}:${it[1]}:${it[2]}" }
                                },
                            fontSize = 32.sp,
                        )
                        Text("until hangup")
                    }

                Button(
                    onClick = {
                        setTargetMs(SystemClock.elapsedRealtime() + toMs(hour, minute, second))
                    },
                    modifier = Modifier
                        .size(width = 200.dp, height = 60.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = buttonColour,
                        contentColor = if (active) MaterialTheme.colorScheme.onError else MaterialTheme.colorScheme.onPrimary
                    )
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (active) {
                            Text("Stop")
                            Icon(
                                imageVector = Icons.Default.Stop,
                                contentDescription = "Stop"
                            )
                        } else {
                            Text("Start")
                            Icon(
                                imageVector = Icons.Default.PlayArrow,
                                contentDescription = "Start"
                            )
                        }
                    }
                }
            }

            Column(
                verticalArrangement = Arrangement.Bottom,
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                Text(
                    "Auto Hangup will attempt to disconnect phone operations after the timer has elapsed.",
                    fontSize = 12.sp,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}