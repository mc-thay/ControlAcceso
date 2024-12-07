package com.aguilar.controlacceso

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import com.aguilar.controlacceso.databinding.FragmentAdminBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader

class AdminFragment : Fragment(R.layout.fragment_admin) {
    private val firestore by lazy { FirebaseFirestore.getInstance() }
    private val auth by lazy { FirebaseAuth.getInstance() }
    private var _binding: FragmentAdminBinding? = null
    private val binding get() = _binding!!

    private var docenteEncontrado: JSONObject? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentAdminBinding.bind(view)

        requireActivity().title = "Gestión Docentes"

        binding.btnAgregarDocente.setOnClickListener {
            buscarDocenteEnJSON()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun buscarDocenteEnJSON() {
        try {
            val inputStream = resources.openRawResource(R.raw.usuarios)
            val jsonString = BufferedReader(InputStreamReader(inputStream)).use { it.readText() }
            val jsonObject = JSONObject(jsonString)

            if (!jsonObject.has("usuarios")) {
                Log.e("JSON", "La clave 'usuarios' no existe en el JSON")
                Toast.makeText(requireContext(), "No se encontraron usuarios en el archivo JSON", Toast.LENGTH_SHORT).show()
                return
            }

            val usuarios = jsonObject.getJSONObject("usuarios")
            val correoBuscado = binding.etBuscarCorreo.text.toString().trim()

            if (correoBuscado.isEmpty()) {
                Toast.makeText(requireContext(), "Por favor, ingrese un correo para buscar", Toast.LENGTH_SHORT).show()
                return
            }

            // Iterar sobre los usuarios para buscar el correo
            var docenteEncontrado: JSONObject? = null
            usuarios.keys().forEach { key ->
                val usuario = usuarios.getJSONObject(key)
                if (usuario.getString("correo") == correoBuscado) {
                    docenteEncontrado = usuario
                    return@forEach
                }
            }

            if (docenteEncontrado != null) {
                if (docenteEncontrado!!.has("rol") && docenteEncontrado!!.getString("rol") == "docente") {
                    mostrarDialogoConfirmacion(docenteEncontrado!!)
                } else {
                    Toast.makeText(requireContext(), "El correo no corresponde a un docente válido", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(requireContext(), "No se encontró el docente con ese correo", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            Log.e("JSON", "Error al procesar el JSON", e)
            Toast.makeText(requireContext(), "Error al procesar el archivo JSON", Toast.LENGTH_SHORT).show()
        }
    }


    private fun mostrarDialogoConfirmacion(docente: JSONObject) {
        AlertDialog.Builder(requireContext()).apply {
            setTitle("Confirmar Acción")
            setMessage("¿Desea agregar al docente '${docente.getString("nombre")}' a la base de datos y autenticarlo?")
            setPositiveButton("Sí") { _, _ -> subirDocente(docente) }
            setNegativeButton("No") { dialog, _ -> dialog.dismiss() }
            create()
            show()
        }
    }

    private fun subirDocente(docente: JSONObject) {
        val correo = docente.getString("correo")
        val contraseña = docente.getString("nombre").replace(" ", "_") // Reemplaza espacios por "_"

        // Registrar al usuario en Firebase Authentication
        registrarUsuarioEnAuth(correo, contraseña,
            onSuccess = {
                // Si se registra correctamente, agregar a Firestore
                val gradosAcademicos = mapOf(
                    "licenciatura" to docente.optString("licenciatura", "No disponible"),
                    "maestria" to docente.optString("maestria", "No disponible"),
                    "doctorado" to docente.optString("doctorado", "No disponible")
                )

                val datosDocente = hashMapOf(
                    "nombre" to docente.getString("nombre"),
                    "correo" to correo,
                    "telefono" to docente.optString("telefono", "No disponible"),
                    "direccion" to docente.optString("direccion", "No disponible"),
                    "fechaNacimiento" to docente.optString("fechaNacimiento", "No disponible"),
                    "genero" to docente.optString("genero", "No disponible"),
                    "estadoCivil" to docente.optString("estadoCivil", "No disponible"),
                    "especialidades" to parseListaEspecialidades(docente),
                    "añosExperiencia" to docente.optInt("añosExperiencia", 0),
                    "gradosAcademicos" to gradosAcademicos,
                    "idiomas" to parseListaIdiomas(docente),
                    "rol" to docente.optString("rol", "No disponible"),
                    "horarios" to parseHorarios(docente.optJSONObject("horarios"))
                )

                firestore.collection("usuarios")
                    .document(correo)
                    .set(datosDocente)
                    .addOnSuccessListener {
                        Log.d("Firestore", "Docente agregado correctamente: $correo")
                        Toast.makeText(requireContext(), "Docente subido a Firestore correctamente", Toast.LENGTH_SHORT).show()
                    }
                    .addOnFailureListener { e ->
                        Log.e("Firestore", "Error al agregar docente", e)
                        Toast.makeText(requireContext(), "Error al subir el docente", Toast.LENGTH_SHORT).show()
                    }
            },
            onFailure = { exception ->
                Toast.makeText(requireContext(), "Error al registrar el usuario: ${exception.message}", Toast.LENGTH_SHORT).show()
            }
        )
    }


    private fun parseListaEspecialidades(docente: JSONObject): List<String> {
        return docente.optJSONArray("especialidades")?.let {
            (0 until it.length()).map { i -> it.getString(i) }
        } ?: listOf("No especificadas")
    }

    private fun parseListaIdiomas(docente: JSONObject): List<String> {
        return docente.optJSONArray("idiomas")?.let {
            (0 until it.length()).map { i -> it.getString(i) }
        } ?: listOf("No disponible")
    }
    private fun registrarUsuarioEnAuth(correo: String, contraseña: String, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
        auth.createUserWithEmailAndPassword(correo, contraseña)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.d("Auth", "Usuario registrado correctamente: $correo")
                    onSuccess()
                } else {
                    val exception = task.exception
                    Log.e("Auth", "Error al registrar usuario: $correo", exception)
                    onFailure(exception ?: Exception("Error desconocido"))
                }
            }
    }

    private fun parseHorarios(jsonHorarios: JSONObject?): Map<String, List<Map<String, String>>> {
        val horariosMap = mutableMapOf<String, List<Map<String, String>>>()
        if (jsonHorarios != null) {
            for (day in jsonHorarios.keys()) {
                val horas = jsonHorarios.getJSONArray(day)
                val listaHorarios = mutableListOf<Map<String, String>>()
                for (i in 0 until horas.length()) {
                    val horario = horas.getJSONObject(i)
                    val horarioMap = mapOf(
                        "horaInicio" to horario.optString("horaInicio", "No disponible"),
                        "horaFin" to horario.optString("horaFin", "No disponible"),
                        "aula" to horario.optString("aula", "No disponible")
                    )
                    listaHorarios.add(horarioMap)
                }
                horariosMap[day] = listaHorarios
            }
        }
        return horariosMap
    }
}
