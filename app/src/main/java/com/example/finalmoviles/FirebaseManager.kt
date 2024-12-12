package com.example.finalmoviles

import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

// Objeto para gestionar la interacción con Firebase Realtime Database.
object FirebaseManager {
    // Referencia a la base de datos de Firebase.
    private val database = FirebaseDatabase.getInstance()
    // Referencia al nodo "scores" dentro de la base de datos.
    private val scoresRef = database.getReference("scores")

    init {
        // Habilitar persistencia offline para que los datos estén disponibles sin conexión.
        database.setPersistenceEnabled(true)
        // Mantener sincronizado el nodo "scores" incluso cuando esté offline.
        scoresRef.keepSynced(true)
    }

    // Método para guardar un puntaje en la base de datos.
    fun saveScore(score: Score, onSuccess: () -> Unit, onError: (String) -> Unit) {
        // Añade un nuevo puntaje al nodo "scores".
        scoresRef.push().setValue(score)
            // Callback en caso de éxito.
            .addOnSuccessListener { onSuccess() }
            // Callback en caso de error con un mensaje descriptivo.
            .addOnFailureListener { error -> onError(error.message ?: "Error desconocido") }
    }

    // Método para obtener los puntajes más altos, limitado a una cantidad específica.
    fun getTopScores(limit: Int, onSuccess: (List<Score>) -> Unit, onError: (String) -> Unit) {
        // Ordenar los puntajes por la propiedad "score" en orden ascendente.
        scoresRef.orderByChild("score")
            // Limitar el resultado a los últimos `limit` puntajes más altos.
            .limitToLast(limit)
            // Añadir un listener para obtener los datos una sola vez.
            .addListenerForSingleValueEvent(object : ValueEventListener {
                // Callback que se ejecuta cuando los datos son recibidos.
                override fun onDataChange(snapshot: DataSnapshot) {
                    // Mapear los datos recibidos a una lista de objetos `Score`.
                    val scores = snapshot.children.mapNotNull {
                        it.getValue(Score::class.java) // Convertir cada hijo en un objeto `Score`.
                    }.sortedByDescending { it.score } // Ordenar los puntajes de mayor a menor.
                    onSuccess(scores) // Pasar la lista resultante al callback de éxito.
                }

                // Callback que se ejecuta si ocurre un error al obtener los datos.
                override fun onCancelled(error: DatabaseError) {
                    onError(error.message) // Pasar el mensaje de error al callback.
                }
            })
    }
}
