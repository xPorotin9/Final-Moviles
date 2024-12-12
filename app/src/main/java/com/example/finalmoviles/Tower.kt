package com.example.finalmoviles

/**
 * Data class representing a Tower in the game.
 *
 * @property x The X position of the tower.
 * @property y The Y position of the tower.
 * @property damage The damage the tower inflicts.
 * @property range The range of the tower.
 * @property type The type of the tower.
 */
data class Tower(
    val x: Float,  // Posición X de la torre.
    val y: Float,  // Posición Y de la torre.
    val damage: Int,  // Daño que inflige.
    val range: Float,  // Rango de alcance.
    val type: GameFragment.TowerType  // Tipo de torre.
)