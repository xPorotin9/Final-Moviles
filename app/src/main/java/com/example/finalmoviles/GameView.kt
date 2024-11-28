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
    private val gridSize = 60f
    private val gridSpacing = 5f
    private val towerSize = 40f
    private val gridCells = mutableMapOf<Pair<Int, Int>, Boolean>()
    private var selectedCell: Pair<Int, Int>? = null
    private var errorMessage: String? = null
    private var errorMessageTimeout = 0L

    // Rectángulo que representa la zona final del juego.
    private val waypoints = mutableListOf<PointF>()
    private lateinit var endZone: RectF

    init {
        startGameLoop()  // Inicia el bucle de juego.
    }

    // Configura el tamaño de la vista y la zona final.
    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)

        // Calcular los waypoints basados en la cuadrícula
        val gridCol = (gridSize + gridSpacing)
        val gridRow = (gridSize + gridSpacing)

        // Limpiar waypoints anteriores
        waypoints.clear()

        // Crear un camino más largo y con más giros que llegue hasta el fondo
        waypoints.addAll(listOf(
            PointF(0f, 2 * gridRow),                    // Inicio desde la izquierda
            PointF(4 * gridCol, 2 * gridRow),           // Derecha
            PointF(4 * gridCol, 4 * gridRow),           // Baja
            PointF(12 * gridCol, 4 * gridRow),          // Derecha
            PointF(12 * gridCol, 6 * gridRow),          // Baja
            PointF(2 * gridCol, 6 * gridRow),           // Izquierda
            PointF(2 * gridCol, 8 * gridRow),           // Baja
            PointF(14 * gridCol, 8 * gridRow),          // Derecha
            PointF(14 * gridCol, 12 * gridRow),         // Baja
            PointF(4 * gridCol, 12 * gridRow),          // Izquierda
            PointF(4 * gridCol, 16 * gridRow),          // Baja
            PointF(12 * gridCol, 16 * gridRow),         // Derecha
            PointF(12 * gridCol, 20 * gridRow),         // Baja
            PointF(2 * gridCol, 20 * gridRow),          // Izquierda
            PointF(2 * gridCol, 22 * gridRow),          // Baja
            PointF(15 * gridCol, 22 * gridRow)          // Final derecha
        ))

        // Actualizar el path
        path.reset()
        path.moveTo(waypoints[0].x, waypoints[0].y)
        for (i in 1 until waypoints.size) {
            path.lineTo(waypoints[i].x, waypoints[i].y)
        }

        // Actualizar la zona final
        val lastPoint = waypoints.last()
        endZone = RectF(
            lastPoint.x - 40f,
            lastPoint.y - 60f,
            lastPoint.x + 40f,
            lastPoint.y + 60f
        )
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

        // Crear un margen más preciso alrededor del camino
        val pathWidth = 50f // Debe coincidir con el strokeWidth del camino
        val margin = pathWidth / 2 + 5f // Un pequeño margen adicional

        // Comprobar si el punto está cerca de algún segmento del camino
        for (i in 0 until waypoints.size - 1) {
            val x1 = waypoints[i].x
            val y1 = waypoints[i].y
            val x2 = waypoints[i + 1].x
            val y2 = waypoints[i + 1].y

            // Si es un segmento horizontal
            if (y1 == y2) {
                val minX = minOf(x1, x2)
                val maxX = maxOf(x1, x2)
                if (x >= minX - margin && x <= maxX + margin &&
                    y >= y1 - margin && y <= y1 + margin) {
                    return false
                }
            }
            // Si es un segmento vertical
            else if (x1 == x2) {
                val minY = minOf(y1, y2)
                val maxY = maxOf(y1, y2)
                if (y >= minY - margin && y <= maxY + margin &&
                    x >= x1 - margin && x <= x1 + margin) {
                    return false
                }
            }
        }

        return true
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
        if (waypoints.isNotEmpty()) {
            enemies.add(Enemy.createForWave(wave, waypoints[0]))
        }
    }
}
