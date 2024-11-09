package com.example.finalmoviles

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import androidx.fragment.app.FragmentActivity
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
    val gameScope = CoroutineScope(Dispatchers.Main + Job())

    // Zona final con RectF para mejor detección de colisiones
    private lateinit var endZone: RectF

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

    // Añade esta interfaz al inicio de la clase
    interface GameCallbacks {
        fun onEnemyReachedEnd(enemy: Enemy)
        fun onEnemyKilled(enemy: Enemy)
    }

    // Añade esta propiedad
    private var gameCallbacks: GameCallbacks? = null

    // Añade este método
    fun setGameCallbacks(callbacks: GameCallbacks) {
        gameCallbacks = callbacks
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        val lastPoint = waypoints.last()
        endZone = RectF(
            lastPoint.x - 40f,
            lastPoint.y - 60f,
            lastPoint.x + 40f,
            lastPoint.y + 60f
        )
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

    private fun checkEnemyCollision(enemy: Enemy): Boolean {
        // Crear un círculo para el enemigo (usando su posición y radio)
        val enemyRadius = 20f
        val enemyBounds = RectF(
            enemy.x - enemyRadius,
            enemy.y - enemyRadius,
            enemy.x + enemyRadius,
            enemy.y + enemyRadius
        )
        return enemyBounds.intersect(endZone)
    }

    private fun updateEnemies() {
        val iterator = enemies.iterator()
        while (iterator.hasNext()) {
            val enemy = iterator.next()
            enemy.move(waypoints)

            if (checkEnemyCollision(enemy)) {
                (context as? FragmentActivity)?.runOnUiThread {
                    gameCallbacks?.onEnemyReachedEnd(enemy)
                }
                iterator.remove()
                continue
            }

            if (enemy.isDead()) {
                iterator.remove()
                gameCallbacks?.onEnemyKilled(enemy)
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
        canvas.drawColor(Color.DKGRAY)

        // Dibujar camino
        paint.apply {
            color = Color.GRAY
            style = Paint.Style.STROKE
            strokeWidth = 50f
        }
        canvas.drawPath(path, paint)

        // Dibujar zona final
        paint.apply {
            color = Color.RED
            style = Paint.Style.FILL
            alpha = 80 // Más transparente
        }
        canvas.drawRect(endZone, paint)

        // Borde de la zona final
        paint.apply {
            style = Paint.Style.STROKE
            strokeWidth = 5f
            alpha = 255 // Completamente opaco
        }
        canvas.drawRect(endZone, paint)

        // Dibujar torres
        towers.forEach { tower ->
            paint.apply {
                style = Paint.Style.FILL
                color = when (tower.type) {
                    GameFragment.TowerType.BASIC -> Color.BLUE
                    GameFragment.TowerType.ADVANCED -> Color.RED
                }
                alpha = 255
            }
            canvas.drawRect(
                tower.x - 25f,
                tower.y - 25f,
                tower.x + 25f,
                tower.y + 25f,
                paint
            )

            // Dibujar rango
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
        enemies.forEach { enemy ->
            // Cuerpo del enemigo
            paint.apply {
                style = Paint.Style.FILL
                color = Color.RED
                alpha = 255
            }
            canvas.drawCircle(enemy.x, enemy.y, 20f, paint)


            paint.color = Color.BLACK
            canvas.drawRect(
                enemy.x - 25f,
                enemy.y - 35f,
                enemy.x + 25f,
                enemy.y - 30f,
                paint
            )
            // Vida actual en verde
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

    fun spawnEnemy(wave: Int) {
        enemies.add(Enemy.createForWave(wave, waypoints[0]))
    }

    fun addTower(tower: Tower) {
        towers.add(tower)
    }
}