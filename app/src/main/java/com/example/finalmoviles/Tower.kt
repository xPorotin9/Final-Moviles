package com.example.finalmoviles

data class Tower(
    val x: Float,
    val y: Float,
    val damage: Int,
    val range: Float,
    val type: GameFragment.TowerType
)