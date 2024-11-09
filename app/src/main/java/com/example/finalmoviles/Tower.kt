package com.example.finalmoviles

data class Tower(
    val x: Float,  // Posición X de la torre.
    val y: Float,  // Posición Y de la torre.
    val damage: Int,  // Daño que inflige.
    val range: Float,  // Rango de alcance.
    val type: GameFragment.TowerType  // Tipo de torre.
)