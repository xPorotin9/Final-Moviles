package com.example.finalmoviles

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

// Adaptador para manejar la lista de puntajes en un RecyclerView.
class ScoresAdapter : RecyclerView.Adapter<ScoresAdapter.ScoreViewHolder>() {
    // Lista de puntajes que se mostrará en el RecyclerView.
    private var scores = listOf<Score>()

    // ViewHolder: representa cada elemento individual de la lista.
    class ScoreViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        // TextView para mostrar el rango del jugador.
        val tvRank: TextView = view.findViewById(R.id.tvRank)
        // TextView para mostrar el nombre del jugador.
        val tvName: TextView = view.findViewById(R.id.tvName)
        // TextView para mostrar el puntaje del jugador.
        val tvScore: TextView = view.findViewById(R.id.tvScore)
        // TextView para mostrar la oleada alcanzada por el jugador.
        val tvWave: TextView = view.findViewById(R.id.tvWave)
    }

    // Infla la vista de cada elemento de la lista desde el archivo XML `item_score`.
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ScoreViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_score, parent, false) // Crea la vista del elemento.
        return ScoreViewHolder(view) // Retorna el ViewHolder asociado a la vista.
    }

    // Vincula los datos de la lista al ViewHolder en una posición específica.
    override fun onBindViewHolder(holder: ScoreViewHolder, position: Int) {
        val score = scores[position] // Obtiene el puntaje actual de la lista.
        holder.apply {
            tvRank.text = "${position + 1}" // Asigna el rango basado en la posición.
            tvName.text = score.playerName // Muestra el nombre del jugador.
            tvScore.text = "${score.score}" // Muestra el puntaje del jugador.
            tvWave.text = "Oleada ${score.wave}" // Muestra la oleada alcanzada.
        }
    }

    // Retorna el tamaño de la lista de puntajes.
    override fun getItemCount() = scores.size

    // Actualiza la lista de puntajes y notifica al RecyclerView para refrescar los datos.
    fun updateScores(newScores: List<Score>) {
        scores = newScores // Actualiza la lista de puntajes.
        notifyDataSetChanged() // Notifica cambios en los datos para refrescar la vista.
    }
}
