package com.example.finalmoviles

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.commit
import com.google.firebase.database.FirebaseDatabase

class GameOverFragment : Fragment() {
    private lateinit var musicManager: MusicManager
    private lateinit var btnSaveScore: Button
    private var hasScoreSaved = false

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
        // Obtener la instancia compartida del MusicManager
        musicManager = (activity as MainActivity).getMusicManager()

        val score = arguments?.getInt(ARG_SCORE, 0) ?: 0
        val wave = arguments?.getInt(ARG_WAVE, 0) ?: 0

        // Configurar botón de guardar puntuación
        btnSaveScore = view.findViewById<Button>(R.id.btnSaveScore).apply {
            setOnClickListener {
                showSaveScoreDialog(score, wave)
            }
        }

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
        // Adicional precaución para liberar recursos
        musicManager.stopAndReleaseGameOverTheme()
    }

    override fun onDestroy() {
        super.onDestroy()
        // Asegurarse de liberar los recursos de música
        musicManager.stopAndReleaseGameOverTheme()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // Solo detener la música si el fragmento está siendo destruido completamente
        if (isRemoving) {
            musicManager.stopAndReleaseGameOverTheme()
        }
    }

    private fun showSaveScoreDialog(score: Int, wave: Int) {
        if (hasScoreSaved) {
            Toast.makeText(context, "Ya has guardado tu puntuación", Toast.LENGTH_SHORT).show()
            return
        }

        val dialog = Dialog(requireContext()).apply {
            setContentView(R.layout.dialog_save_score)
            window?.setLayout(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        }

        val etPlayerName = dialog.findViewById<EditText>(R.id.etPlayerName)

        dialog.findViewById<Button>(R.id.btnSave).setOnClickListener {
            val name = etPlayerName.text.toString().trim()

            when {
                name.isEmpty() -> {
                    etPlayerName.setError("Ingresa un nombre")
                }
                name.length > 8 -> {
                    etPlayerName.setError("Máximo 8 caracteres")
                }
                !name.matches(Regex("^[a-zA-Z0-9]*$")) -> {
                    etPlayerName.setError("Solo letras y números")
                }
                else -> {
                    saveScore(name, score, wave)
                    dialog.dismiss()
                }
            }
        }

        dialog.findViewById<Button>(R.id.btnCancel).setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun saveScore(playerName: String, score: Int, wave: Int) {
        val database = FirebaseDatabase.getInstance()
        val scoresRef = database.getReference("scores")

        val newScore = Score(
            playerName = playerName,
            score = score,
            wave = wave,
            timestamp = System.currentTimeMillis()
        )

        scoresRef.push().setValue(newScore)
            .addOnSuccessListener {
                hasScoreSaved = true
                btnSaveScore.isEnabled = false
                Toast.makeText(context, "Puntuación guardada!", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { error ->
                Toast.makeText(
                    context,
                    "Error al guardar: ${error.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
    }

}