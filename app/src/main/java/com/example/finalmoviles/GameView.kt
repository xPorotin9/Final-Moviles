package com.example.finalmoviles

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import kotlinx.coroutines.*
import kotlin.math.pow
import kotlin.math.sqrt

class GameView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val paint = Paint()
    private val path = Path()
    private val enemies = mutableListOf<Enemy>()
    private val towers = mutableListOf<Tower>()
    private val gameScope = CoroutineScope(Dispatchers.Main + Job())

    private val waypoints = listOf(
        PointF(0f, 300f),
        PointF(200f, 300f),
        PointF(200f, 500f),
        PointF(800f, 500f)
    )

    init {
        path.moveTo(waypoints[0].x, waypoints[0].y)
        for (i in 1 until waypoints.size) {
            path.lineTo(waypoints[i].x, waypoints[i].y)
        }

        startGameLoop()
    }

    private fun startGameLoop() {
        gameScope.launch {
            while (isActive) {
                updateEnemies()
                updateTowers()
                invalidate()
                delay(16)
            }
        }
    }

    private fun updateEnemies() {
        val iterator = enemies.iterator()
        while (iterator.hasNext()) {
            val enemy = iterator.next()
            enemy.move(waypoints)

            if (enemy.reachedEnd(waypoints.last())) {
                iterator.remove()
                (context as? GameActivity)?.onEnemyReachedEnd()
            } else if (enemy.health <= 0) {
                iterator.remove()
                (context as? GameActivity)?.onEnemyKilled()
            }
        }
    }

    private fun updateTowers() {
        towers.forEach { tower ->
            val target = enemies.firstOrNull { enemy ->
                val distance = sqrt(
                    (tower.x - enemy.x).pow(2) +
                            (tower.y - enemy.y).pow(2)
                )
                distance <= tower.range
            }

            target?.let { enemy ->
                enemy.health -= tower.damage
            }
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        // Dibujar fondo
        canvas.drawColor(Color.WHITE)

        // Dibujar camino
        paint.apply {
            color = Color.GRAY
            style = Paint.Style.STROKE
            strokeWidth = 50f
        }
        canvas.drawPath(path, paint)

        // Dibujar torres
        towers.forEach { tower ->
            paint.apply {
                style = Paint.Style.FILL
                color = when (tower.type) {
                    GameActivity.TowerType.BASIC -> Color.BLUE
                    GameActivity.TowerType.ADVANCED -> Color.RED
                }
            }
            canvas.drawRect(
                tower.x - 25f,
                tower.y - 25f,
                tower.x + 25f,
                tower.y + 25f,
                paint
            )

            // Dibujar rango (cuando se está colocando)
            if (tower == towers.lastOrNull()) {
                paint.apply {
                    style = Paint.Style.STROKE
                    color = Color.GRAY
                    strokeWidth = 2f
                }
                canvas.drawCircle(tower.x, tower.y, tower.range, paint)
            }
        }

        // Dibujar enemigos
        paint.apply {
            style = Paint.Style.FILL
            color = Color.RED
        }
        enemies.forEach { enemy ->
            canvas.drawCircle(enemy.x, enemy.y, 20f, paint)

            // Barra de vida
            paint.color = Color.BLACK
            canvas.drawRect(
                enemy.x - 25f,
                enemy.y - 35f,
                enemy.x + 25f,
                enemy.y - 30f,
                paint
            )
            paint.color = Color.GREEN
            val healthWidth = (enemy.health / 100f) * 50f
            canvas.drawRect(
                enemy.x - 25f,
                enemy.y - 35f,
                enemy.x - 25f + healthWidth,
                enemy.y - 30f,
                paint
            )
        }
    }

    fun spawnEnemy() {
        enemies.add(Enemy(waypoints[0].x, waypoints[0].y))
    }

    fun addTower(tower: Tower) {
        towers.add(tower)
    }
}