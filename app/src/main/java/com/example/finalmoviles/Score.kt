package com.example.finalmoviles

data class Score(
    val playerName: String = "",
    val score: Int = 0,
    val wave: Int = 0,
    val timestamp: Long = System.currentTimeMillis()
)