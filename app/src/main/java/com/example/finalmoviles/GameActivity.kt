package com.example.finalmoviles

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class GameActivity : AppCompatActivity() {
    private var playerLives = 20
    private var playerMoney = 75
    private var currentWave = 1
    private var score = 0

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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game)

        tvLives = findViewById(R.id.tvLives)
        tvMoney = findViewById(R.id.tvMoney)
        tvWave = findViewById(R.id.tvWave)
        tvScore = findViewById(R.id.tvScore)
        gameView = findViewById(R.id.gameView)

        setupTowerSelectors()
        setupGameViewTouchListener()
        updateUI()
        startGame()
    }

    private fun setupTowerSelectors() {
        findViewById<View>(R.id.basicTower).setOnClickListener {
            if (playerMoney >= TowerType.BASIC.cost) {
                selectedTowerType = TowerType.BASIC
            }
        }

        findViewById<View>(R.id.advancedTower).setOnClickListener {
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
                    gameView.addTower(Tower(
                        x = event.x,
                        y = event.y,
                        damage = towerType.damage,
                        range = towerType.range,
                        type = towerType
                    ))
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
        lifecycleScope.launch {
            while (playerLives > 0) {
                startWave()
                delay(10000) // 20 segundos por oleada
                currentWave++
                playerMoney += 20 // Bonus por oleada
                score += 100
                updateUI()
            }
            gameOver()
        }
    }

    private fun startWave() {
        val enemyCount = 5 + currentWave
        lifecycleScope.launch {
            repeat(enemyCount) {
                gameView.spawnEnemy()
                delay(1000)
            }
        }
    }

    fun onEnemyReachedEnd() {
        playerLives--
        updateUI()
        if (playerLives <= 0) {
            gameOver()
        }
    }

    fun onEnemyKilled() {
        playerMoney += 5
        score += 20
        updateUI()
    }

    private fun gameOver() {
        finish()
    }
}