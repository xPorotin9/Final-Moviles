package com.example.finalmoviles

import android.content.Context
import android.media.MediaPlayer
import android.util.Log

/**
 * MusicManager is responsible for managing the background music and sound effects in the game.
 * It handles loading, playing, pausing, and releasing media players for different game themes.
 *
 * @property context The context of the application.
 */
class MusicManager(private val context: Context) {
    private var mainThemePlayer: MediaPlayer? = null
    private var gameOverPlayer: MediaPlayer? = null

    /**
     * Loads the main theme music.
     * Releases any existing media player and creates a new one for the main theme.
     */
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

    /**
     * Plays the main theme music.
     * Stops and releases the game over music if it is playing.
     * Loads the main theme if it is not already loaded.
     */
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

    /**
     * Pauses the main theme music.
     */
    fun pauseMainTheme() {
        mainThemePlayer?.pause()
    }

    /**
     * Checks if the main theme music is playing.
     *
     * @return True if the main theme is playing, false otherwise.
     */
    fun isMainThemePlaying(): Boolean {
        return mainThemePlayer?.isPlaying == true
    }

    /**
     * Loads the game over theme music.
     * Releases any existing media player and creates a new one for the game over theme.
     */
    fun loadGameOverTheme() {
        try {
            // Liberar cualquier reproductor existente de game over
            gameOverPlayer?.release()
            gameOverPlayer = MediaPlayer.create(context, R.raw.gameover)
        } catch (e: Exception) {
            Log.e("MusicManager", "Error", e)
        }
    }

    /**
     * Plays the game over theme music.
     * Pauses the main theme music and releases any existing game over music player.
     * Loads and starts the game over theme.
     */
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

    /**
     * Stops and releases the game over theme music player.
     */
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

    /**
     * Releases all media players to free up resources.
     */
    fun releaseMediaPlayers() {
        mainThemePlayer?.release()
        gameOverPlayer?.release()
        mainThemePlayer = null
        gameOverPlayer = null
    }
}