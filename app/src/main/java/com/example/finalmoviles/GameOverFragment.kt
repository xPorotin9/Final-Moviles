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
    companion object {
        private const val ARG_SCORE = "score"
        private const val ARG_WAVE = "wave"

        fun newInstance(score: Int, wave: Int) = GameOverFragment().apply {
            arguments = Bundle().apply {
                putInt(ARG_SCORE, score)
                putInt(ARG_WAVE, wave)
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

        val score = arguments?.getInt(ARG_SCORE, 0) ?: 0
        val wave = arguments?.getInt(ARG_WAVE, 0) ?: 0

        view.findViewById<TextView>(R.id.tvFinalScore).text = "Puntuación Final: $score"
        view.findViewById<TextView>(R.id.tvWaveReached).text = "Oleada Alcanzada: $wave"

        // Configurar el botón de jugar de nuevo
        view.findViewById<Button>(R.id.btnPlayAgain).setOnClickListener {
            parentFragmentManager.commit {
                replace(R.id.fragmentContainer, GameFragment())
            }
        }

        // Configurar el botón de menú principal
        view.findViewById<Button>(R.id.btnMainMenu).setOnClickListener {
            parentFragmentManager.commit {
                replace(R.id.fragmentContainer, MainMenuFragment.newInstance())
            }
        }
    }
}
