package com.example.finalmoviles

// Importaciones necesarias para el manejo de vistas, fragmentos, RecyclerView y Firebase.
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

// Fragmento que muestra una lista de puntuaciones con paginación.
class ScoresFragment : Fragment() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: ScoresAdapter
    private lateinit var btnPrevious: Button
    private lateinit var btnNext: Button

    // Configuración de variables para paginación.
    private val scoresPerPage = 15 
    private var currentPage = 0 
    private var allScores = listOf<Score>() 

    // Método para inflar el layout del fragmento.
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Retorna el layout asociado al fragmento.
        return inflater.inflate(R.layout.fragment_scores, container, false)
    }

    // Método que se ejecuta cuando la vista ha sido creada.
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recyclerView = view.findViewById(R.id.rvScores) // RecyclerView para mostrar los puntajes.
        btnPrevious = view.findViewById(R.id.btnPrevious) // Botón para ir a la página anterior.
        btnNext = view.findViewById(R.id.btnNext) // Botón para ir a la página siguiente.

        adapter = ScoresAdapter() // Creación del adaptador para manejar los datos.
        recyclerView.adapter = adapter // Asociar el adaptador al RecyclerView.
        recyclerView.layoutManager = LinearLayoutManager(context) // Configuración de layout vertical.

        // Cargar los puntajes desde Firebase y configurar los botones de paginación.
        loadScores()
        setupPagination()
    }

    // Método para cargar los puntajes desde Firebase.
    private fun loadScores() {
        val database = FirebaseDatabase.getInstance() // Referencia a la base de datos de Firebase.
        val scoresRef = database.getReference("scores") // Referencia al nodo "scores".

        // Obtener los 100 puntajes más altos, ordenados por la propiedad "score".
        scoresRef.orderByChild("score").limitToLast(100).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                // Convertir los datos recibidos en una lista de objetos `Score`.
                allScores = snapshot.children.mapNotNull {
                    it.getValue(Score::class.java) // Transformar cada hijo en un objeto `Score`.
                }.sortedByDescending { it.score } // Ordenar la lista de mayor a menor puntaje.
                updatePageContent() // Actualizar la página actual con los nuevos datos.
            }

            override fun onCancelled(error: DatabaseError) {
                // Mostrar un mensaje de error al usuario si la carga falla.
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

    // Método para actualizar el contenido mostrado en la página actual.
    private fun updatePageContent() {
        val startIndex = currentPage * scoresPerPage // Índice inicial de la página.
        val endIndex = minOf(startIndex + scoresPerPage, allScores.size) // Índice final de la página.

        // Si hay datos dentro del rango actual, actualizar el adaptador.
        if (startIndex < allScores.size) {
            adapter.updateScores(allScores.subList(startIndex, endIndex)) // Sublista de la página actual.
        }

        // Habilitar o deshabilitar los botones según la página actual.
        btnPrevious.isEnabled = currentPage > 0 // Deshabilitar "Anterior" si es la primera página.
        btnNext.isEnabled = endIndex < allScores.size // Deshabilitar "Siguiente" si es la última página.
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
