package com.example.finalmoviles

import android.graphics.PointF
import kotlin.math.pow
import kotlin.math.sqrt

data class Enemy(
    var x: Float,
    var y: Float,
    var health: Int = 100,
    private var currentWaypoint: Int = 1,
    private val speed: Float = 2f
) {

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
}