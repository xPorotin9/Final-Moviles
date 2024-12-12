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
 * MainMenuFragment is responsible for displaying the main menu of the game.
 * It provides options to start the game or view high scores.
 */
class MainMenuFragment : Fragment() {
    companion object {
        /**
         * Creates a new instance of MainMenuFragment.
         *
         * @return A new instance of MainMenuFragment.
         */
        fun newInstance(): MainMenuFragment {
            return MainMenuFragment()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_main_menu, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Configure the play button
        view.findViewById<Button>(R.id.btnPlay).apply {
            text = getString(R.string.menu_boton_jugar)
            setOnClickListener {
                parentFragmentManager.commit {
                    replace(R.id.fragmentContainer, GameFragment())
                    addToBackStack(null)
                }
            }
        }

        // Configure the high scores button
        view.findViewById<Button>(R.id.btnHighScores).apply {
            text = getString(R.string.menu_boton_puntuaciones)
            setOnClickListener {
                // To be implemented
            }
        }
    }
}