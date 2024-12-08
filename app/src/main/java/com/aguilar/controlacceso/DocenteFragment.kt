package com.aguilar.controlacceso

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.aguilar.controlacceso.databinding.FragmentDocenteBinding
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class DocenteFragment : Fragment(R.layout.fragment_docente) {

    private var _binding: FragmentDocenteBinding? = null
    private val binding get() = _binding!!

    private val firestore by lazy { FirebaseFirestore.getInstance() }
    private val auth by lazy { FirebaseAuth.getInstance() }

    private lateinit var solicitudesAdapter: SolicitudesDocenteAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        _binding = FragmentDocenteBinding.bind(view)

        // Configurar Adapter para la lista de solicitudes
        solicitudesAdapter = SolicitudesDocenteAdapter(
            solicitudes = listOf(),
            onSalirDelAulaClick = { solicitud ->
                mostrarDialogoSalidaDelAula(solicitud)
            }
        )

        binding.recyclerViewDocentes.apply {
            adapter = solicitudesAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }

        // Cargar la lista de solicitudes
        cargarSolicitudesDocente()

        // Configurar el botón "Ingresar al aula"
        binding.btnIngresarAula.setOnClickListener {
            ingresarAlAula()
        }
    }

    private fun ingresarAlAula() {
        val usuarioActual = auth.currentUser
        if (usuarioActual != null) {
            Log.d("DEBUG", "DisplayName: ${usuarioActual.displayName}") // Observa el nombre aquí
            val fechaActual = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(
                Date()
            )

            // Crear la solicitud con los datos correctos
            val solicitud = Solicitud(
                id = "", // Firebase asignará el ID automáticamente
                solicitante = Usuario(
                    nombre = usuarioActual.displayName ?: "Usuario desconocido",
                    correo = usuarioActual.email ?: ""
                ),
                fechaSolicitud = fechaActual,
                estado = "PENDIENTE"
            )

            firestore.collection("solicitudes")
                .add(solicitud.toMap()) // Convertir el objeto Solicitud a un mapa para Firestore
                .addOnSuccessListener {
                    Toast.makeText(requireContext(), "Solicitud enviada para ingresar al aula", Toast.LENGTH_SHORT).show()
                    cargarSolicitudesDocente()
                }
                .addOnFailureListener { exception ->
                    Log.e("Firestore", "Error al enviar solicitud", exception)
                    Toast.makeText(requireContext(), "Error al enviar solicitud", Toast.LENGTH_SHORT).show()
                }
        }
    }


    // Función para mapear el objeto Solicitud a un Map compatible con Firestore
    fun Solicitud.toMap(): Map<String, Any> {
        return mapOf(
            "solicitante" to mapOf(
                "nombre" to solicitante.nombre,
                "correo" to solicitante.correo
            ),
            "fechaSolicitud" to fechaSolicitud,
            "estado" to estado
        )
    }


    private fun mostrarDialogoSalidaDelAula(solicitud: Solicitud) {
        AlertDialog.Builder(requireContext())
            .setTitle("Confirmación")
            .setMessage("¿Estás seguro de que deseas salir del aula?")
            .setPositiveButton("Sí") { dialog, _ ->
                verificarYActualizarEstado(solicitud)
                dialog.dismiss()
            }
            .setNegativeButton("No") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    private fun verificarYActualizarEstado(solicitud: Solicitud) {
        if (solicitud.estado == "ACEPTADA") {
            firestore.collection("solicitudes").document(solicitud.id).update("estado", "TERMINADA")
                .addOnSuccessListener {
                    Toast.makeText(requireContext(), "Has salido del aula", Toast.LENGTH_SHORT).show()
                    cargarSolicitudesDocente()
                }
                .addOnFailureListener { exception ->
                    Toast.makeText(requireContext(), "Error al salir del aula", Toast.LENGTH_SHORT).show()
                    Log.e("Firestore", "Error al actualizar estado", exception)
                }
        } else {
            Toast.makeText(requireContext(), "No se tuvo acceso al aula", Toast.LENGTH_SHORT).show()
        }
    }

    private fun cargarSolicitudesDocente() {
        val usuarioActual = auth.currentUser

        if (usuarioActual != null) {
            firestore.collection("solicitudes")
                .whereEqualTo("solicitante.correo", usuarioActual.email)
                .get()
                .addOnSuccessListener { snapshot ->
                    val solicitudes = snapshot.documents.mapNotNull { doc ->
                        doc.toObject(Solicitud::class.java)?.copy(id = doc.id)
                    }

                    solicitudesAdapter.solicitudes = solicitudes
                    solicitudesAdapter.notifyDataSetChanged()
                }
                .addOnFailureListener { exception ->
                    Log.e("Firestore", "Error al cargar solicitudes", exception)
                }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
