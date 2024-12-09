package com.aguilar.controlacceso
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.aguilar.controlacceso.R
import com.aguilar.controlacceso.Usuario
import com.google.firebase.firestore.FirebaseFirestore

class AdminInfoDocentesFragment : Fragment(R.layout.fragment_admin_info_docentes) {

    private val firestore by lazy { FirebaseFirestore.getInstance() }
    private lateinit var docentesAdapter: DocentesAdapter
    private lateinit var recyclerViewDocentes: RecyclerView

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recyclerViewDocentes = view.findViewById(R.id.recyclerViewDocentes)
        docentesAdapter = DocentesAdapter(listOf())
        recyclerViewDocentes.apply {
            adapter = docentesAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }

        cargarDocentes()
    }

    private fun cargarDocentes() {
        firestore.collection("usuarios")
            .whereEqualTo("rol", "docente")
            .get()
            .addOnSuccessListener { snapshot ->
                val docentes = snapshot.documents.mapNotNull { doc ->
                    doc.toObject(Usuario::class.java)
                }
                docentesAdapter.updateData(docentes)
            }
            .addOnFailureListener { exception ->
                Log.e("Firestore", "Error al cargar docentes", exception)
                Toast.makeText(requireContext(), "Error al cargar docentes", Toast.LENGTH_SHORT).show()
            }
    }
}
