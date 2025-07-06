package me.rojan.pomodoro

import androidx.compose.animation.core.InfiniteRepeatableSpec
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import org.jetbrains.compose.ui.tooling.preview.Preview
import kotlin.time.Duration.Companion.seconds

@Composable
@Preview
fun App() {
    MaterialTheme {
        PomodoroApp()
    }
}

private const val TIME_POMODORO_IN_SECONDS = 5
private const val TIME_BREAK_IN_SECONDS = 2

private sealed interface TimerState {

    val initialTime: Int
    val timeRemaining: Int

    data class Pomodoro(
        override val initialTime: Int = TIME_POMODORO_IN_SECONDS,
        override val timeRemaining: Int = initialTime,
    ) : TimerState

    data class Break(
        override val initialTime: Int = TIME_BREAK_IN_SECONDS,
        override val timeRemaining: Int = initialTime,
    ) : TimerState
}

@Composable
private fun PomodoroApp() {

    var started by remember { mutableStateOf(false) }
    var timerState by remember { mutableStateOf<TimerState>(TimerState.Pomodoro()) }

    LaunchedEffect(Unit) {
        while (true) {
            delay(1.seconds)

            if (started) {
                when (timerState) {
                    is TimerState.Break -> {
                        if (timerState.timeRemaining == 0) {
                            timerState = TimerState.Pomodoro(TIME_POMODORO_IN_SECONDS)
                        } else {
                            timerState =
                                (timerState as TimerState.Break).copy(timeRemaining = timerState.timeRemaining - 1)
                        }
                    }

                    is TimerState.Pomodoro -> {
                        if (timerState.timeRemaining == 0) {
                            timerState = TimerState.Break(TIME_BREAK_IN_SECONDS)
                        } else {
                            timerState =
                                (timerState as TimerState.Pomodoro).copy(timeRemaining = timerState.timeRemaining - 1)
                        }
                    }
                }
            }
        }
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(0.25f)
                .fillMaxHeight(0.5f)
        ) {
            Text(
                modifier = Modifier.padding(16.dp),
                text = "Pomodoro Timer",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
            )

            CircularProgressContainer(
                initialValue = timerState.initialTime.toFloat(),
                currentValue = timerState.timeRemaining.toFloat(),
                color = when (timerState) {
                    is TimerState.Break -> Color(0xFF4FC3F7) // Light Blue (Material Design Light Blue 300)
                    is TimerState.Pomodoro -> Color(0xFFFFA726) // Orange (Material Design Orange 300)
                }
            ) {

                Column(
                    modifier = Modifier.fillMaxSize()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        when (timerState) {
                            is TimerState.Break -> "Break"
                            is TimerState.Pomodoro -> "Focus Time"
                        },
                        style = MaterialTheme.typography.titleMedium,
                    )

                    Spacer(Modifier.height(8.dp))

                    val mins = timerState.timeRemaining / 60
                    val secs = timerState.timeRemaining % 60
                    val formattedMins = mins.toString().padStart(2, '0')
                    val formattedSecs = secs.toString().padStart(2, '0')

                    Text(
                        text = "$formattedMins:$formattedSecs",
                        style = MaterialTheme.typography.titleLarge,
                        fontSize = 36.sp,
                    )

                    Spacer(Modifier.height(8.dp))

                    OutlinedButton(
                        onClick = {
                            started = !started
                        }
                    ) {
                        Text(
                            if (started) "Stop" else "Start",
                            color = Color.Black
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CircularProgressContainer(
    initialValue: Float,
    currentValue: Float,
    color: Color,
    content: @Composable () -> Unit,
) {
    Box(
        contentAlignment = Alignment.Center
    ) {
        val targetProgress = if (initialValue > 0) {
            currentValue / initialValue
        } else {
            0f
        }

        val animatedProgress by animateFloatAsState(
            targetValue = targetProgress,
            animationSpec = tween(durationMillis = 250),
            label = "ProgressAnimation"
        )

        val infiniteTransition = rememberInfiniteTransition(label = "PulseTransition")
        val animatedAlpha by infiniteTransition.animateFloat(
            initialValue = 0.5f,
            targetValue = 1f,
            animationSpec = InfiniteRepeatableSpec(
                animation = tween(durationMillis = 1500, easing = LinearEasing),
                repeatMode = RepeatMode.Reverse // Pulse back and forth
            ),
            label = "PulseAlpha"
        )

        CircularProgressIndicator(
            modifier = Modifier
                .fillMaxSize()
                .aspectRatio(
                    1f,
                    matchHeightConstraintsFirst = true
                )
                .padding(16.dp), // Or true, depending on desired behavior if parent is not square
            progress = { animatedProgress },
            color = color.copy(alpha = animatedAlpha),
            strokeWidth = 12.dp // You might want to make this dynamic based on size
        )

        content()
    }
}