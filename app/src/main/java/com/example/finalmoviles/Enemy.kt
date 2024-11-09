package com.example.finalmoviles

import android.graphics.PointF
import kotlin.math.pow
import kotlin.math.sqrt

class Enemy(
    var x: Float,
    var y: Float,
    var health: Int,
    private var currentWaypoint: Int = 0,
    private val speed: Float,
    val reward: Int,
    val damage: Int = 4
) {

    companion object {
        fun createForWave(wave: Int, startPoint: PointF): Enemy {
            val healthMultiplier = 1.0f + (wave * 0.2f)
            val speedMultiplier = 1.0f + (wave * 0.1f)
            val baseHealth = 100
            val baseSpeed = 2f
            val baseReward = 5

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
        val distance = sqrt((x - endPoint.x).pow(2) + (y - endPoint.y).pow(2))
        return distance < 10f  // Radio de detección para la meta final
    }

    fun isDead(): Boolean = health <= 0
}
