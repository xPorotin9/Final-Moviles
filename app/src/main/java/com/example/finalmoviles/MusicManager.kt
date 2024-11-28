package com.example.finalmoviles

import android.content.Context
import android.media.MediaPlayer

class MusicManager(private val context: Context) {
    private var mainThemePlayer: MediaPlayer? = null
    private var gameOverPlayer: MediaPlayer? = null

    fun loadMainTheme() {
        mainThemePlayer?.release() // Liberar cualquier reproductor existente
        mainThemePlayer = MediaPlayer.create(context, R.raw.glamour)
        mainThemePlayer?.isLooping = true
    }

    fun playMainTheme() {
        if (mainThemePlayer == null) {
            loadMainTheme()
        }
        mainThemePlayer?.start()
    }

    fun pauseMainTheme() {
        mainThemePlayer?.pause()
    }

    // Nuevo método para verificar si la música principal está sonando
    fun isMainThemePlaying(): Boolean {
        return mainThemePlayer?.isPlaying == true
    }

    fun loadGameOverTheme() {
        gameOverPlayer = MediaPlayer.create(context, R.raw.gameover)
    }

    fun playGameOverTheme() {
        pauseMainTheme()
        gameOverPlayer?.start()
    }

    fun releaseMediaPlayers() {
        mainThemePlayer?.release()
        gameOverPlayer?.release()
    }
}