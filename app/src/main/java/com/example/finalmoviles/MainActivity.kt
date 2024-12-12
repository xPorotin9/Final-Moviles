package com.example.finalmoviles

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.commit

/**
 * MainActivity is the entry point of the application.
 * It sets the content view and initializes the main menu fragment if there is no saved instance state.
 */
class MainActivity : AppCompatActivity() {
    /**
     * Called when the activity is starting. This is where most initialization should go.
     *
     * @param savedInstanceState If the activity is being re-initialized after previously being shut down then this Bundle contains the data it most recently supplied in onSaveInstanceState(Bundle). Otherwise it is null.
     */    
    private lateinit var musicManager: MusicManager
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        musicManager = MusicManager(this)

        if (savedInstanceState == null) {
            supportFragmentManager.commit {
                setReorderingAllowed(true)
                add(R.id.fragmentContainer, MainMenuFragment.newInstance())
            }
        }
    }
    override fun onPause() {
        super.onPause()
        musicManager.pauseMainTheme()
    }

    override fun onResume() {
        super.onResume()
        // Solo reproducir si estamos en el juego
        if (supportFragmentManager.findFragmentById(R.id.fragmentContainer) is GameFragment) {
            musicManager.playMainTheme()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        musicManager.releaseMediaPlayers()
    }

    fun getMusicManager(): MusicManager = musicManager

}