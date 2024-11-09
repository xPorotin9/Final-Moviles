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

        val score = arguments?.getInt(ARG_SCORE, 0) ?: 0
        val wave = arguments?.getInt(ARG_WAVE, 0) ?: 0

        view.findViewById<TextView>(R.id.tvGameOverTitle).text = getString(R.string.fin_juego_titulo)
        view.findViewById<TextView>(R.id.tvFinalScore).text = getString(R.string.fin_juego_puntuacion, score)
        view.findViewById<TextView>(R.id.tvWaveReached).text = getString(R.string.fin_juego_oleada, wave)

        view.findViewById<Button>(R.id.btnPlayAgain).apply {
            text = getString(R.string.fin_juego_jugar_nuevo)
            setOnClickListener {
                parentFragmentManager.commit {
                    replace(R.id.fragmentContainer, GameFragment())
                }
            }
        }

        view.findViewById<Button>(R.id.btnMainMenu).apply {
            text = getString(R.string.fin_juego_menu_principal)
            setOnClickListener {
                parentFragmentManager.commit {
                    replace(R.id.fragmentContainer, MainMenuFragment())
                }
            }
        }
    }
}