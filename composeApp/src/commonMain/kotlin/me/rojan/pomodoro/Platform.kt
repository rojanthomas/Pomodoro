package me.rojan.pomodoro

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform