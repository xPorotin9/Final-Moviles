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

    private val paint = Paint()  // Pincel para dibujar elementos.
    private val path = Path()  // Camino para los enemigos.
    private val enemies = mutableListOf<Enemy>()  // Lista de enemigos en el juego.
    private val towers = mutableListOf<Tower>()  // Lista de torres en el juego.
    val gameScope = CoroutineScope(Dispatchers.Main + Job())  // Alcance para la corutina del juego.

    // Rectángulo que representa la zona final del juego.
    private lateinit var endZone: RectF

    // Puntos de ruta que siguen los enemigos.
    private val waypoints = listOf(
        PointF(0f, 300f),
        PointF(200f, 300f),
        PointF(200f, 500f),
        PointF(800f, 500f)
    )

    init {
        // Configura el camino que seguirán los enemigos.
        path.moveTo(waypoints[0].x, waypoints[0].y)
        for (i in 1 until waypoints.size) {
            path.lineTo(waypoints[i].x, waypoints[i].y)
        }
        startGameLoop()  // Inicia el bucle de juego.
    }

    // Interfaz de callbacks para eventos del juego.
    interface GameCallbacks {
        fun onEnemyReachedEnd(enemy: Enemy)  // Llamado cuando un enemigo llega al final.
        fun onEnemyKilled(enemy: Enemy)  // Llamado cuando un enemigo es derrotado.
    }

    private var gameCallbacks: GameCallbacks? = null  // Referencia a los callbacks de juego.

    // Configura los callbacks para los eventos del juego.
    fun setGameCallbacks(callbacks: GameCallbacks) {
        gameCallbacks = callbacks
    }

    // Configura el tamaño de la vista y la zona final.
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

    // Inicia el bucle principal de actualización de la vista de juego.
    private fun startGameLoop() {
        gameScope.launch {
            while (isActive) {
                updateEnemies()  // Actualiza la posición de los enemigos.
                updateTowers()  // Actualiza las acciones de las torres.
                invalidate()  // Redibuja la vista.
                delay(16)  // Retraso para simular 60 FPS.
            }
        }
    }

    // Verifica si un enemigo ha colisionado con la zona final.
    private fun checkEnemyCollision(enemy: Enemy): Boolean {
        val enemyRadius = 20f
        val enemyBounds = RectF(
            enemy.x - enemyRadius,
            enemy.y - enemyRadius,
            enemy.x + enemyRadius,
            enemy.y + enemyRadius
        )
        return enemyBounds.intersect(endZone)
    }

    // Actualiza la posición y estado de cada enemigo.
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

    // Actualiza el comportamiento de cada torre para atacar enemigos dentro de su rango.
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
                enemy.health -= tower.damage  // Disminuye la vida del enemigo si está en rango.
            }
        }
    }

    // Dibuja los elementos en la vista.
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        // Dibujar fondo.
        canvas.drawColor(Color.DKGRAY)

        // Dibujar camino.
        paint.apply {
            color = Color.GRAY
            style = Paint.Style.STROKE
            strokeWidth = 50f
        }
        canvas.drawPath(path, paint)

        // Dibujar zona final.
        paint.apply {
            color = Color.RED
            style = Paint.Style.FILL
            alpha = 80  // Transparencia.
        }
        canvas.drawRect(endZone, paint)

        // Dibujar torres en el área de juego.
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
        }

        // Dibujar enemigos con indicador de salud.
        enemies.forEach { enemy ->
            paint.apply {
                style = Paint.Style.FILL
                color = Color.RED
                alpha = 255
            }
            canvas.drawCircle(enemy.x, enemy.y, 20f, paint)  // Dibuja cuerpo del enemigo.

            paint.color = Color.GREEN  // Barra de salud.
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

    // Agrega un nuevo enemigo al inicio de la ruta.
    fun spawnEnemy(wave: Int) {
        enemies.add(Enemy.createForWave(wave, waypoints[0]))
    }

    // Agrega una torre a la lista de torres en la vista.
    fun addTower(tower: Tower) {
        towers.add(tower)
    }
}