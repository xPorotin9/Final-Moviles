package com.example.finalmoviles

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class ScoresAdapter : RecyclerView.Adapter<ScoresAdapter.ScoreViewHolder>() {
    private var scores = listOf<Score>()

    class ScoreViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvRank: TextView = view.findViewById(R.id.tvRank)
        val tvName: TextView = view.findViewById(R.id.tvName)
        val tvScore: TextView = view.findViewById(R.id.tvScore)
        val tvWave: TextView = view.findViewById(R.id.tvWave)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ScoreViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_score, parent, false)
        return ScoreViewHolder(view)
    }

    override fun onBindViewHolder(holder: ScoreViewHolder, position: Int) {
        val score = scores[position]
        holder.apply {
            tvRank.text = "${position + 1}"
            tvName.text = score.playerName
            tvScore.text = "${score.score}"
            tvWave.text = "Oleada ${score.wave}"
        }
    }

    override fun getItemCount() = scores.size

    fun updateScores(newScores: List<Score>) {
        scores = newScores
        notifyDataSetChanged()
    }
}