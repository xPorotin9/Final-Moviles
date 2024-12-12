package com.example.finalmoviles

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class ScoresFragment : Fragment() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: ScoresAdapter
    private lateinit var btnPrevious: Button
    private lateinit var btnNext: Button

    private val scoresPerPage = 15
    private var currentPage = 0
    private var allScores = listOf<Score>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_scores, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recyclerView = view.findViewById(R.id.rvScores)
        btnPrevious = view.findViewById(R.id.btnPrevious)
        btnNext = view.findViewById(R.id.btnNext)

        adapter = ScoresAdapter()
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(context)

        loadScores()
        setupPagination()
    }

    private fun loadScores() {
        val database = FirebaseDatabase.getInstance()
        val scoresRef = database.getReference("scores")

        scoresRef.orderByChild("score").limitToLast(100).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                allScores = snapshot.children.mapNotNull {
                    it.getValue(Score::class.java)
                }.sortedByDescending { it.score }
                updatePageContent()
            }

            override fun onCancelled(error: DatabaseError) {
                activity?.runOnUiThread {
                    Toast.makeText(
                        context,
                        "Error al cargar puntuaciones: ${error.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        })
    }

    private fun updatePageContent() {
        val startIndex = currentPage * scoresPerPage
        val endIndex = minOf(startIndex + scoresPerPage, allScores.size)

        if (startIndex < allScores.size) {
            adapter.updateScores(allScores.subList(startIndex, endIndex))
        }

        btnPrevious.isEnabled = currentPage > 0
        btnNext.isEnabled = endIndex < allScores.size
    }

    private fun setupPagination() {
        btnPrevious.setOnClickListener {
            if (currentPage > 0) {
                currentPage--
                updatePageContent()
            }
        }

        btnNext.setOnClickListener {
            if ((currentPage + 1) * scoresPerPage < allScores.size) {
                currentPage++
                updatePageContent()
            }
        }
    }

    companion object {
        fun newInstance() = ScoresFragment()
    }
}