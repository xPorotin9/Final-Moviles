package com.example.finalmoviles

import android.graphics.PointF
import kotlin.math.pow
import kotlin.math.sqrt

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
        // Crea un enemigo con estadísticas ajustadas para la ola especificada.
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

    // Mueve al enemigo hacia el siguiente punto en su ruta.
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

    // Verifica si el enemigo ha alcanzado el punto final.
    fun reachedEnd(endPoint: PointF): Boolean {
        val distance = sqrt((x - endPoint.x).pow(2) + (y - endPoint.y).pow(2))
        return distance < 10f  // Verifica si está dentro del radio de 10 unidades.
    }

    // Verifica si el enemigo ha sido derrotado.
    fun isDead(): Boolean = health <= 0
}