package com.example.finalmoviles

import android.annotation.SuppressLint
import android.os.Bundle
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

/**
 * GameFragment is responsible for managing the game UI and logic.
 * It handles the game lifecycle, user interactions, and updates the UI accordingly.
 */
class GameFragment : Fragment(), GameView.GameCallbacks {
    private var playerLives = 20
    private var playerMoney = 75
    private var currentWave = 1
    private var score = 0
    private var waveInProgress = false

    private var selectedTowerType: TowerType? = null

    private lateinit var tvLives: TextView
    private lateinit var tvMoney: TextView
    private lateinit var tvWave: TextView
    private lateinit var tvScore: TextView
    private lateinit var gameView: GameView

    private lateinit var musicManager: MusicManager

    /**
     * Enum class representing different types of towers with their respective cost, damage, and range.
     */
    enum class TowerType(val cost: Int, val damage: Int, val range: Float) {
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

        musicManager = (activity as MainActivity).getMusicManager()
        musicManager.playMainTheme()

        tvLives = view.findViewById(R.id.tvLives)
        tvMoney = view.findViewById(R.id.tvMoney)
        tvWave = view.findViewById(R.id.tvWave)
        tvScore = view.findViewById(R.id.tvScore)
        gameView = view.findViewById(R.id.gameView)
        gameView.setGameCallbacks(this)

        setupTowerSelectors()
        setupGameViewTouchListener()
        updateUI()
        startGame()
    }

    /**
     * Sets up the tower selectors and their click listeners.
     */
    private fun setupTowerSelectors() {
        view?.findViewById<LinearLayout>(R.id.basicTower)?.let { basicTowerLayout ->
            basicTowerLayout.findViewById<TextView>(R.id.basicTowerName)?.text =
                getString(R.string.torre_basica_nombre)
            basicTowerLayout.findViewById<TextView>(R.id.basicTowerCost)?.text =
                getString(R.string.torre_basica_costo, TowerType.BASIC.cost)

            basicTowerLayout.setOnClickListener {
                selectedTowerType = TowerType.BASIC
            }
        }

        view?.findViewById<LinearLayout>(R.id.advancedTower)?.let { advancedTowerLayout ->
            advancedTowerLayout.findViewById<TextView>(R.id.advancedTowerName)?.text =
                getString(R.string.torre_avanzada_nombre)
            advancedTowerLayout.findViewById<TextView>(R.id.advancedTowerCost)?.text =
                getString(R.string.torre_avanzada_costo, TowerType.ADVANCED.cost)

            advancedTowerLayout.setOnClickListener {
                selectedTowerType = TowerType.ADVANCED
            }
        }
    }

    /**
     * Sets up the touch listener for the game view to handle tower placement.
     */
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
                    gameView.showError("No tienes suficientes monedas")
                }
                selectedTowerType = null
            }
            true
        }
    }

    /**
     * Updates the UI elements with the current game state.
     */
    @SuppressLint("SetTextI18n")
    private fun updateUI() {
        tvLives.text = getString(R.string.juego_vidas, playerLives)
        tvMoney.text = getString(R.string.juego_monedas, playerMoney)
        tvWave.text = getString(R.string.juego_oleada, currentWave)
        tvScore.text = getString(R.string.juego_puntos, score)
    }

    /**
     * Starts the game loop, managing waves and updating the game state.
     */
    private fun startGame() {
        // Ensure the main theme music is playing at the start
        musicManager.loadMainTheme()
        musicManager.playMainTheme()

        viewLifecycleOwner.lifecycleScope.launch {
            while (playerLives > 0 && isActive) {
                // Check and play main theme music if not already playing
                if (!musicManager.isMainThemePlaying()) {
                    musicManager.playMainTheme()
                }

                waveInProgress = true
                startWave()
                delay(getWaveDuration())

                if (playerLives > 0) {
                    val waveReward = 20 + (currentWave * 5)
                    playerMoney += waveReward
                    score += 100 + (currentWave * 25)
                    currentWave++
                    updateUI()
                }
                waveInProgress = false
                delay(3000)
            }
            gameOver()
        }
    }

    /**
     * Starts a new wave of enemies.
     */
    private fun startWave() {
        val enemyCount = 5 + (currentWave * 2)
        viewLifecycleOwner.lifecycleScope.launch {
            repeat(enemyCount) {
                gameView.spawnEnemy(currentWave)
                delay(1000L - (currentWave * 50L).coerceAtMost(800L))
            }
        }
    }

    /**
     * Calculates the duration of the current wave.
     *
     * @return The duration of the wave in milliseconds.
     */
    private fun getWaveDuration(): Long {
        return (10000 + (currentWave * 1000)).toLong()
    }

    override fun onEnemyReachedEnd(enemy: Enemy) {
        viewLifecycleOwner.lifecycleScope.launch(Dispatchers.Main) {
            playerLives -= enemy.damage
            updateUI()

            if (playerLives <= 0) {
                gameOver()
            }
        }
    }

    override fun onEnemyKilled(enemy: Enemy) {
        viewLifecycleOwner.lifecycleScope.launch(Dispatchers.Main) {
            playerMoney += enemy.reward
            score += 20 + currentWave
            updateUI()
        }
    }

    /**
     * Handles the game over state, stopping the game and transitioning to the GameOverFragment.
     */
    private fun gameOver() {
        musicManager.stopAndReleaseGameOverTheme() // Liberar cualquier música anterior
        musicManager.loadGameOverTheme()
        musicManager.playGameOverTheme()

        // Add a small delay to allow the music to start playing
        viewLifecycleOwner.lifecycleScope.launch {
            delay(500) // Small delay to ensure the music starts
            gameView.gameScope.cancel()
            parentFragmentManager.commit {
                replace(R.id.fragmentContainer, GameOverFragment.newInstance(score, currentWave))
                addToBackStack(null)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        viewLifecycleOwner.lifecycleScope.cancel()
        musicManager.stopAndReleaseGameOverTheme()
    }
}