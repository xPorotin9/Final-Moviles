package com.example.finalmoviles

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.commit

/**
 * GameOverFragment is responsible for displaying the game over screen.
 * It shows the final score, the wave reached, and provides options to play again or return to the main menu.
 */
class GameOverFragment : Fragment() {
    private lateinit var musicManager: MusicManager

    companion object {
        private const val ARG_SCORE = "score"
        private const val ARG_WAVE = "wave"

        /**
         * Creates a new instance of GameOverFragment with the provided score and wave.
         *
         * @param score The final score achieved by the player.
         * @param wave The wave reached by the player.
         * @return A new instance of GameOverFragment.
         */
        fun newInstance(score: Int, wave: Int): GameOverFragment {
            return GameOverFragment().apply {
                arguments = Bundle().apply {
                    putInt(ARG_SCORE, score)
                    putInt(ARG_WAVE, wave)
                }
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_game_over, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // Obtener la instancia compartida del MusicManager
        musicManager = (activity as MainActivity).getMusicManager()

        val score = arguments?.getInt(ARG_SCORE, 0) ?: 0
        val wave = arguments?.getInt(ARG_WAVE, 0) ?: 0
        view.findViewById<TextView>(R.id.tvGameOverTitle).text = getString(R.string.fin_juego_titulo)
        view.findViewById<TextView>(R.id.tvFinalScore).text = getString(R.string.fin_juego_puntuacion, score)
        view.findViewById<TextView>(R.id.tvWaveReached).text = getString(R.string.fin_juego_oleada, wave)

        view.findViewById<Button>(R.id.btnPlayAgain).apply {
            setOnClickListener {
                musicManager.stopAndReleaseGameOverTheme()
                parentFragmentManager.commit {
                    replace(R.id.fragmentContainer, GameFragment())
                }
            }
        }

        view.findViewById<Button>(R.id.btnMainMenu).apply {
            text = getString(R.string.fin_juego_menu_principal)
            setOnClickListener {
                musicManager.stopAndReleaseGameOverTheme()
                parentFragmentManager.commit {
                    replace(R.id.fragmentContainer, MainMenuFragment())
                }
            }
        }
    }

    override fun onPause() {
        super.onPause()
        // Additional precaution to release resources
        musicManager.stopAndReleaseGameOverTheme()
    }

    override fun onDestroy() {
        super.onDestroy()
        // Ensure to release music resources
        musicManager.stopAndReleaseGameOverTheme()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // Solo detener la música si el fragmento está siendo destruido completamente
        if (isRemoving) {
            musicManager.stopAndReleaseGameOverTheme()
        }
    }
}