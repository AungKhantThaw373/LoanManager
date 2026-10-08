package com.example.kotlinmulti

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform