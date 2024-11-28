package com.example.finalmoviles

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.commit

class MainMenuFragment : Fragment() {
    companion object {
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

        // Ya no es necesario configurar el título de texto, ya que lo manejará el logo

        // Configurar el botón de jugar
        view.findViewById<Button>(R.id.btnPlay).apply {
            text = getString(R.string.menu_boton_jugar)
            setOnClickListener {
                parentFragmentManager.commit {
                    replace(R.id.fragmentContainer, GameFragment())
                    addToBackStack(null)
                }
            }
        }

        // Configurar el botón de puntuaciones
        view.findViewById<Button>(R.id.btnHighScores).apply {
            text = getString(R.string.menu_boton_puntuaciones)
            setOnClickListener {
                // Por implementar c:
            }
        }
    }
}