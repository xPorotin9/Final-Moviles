package com.example.finalmoviles

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.commit

class GameOverFragment : Fragment() {
    private lateinit var musicManager: MusicManager

    companion object {
        private const val ARG_SCORE = "score"
        private const val ARG_WAVE = "wave"

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


        // Inicializar MusicManager
        musicManager = MusicManager(requireContext())

        // Detener completamente cualquier música de game over
        musicManager.stopAndReleaseGameOverTheme()

        // Obtiene la puntuación y la ola alcanzada de los argumentos.
        val score = arguments?.getInt(ARG_SCORE, 0) ?: 0
        val wave = arguments?.getInt(ARG_WAVE, 0) ?: 0

        // Muestra el título de fin de juego y la puntuación en los TextViews.
        view.findViewById<TextView>(R.id.tvGameOverTitle).text = getString(R.string.fin_juego_titulo)
        view.findViewById<TextView>(R.id.tvFinalScore).text = getString(R.string.fin_juego_puntuacion, score)
        view.findViewById<TextView>(R.id.tvWaveReached).text = getString(R.string.fin_juego_oleada, wave)

        // Configura el botón para jugar de nuevo.
        view.findViewById<Button>(R.id.btnPlayAgain).apply {
            setOnClickListener {
                // Detener completamente la música de game over
                musicManager.stopAndReleaseGameOverTheme()

                parentFragmentManager.commit {
                    replace(R.id.fragmentContainer, GameFragment())
                }
            }
        }

        // Configura el botón para volver al menú principal.
        view.findViewById<Button>(R.id.btnMainMenu).apply {
            text = getString(R.string.fin_juego_menu_principal)
            setOnClickListener {
                // Asegurarse de detener completamente la música de game over
                musicManager.stopAndReleaseGameOverTheme()

                parentFragmentManager.commit {
                    replace(R.id.fragmentContainer, MainMenuFragment())
                }
            }
        }
    }

    override fun onPause() {
        super.onPause()
        // Adicional precaución para liberar recursos
        musicManager.stopAndReleaseGameOverTheme()
    }

    override fun onDestroy() {
        super.onDestroy()
        // Asegurarse de liberar los recursos de música
        musicManager.stopAndReleaseGameOverTheme()
    }
}