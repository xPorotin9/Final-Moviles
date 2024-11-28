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
    private val gridSize = 60f // Tamaño de cada celda de la cuadrícula
    private val gridSpacing = 5f // Espacio entre celdas
    private val towerSize = 40f // Tamaño de la torre dentro de la celda
    private val gridCells = mutableMapOf<Pair<Int, Int>, Boolean>() // Mapa de celdas ocupadas
    private var selectedCell: Pair<Int, Int>? = null
    private var errorMessage: String? = null
    private var errorMessageTimeout = 0L

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


    // Método para convertir coordenadas de pantalla a coordenadas de cuadrícula
    private fun screenToGrid(x: Float, y: Float): Pair<Int, Int>? {
        val gridX = (x / (gridSize + gridSpacing)).toInt()
        val gridY = (y / (gridSize + gridSpacing)).toInt()

        // Verificar si está dentro de los límites y no en el camino
        if (isValidGridPosition(gridX, gridY)) {
            return Pair(gridX, gridY)
        }
        return null
    }

    // Verificar si una posición de cuadrícula es válida
    private fun isValidGridPosition(gridX: Int, gridY: Int): Boolean {
        val x = gridX * (gridSize + gridSpacing) + gridSize / 2
        val y = gridY * (gridSize + gridSpacing) + gridSize / 2

        // Verificar si está en el camino
        val pathBounds = RectF()
        val pathMeasure = PathMeasure(path, false)
        pathMeasure.getSegment(0f, pathMeasure.length, Path(), true)
        path.computeBounds(pathBounds, true)
        pathBounds.inset(-30f, -30f) // Margen alrededor del camino

        return !pathBounds.contains(x, y)
    }

    // Convertir coordenadas de cuadrícula a coordenadas de pantalla
    private fun gridToScreen(gridX: Int, gridY: Int): Pair<Float, Float> {
        val x = gridX * (gridSize + gridSpacing) + gridSize / 2
        val y = gridY * (gridSize + gridSpacing) + gridSize / 2
        return Pair(x, y)
    }

    fun addTower(tower: Tower): Boolean {
        val gridPos = screenToGrid(tower.x, tower.y) ?: return false

        if (gridCells[gridPos] == true) {
            showError("Ya existe una torre en esta posición")
            return false
        }

        if (!isValidGridPosition(gridPos.first, gridPos.second)) {
            showError("No se puede colocar una torre en el camino")
            return false
        }

        val (screenX, screenY) = gridToScreen(gridPos.first, gridPos.second)
        towers.add(tower.copy(x = screenX, y = screenY))
        gridCells[gridPos] = true
        invalidate()
        return true
    }

    fun showError(message: String) {
        errorMessage = message
        errorMessageTimeout = System.currentTimeMillis() + 2000 // Mostrar por 2 segundos
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        // Dibujar fondo
        canvas.drawColor(Color.parseColor("#4CAF50"))

        // Dibujar cuadrícula
        paint.apply {
            style = Paint.Style.STROKE
            color = Color.WHITE
            alpha = 50
            strokeWidth = 1f
        }

        // Dibujar líneas horizontales y verticales de la cuadrícula
        val numRows = (height / (gridSize + gridSpacing)).toInt()
        val numCols = (width / (gridSize + gridSpacing)).toInt()

        for (i in 0..numRows) {
            for (j in 0..numCols) {
                val x = j * (gridSize + gridSpacing)
                val y = i * (gridSize + gridSpacing)

                if (isValidGridPosition(j, i)) {
                    canvas.drawRect(
                        x, y,
                        x + gridSize,
                        y + gridSize,
                        paint
                    )
                }
            }
        }

        // Dibujar camino
        paint.apply {
            color = Color.parseColor("#9E9E9E")
            style = Paint.Style.STROKE
            strokeWidth = 50f
        }
        canvas.drawPath(path, paint)

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
                tower.x - towerSize/2,
                tower.y - towerSize/2,
                tower.x + towerSize/2,
                tower.y + towerSize/2,
                paint
            )
        }

        // Dibujar enemigos
        enemies.forEach { enemy ->
            paint.apply {
                style = Paint.Style.FILL
                color = Color.RED
                alpha = 255
            }
            canvas.drawCircle(enemy.x, enemy.y, 20f, paint)

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

        // Dibujar mensaje de error si existe
        errorMessage?.let { message ->
            if (System.currentTimeMillis() < errorMessageTimeout) {
                paint.apply {
                    color = Color.WHITE
                    textSize = 40f
                    textAlign = Paint.Align.CENTER
                    style = Paint.Style.FILL
                }
                canvas.drawText(message, width/2f, 100f, paint)
            } else {
                errorMessage = null
            }
        }
    }

    // Agrega un nuevo enemigo al inicio de la ruta.
    fun spawnEnemy(wave: Int) {
        enemies.add(Enemy.createForWave(wave, waypoints[0]))
    }
}
