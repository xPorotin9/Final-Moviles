package com.example.finalmoviles

import android.graphics.PointF
import kotlin.math.pow
import kotlin.math.sqrt

data class Enemy(
    var x: Float,
    var y: Float,
    var health: Int,
    private var currentWaypoint: Int = 0,
    private val speed: Float,
    val reward: Int,
    val damage: Int
) {
    companion object {
        fun createForWave(wave: Int, startPoint: PointF): Enemy {
            // Incremento de estadísticas por oleada
            val healthMultiplier = 1.0f + (wave * 0.2f) // +20% de vida por oleada
            val speedMultiplier = 1.0f + (wave * 0.1f)  // +10% de velocidad por oleada
            val baseHealth = 100
            val baseSpeed = 2f
            val baseReward = 5

            return Enemy(
                x = startPoint.x,
                y = startPoint.y,
                health = (baseHealth * healthMultiplier).toInt(),
                speed = baseSpeed * speedMultiplier,
                reward = baseReward + wave, // Más recompensa en oleadas avanzadas
                damage = 1 + (wave * 3) // Más daño cada 3 oleadas
            )
        }
    }

    fun move(waypoints: List<PointF>) {
        if (currentWaypoint >= waypoints.size) return

        val target = waypoints[currentWaypoint]
        val dx = target.x - x
        val dy = target.y - y
        val distance = sqrt(dx.pow(2) + dy.pow(2))

        if (distance < speed) {
            currentWaypoint++
            if (currentWaypoint < waypoints.size) {
                move(waypoints)
            }
        } else {
            x += (dx / distance) * speed
            y += (dy / distance) * speed
        }
    }

    fun reachedEnd(endPoint: PointF): Boolean {
        return currentWaypoint >= endPoint.x &&
                sqrt((x - endPoint.x).pow(2) + (y - endPoint.y).pow(2)) < 10
    }

    // Nuevo método para saber si el enemigo murió por daño
    fun isDead(): Boolean = health <= 0
}
