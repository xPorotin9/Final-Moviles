package com.example.finalmoviles

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.commit
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class GameFragment : Fragment(), GameView.GameCallbacks {
    private var playerLives = 20  // Vidas iniciales del jugador.
    private var playerMoney = 75  // Monedas inicial del jugador.
    private var currentWave = 1
    private var score = 0  // Puntuación
    private var waveInProgress = false  // Indica si una ola está en progreso.

    private var selectedTowerType: TowerType? = null  // Tipo de torre seleccionada.

    // Elementos de la interfaz para mostrar estado de juego.
    private lateinit var tvLives: TextView
    private lateinit var tvMoney: TextView
    private lateinit var tvWave: TextView
    private lateinit var tvScore: TextView
    private lateinit var gameView: GameView

    enum class TowerType(val cost: Int, val damage: Int, val range: Float) { // Datos de torres.
        BASIC(50, 5, 150f),
        ADVANCED(120, 12, 200f)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_game, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Inicializa los elementos de la interfaz.
        tvLives = view.findViewById(R.id.tvLives)
        tvMoney = view.findViewById(R.id.tvMoney)
        tvWave = view.findViewById(R.id.tvWave)
        tvScore = view.findViewById(R.id.tvScore)
        gameView = view.findViewById(R.id.gameView)
        gameView.setGameCallbacks(this)

        setupTowerSelectors()  // Configura botones para seleccionar torres.
        setupGameViewTouchListener()  // Configura la detección de toques en la vista de juego.
        updateUI()
        startGame()
    }

    // Configura las opciones de selección de torre en la interfaz.
    private fun setupTowerSelectors() {
        view?.findViewById<LinearLayout>(R.id.basicTower)?.let { basicTowerLayout ->
            basicTowerLayout.findViewById<TextView>(R.id.basicTowerName)?.text =
                getString(R.string.torre_basica_nombre)
            basicTowerLayout.findViewById<TextView>(R.id.basicTowerCost)?.text =
                getString(R.string.torre_basica_costo, TowerType.BASIC.cost)

            // Selecciona la torre básica al hacer clic.
            basicTowerLayout.setOnClickListener {
                selectedTowerType = TowerType.BASIC
            }
        }

        view?.findViewById<LinearLayout>(R.id.advancedTower)?.let { advancedTowerLayout ->
            advancedTowerLayout.findViewById<TextView>(R.id.advancedTowerName)?.text =
                getString(R.string.torre_avanzada_nombre)
            advancedTowerLayout.findViewById<TextView>(R.id.advancedTowerCost)?.text =
                getString(R.string.torre_avanzada_costo, TowerType.ADVANCED.cost)

            // Selecciona la torre avanzada al hacer clic.
            advancedTowerLayout.setOnClickListener {
                selectedTowerType = TowerType.ADVANCED
            }
        }
    }

    // Configura el detector de toques en la vista del juego.
    @SuppressLint("ClickableViewAccessibility")
    private fun setupGameViewTouchListener() {
        gameView.setOnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_UP && selectedTowerType != null) {
                val towerType = selectedTowerType ?: return@setOnTouchListener true

                if (playerMoney >= towerType.cost) {
                    val tower = Tower(
                        x = event.x,
                        y = event.y,
                        damage = towerType.damage,
                        range = towerType.range,
                        type = towerType
                    )

                    if (gameView.addTower(tower)) {
                        playerMoney -= towerType.cost
                        updateUI()
                    }
                } else {
                    // Mostrar mensaje de monedas insuficientes
                    gameView.showError("No tienes suficientes monedas")
                }
                selectedTowerType = null
            }
            true
        }
    }

    // Actualiza la interfaz con la información actual del juego.
    @SuppressLint("SetTextI18n")
    private fun updateUI() {
        tvLives.text = getString(R.string.juego_vidas, playerLives)
        tvMoney.text = getString(R.string.juego_monedas, playerMoney)
        tvWave.text = getString(R.string.juego_oleada, currentWave)
        tvScore.text = getString(R.string.juego_puntos, score)
    }

    // Inicia el bucle del juego.
    private fun startGame() {
        viewLifecycleOwner.lifecycleScope.launch {
            while (playerLives > 0 && isActive) {
                waveInProgress = true
                startWave()  // Inicia una nueva ola de enemigos.
                delay(getWaveDuration())  // Espera el tiempo de duración de la ola.

                // Otorga recompensas si el jugador sigue en juego.
                if (playerLives > 0) {
                    val waveReward = 20 + (currentWave * 5)
                    playerMoney += waveReward
                    score += 100 + (currentWave * 25)
                    currentWave++
                    updateUI()
                }
                waveInProgress = false
                delay(3000)  // Pausa antes de la siguiente ola.
            }
            gameOver()  // Termina el juego si el jugador pierde todas las vidas.
        }
    }

    // Inicia el despliegue de enemigos en una ola.
    private fun startWave() {
        val enemyCount = 5 + (currentWave * 2)  // Calcula cantidad de enemigos en la ola.
        viewLifecycleOwner.lifecycleScope.launch {
            repeat(enemyCount) {
                gameView.spawnEnemy(currentWave)  // Agrega un enemigo en la vista.
                delay(1000L - (currentWave * 50L).coerceAtMost(800L))  // Espera entre enemigos.
            }
        }
    }

    private fun getWaveDuration(): Long {
        return (10000 + (currentWave * 1000)).toLong()  // Duración de cada ola en milisegundos.
    }

    // Maneja eventos cuando el enemigo llega al final del camino.
    override fun onEnemyReachedEnd(enemy: Enemy) {
        viewLifecycleOwner.lifecycleScope.launch(Dispatchers.Main) {
            playerLives -= enemy.damage  // Resta vidas del jugador.
            updateUI()

            if (playerLives <= 0) {
                gameOver()  // Termina el juego si no quedan vidas.
            }
        }
    }

    // Maneja eventos cuando el enemigo es derrotado.
    override fun onEnemyKilled(enemy: Enemy) {
        viewLifecycleOwner.lifecycleScope.launch(Dispatchers.Main) {
            playerMoney += enemy.reward  // Incrementa el dinero del jugador.
            score += 20 + currentWave  // Incrementa la puntuación.
            updateUI()
        }
    }

    private fun gameOver() {
        gameView.gameScope.cancel()  // Detiene la vista de juego.
        parentFragmentManager.commit {
            replace(R.id.fragmentContainer, GameOverFragment.newInstance(score, currentWave))  // Muestra el fragmento de fin de juego.
            addToBackStack(null)
        }
    }

}
