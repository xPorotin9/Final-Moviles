package com.example.finalmoviles

import android.content.Context
import android.media.MediaPlayer
import android.util.Log

class MusicManager(private val context: Context) {
    private var mainThemePlayer: MediaPlayer? = null
    private var gameOverPlayer: MediaPlayer? = null

    fun loadMainTheme() {
        try {
            // Liberar cualquier reproductor existente
            mainThemePlayer?.release()

            // Crear nuevo reproductor
            mainThemePlayer = MediaPlayer.create(context, R.raw.glamour)
            mainThemePlayer?.isLooping = true
        } catch (e: Exception) {
            Log.e("MusicManager", "Error", e)
        }
    }

    fun playMainTheme() {
        try {
            // Detener y liberar música de game over si existe
            stopAndReleaseGameOverTheme()

            // Si no hay reproductor, cargarlo primero
            if (mainThemePlayer == null) {
                loadMainTheme()
            }

            // Si no está reproduciendo, iniciar
            if (mainThemePlayer?.isPlaying == false) {
                mainThemePlayer?.start()
            }
        } catch (e: Exception) {
            Log.e("MusicManager", "Error", e)
        }
    }

    fun pauseMainTheme() {
        mainThemePlayer?.pause()
    }

    fun isMainThemePlaying(): Boolean {
        return mainThemePlayer?.isPlaying == true
    }

    fun loadGameOverTheme() {
        try {
            // Liberar cualquier reproductor existente de game over
            gameOverPlayer?.release()
            gameOverPlayer = MediaPlayer.create(context, R.raw.gameover)
        } catch (e: Exception) {
            Log.e("MusicManager", "Error", e)
        }
    }

    fun playGameOverTheme() {
        try {
            // Detener música principal
            pauseMainTheme()

            // Liberar cualquier reproductor existente
            stopAndReleaseGameOverTheme()

            // Cargar y reproducir
            loadGameOverTheme()
            gameOverPlayer?.start()
        } catch (e: Exception) {
            Log.e("MusicManager", "Error", e)
        }
    }

    fun stopAndReleaseGameOverTheme() {
        try {
            gameOverPlayer?.let {
                if (it.isPlaying) {
                    it.stop()
                }
                it.release()
                gameOverPlayer = null  // Establecer a null después de liberar
            }
        } catch (e: Exception) {
            Log.e("MusicManager", "Error", e)
        }
    }

    fun releaseMediaPlayers() {
        mainThemePlayer?.release()
        gameOverPlayer?.release()
        mainThemePlayer = null
        gameOverPlayer = null
    }
}