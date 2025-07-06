package me.rojan.pomodoro

actual fun playTimerEndSound() {
    // Assumes alarm.mp3 is in your jsMain/resources folder
    // and accessible at the root of your deployed site.
    js("new Audio('beep.mp3').play()")
}