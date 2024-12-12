package com.example.finalmoviles

import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

object FirebaseManager {
    private val database = FirebaseDatabase.getInstance()
    private val scoresRef = database.getReference("scores")

    init {
        // Habilitar persistencia offline
        database.setPersistenceEnabled(true)
        scoresRef.keepSynced(true)
    }

    fun saveScore(score: Score, onSuccess: () -> Unit, onError: (String) -> Unit) {
        scoresRef.push().setValue(score)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { error -> onError(error.message ?: "Error desconocido") }
    }

    fun getTopScores(limit: Int, onSuccess: (List<Score>) -> Unit, onError: (String) -> Unit) {
        scoresRef.orderByChild("score")
            .limitToLast(limit)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val scores = snapshot.children.mapNotNull {
                        it.getValue(Score::class.java)
                    }.sortedByDescending { it.score }
                    onSuccess(scores)
                }

                override fun onCancelled(error: DatabaseError) {
                    onError(error.message)
                }
            })
    }
}