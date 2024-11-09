package com.example.finalmoviles

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.commit
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class GameFragment : Fragment() {
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

        tvLives = view.findViewById(R.id.tvLives)
        tvMoney = view.findViewById(R.id.tvMoney)
        tvWave = view.findViewById(R.id.tvWave)
        tvScore = view.findViewById(R.id.tvScore)
        gameView = view.findViewById(R.id.gameView)

        setupTowerSelectors()
        setupGameViewTouchListener()
        updateUI()
        startGame()
    }

    private fun setupTowerSelectors() {
        view?.findViewById<View>(R.id.basicTower)?.setOnClickListener {
            if (playerMoney >= TowerType.BASIC.cost) {
                selectedTowerType = TowerType.BASIC
            }
        }

        view?.findViewById<View>(R.id.advancedTower)?.setOnClickListener {
            if (playerMoney >= TowerType.ADVANCED.cost) {
                selectedTowerType = TowerType.ADVANCED
            }
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setupGameViewTouchListener() {
        gameView.setOnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_UP && selectedTowerType != null) {
                val towerType = selectedTowerType ?: return@setOnTouchListener true

                if (playerMoney >= towerType.cost) {
                    playerMoney -= towerType.cost
                    gameView.addTower(
                        Tower(
                            x = event.x,
                            y = event.y,
                            damage = towerType.damage,
                            range = towerType.range,
                            type = towerType
                        )
                    )
                    updateUI()
                }
                selectedTowerType = null
            }
            true
        }
    }

    @SuppressLint("SetTextI18n")
    private fun updateUI() {
        tvLives.text = "Vidas: $playerLives"
        tvMoney.text = "Monedas: $playerMoney"
        tvWave.text = "Oleada: $currentWave"
        tvScore.text = "Puntos: $score"
    }

    private fun startGame() {
        viewLifecycleOwner.lifecycleScope.launch {
            while (playerLives > 0 && isActive) {
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

    private fun startWave() {
        val enemyCount = 5 + (currentWave * 2)
        viewLifecycleOwner.lifecycleScope.launch {
            repeat(enemyCount) {
                gameView.spawnEnemy(currentWave)
                delay(1000L - (currentWave * 50L).coerceAtMost(800L))
            }
        }
    }

    private fun getWaveDuration(): Long {
        return (10000 + (currentWave * 1000)).toLong()
    }

    fun onEnemyReachedEnd(enemy: Enemy) {
        playerLives -= enemy.damage
        updateUI()

        // Verifica si el juego debe terminar
        if (playerLives <= 0) {
            gameOver()
        }
    }

    fun onEnemyKilled(enemy: Enemy) {
        playerMoney += enemy.reward
        score += 20 + currentWave
        updateUI()
    }


    private fun gameOver() {
        gameView.gameScope.cancel()
        parentFragmentManager.commit {
            replace(R.id.fragmentContainer, GameOverFragment.newInstance(score, currentWave))
            addToBackStack(null)
        }
    }
}