package com.example.finalmoviles

import android.graphics.PointF
import kotlin.math.pow
import kotlin.math.sqrt

/**
 * Represents an enemy in the game with position, health, speed, and other attributes.
 *
 * @property x The X position of the enemy.
 * @property y The Y position of the enemy.
 * @property health The health of the enemy.
 * @property currentWaypoint The current waypoint the enemy is moving towards.
 * @property speed The speed at which the enemy moves.
 * @property reward The reward given to the player when the enemy is defeated.
 * @property damage The damage the enemy deals to the player if it reaches the end.
 */
class Enemy(
    var x: Float,  // Posición X
    var y: Float,  // Posición Y
    var health: Int,
    private var currentWaypoint: Int = 0,  // Punto de destino actual en su ruta.
    private val speed: Float,
    val reward: Int,
    val damage: Int = 4  // Daño al jugador si llega al final.
) {
    companion object {
        /**
         * Creates an enemy with adjusted statistics for the specified wave.
         *
         * @param wave The wave number to scale the enemy's attributes.
         * @param startPoint The starting point of the enemy.
         * @return A new Enemy instance with scaled attributes.
         */
        fun createForWave(wave: Int, startPoint: PointF): Enemy {
            val healthMultiplier = 1.0f + (wave * 0.2f)  // Incrementa la caracteristicas según la oleada
            val speedMultiplier = 1.0f + (wave * 0.1f)
            val baseHealth = 100
            val baseSpeed = 2f
            val baseReward = 5

            // Crea y retorna un enemigo con atributos escalados.
            return Enemy(
                x = startPoint.x,
                y = startPoint.y,
                health = (baseHealth * healthMultiplier).toInt(),
                speed = baseSpeed * speedMultiplier,
                reward = baseReward + wave,
                damage = 4
            )
        }
    }

    /**
     * Moves the enemy towards the next point in its route.
     *
     * @param waypoints The list of waypoints the enemy follows.
     */
    fun move(waypoints: List<PointF>) {
        if (currentWaypoint >= waypoints.size) return  // Verifica si ya alcanzó el último punto.

        val target = waypoints[currentWaypoint]
        val dx = target.x - x  // Distancia en X hacia el destino.
        val dy = target.y - y  // Distancia en Y hacia el destino.
        val distance = sqrt(dx.pow(2) + dy.pow(2))

        if (distance < speed) {
            currentWaypoint++
            if (currentWaypoint < waypoints.size) {
                move(waypoints)  // Llama de nuevo para ajustar al siguiente punto.
            }
        } else {
            x += (dx / distance) * speed
            y += (dy / distance) * speed
        }
    }

    /**
     * Checks if the enemy has reached the end point.
     *
     * @param endPoint The end point to check against.
     * @return True if the enemy is within 10 units of the end point, false otherwise.
     */
    fun reachedEnd(endPoint: PointF): Boolean {
        val distance = sqrt((x - endPoint.x).pow(2) + (y - endPoint.y).pow(2))
        return distance < 10f  // Verifica si está dentro del radio de 10 unidades.
    }

    /**
     * Checks if the enemy has been defeated.
     *
     * @return True if the enemy's health is 0 or less, false otherwise.
     */
    fun isDead(): Boolean = health <= 0
}