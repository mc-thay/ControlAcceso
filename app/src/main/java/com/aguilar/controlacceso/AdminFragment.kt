package com.aguilar.controlacceso

import android.os.Bundle
import android.os.Environment
import android.util.Log

import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.aguilar.controlacceso.databinding.FragmentAdminBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.itextpdf.text.Paragraph
import com.itextpdf.text.DocumentException
import com.itextpdf.text.pdf.PdfWriter
import org.json.JSONObject
import com.itextpdf.text.Document
import java.io.BufferedReader
import com.itextpdf.text.pdf.PdfPTable
import java.io.File
import java.io.FileOutputStream
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
    private fun aceptarSolicitud(solicitud: Solicitud) {
        firestore.collection("solicitudes").document(solicitud.id)
            .update("estado", "APROBADA")
            .addOnSuccessListener {
                Toast.makeText(requireContext(), "Solicitud aceptada", Toast.LENGTH_SHORT).show()
                // Remover la solicitud de la lista y actualizar el adaptador
                eliminarSolicitudDeLista(solicitud)
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Error al aceptar solicitud", Toast.LENGTH_SHORT).show()
            }
    }
    private fun eliminarSolicitudDeLista(solicitud: Solicitud) {
        val adapter = (binding.rvSolicitudes.adapter as SolicitudesAdapter)
        adapter.solicitudes = adapter.solicitudes.filter { it.id != solicitud.id }
        adapter.notifyDataSetChanged()
    }

    private fun rechazarSolicitud(solicitud: Solicitud) {
        firestore.collection("solicitudes").document(solicitud.id)
            .update("estado", "RECHAZADA")
            .addOnSuccessListener {
                Toast.makeText(requireContext(), "Solicitud rechazada", Toast.LENGTH_SHORT).show()
                // Remover la solicitud de la lista y actualizar el adaptador
                eliminarSolicitudDeLista(solicitud)
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Error al rechazar solicitud", Toast.LENGTH_SHORT).show()
            }
    }

    private fun cargarSolicitudes(adapter: SolicitudesAdapter) {
        firestore.collection("solicitudes")
            .whereEqualTo("estado", "PENDIENTE") // Filtrar solo solicitudes pendientes
            .get()
            .addOnSuccessListener { snapshot ->
                val solicitudes = snapshot.documents.mapNotNull { doc ->
                    doc.toObject(Solicitud::class.java)?.copy(id = doc.id)
                }

                // Actualizar el adaptador con los datos filtrados
                adapter.apply {
                    this.solicitudes = solicitudes
                    notifyDataSetChanged()
                }
            }
            .addOnFailureListener { exception ->
                Log.e("Firestore", "Error al cargar solicitudes", exception)
                Toast.makeText(requireContext(), "Error al cargar solicitudes", Toast.LENGTH_SHORT).show()
            }
    }

    private fun mostrarDialogoDescarga() {
        AlertDialog.Builder(requireContext()).apply {
            setTitle("Descargar Registro")
            setMessage("¿Desea descargar el registro de solicitudes en PDF?")
            setPositiveButton("Sí") { _, _ ->
                generarPDF()
            }
            setNegativeButton("No") { dialog, _ ->
                dialog.dismiss()
            }
            create()
            show()
        }
    }

    private fun generarPDF() {
        firestore.collection("solicitudes")
            .get()
            .addOnSuccessListener { snapshot ->
                val solicitudes = snapshot.documents.mapNotNull { doc ->
                    doc.toObject(Solicitud::class.java)?.copy(id = doc.id)
                }

                if (solicitudes.isEmpty()) {
                    Toast.makeText(requireContext(), "No hay solicitudes para descargar", Toast.LENGTH_SHORT).show()
                    return@addOnSuccessListener
                }

                try {
                    val pdfFile = crearPDF(solicitudes)
                    Toast.makeText(requireContext(), "PDF generado: ${pdfFile.absolutePath}", Toast.LENGTH_LONG).show()
                } catch (e: Exception) {
                    Toast.makeText(requireContext(), "Error al crear el PDF: ${e.message}", Toast.LENGTH_SHORT).show()
                    Log.e("PDF", "Error al crear el PDF", e)
                }
            }
            .addOnFailureListener { e ->
                Log.e("Firestore", "Error al obtener solicitudes", e)
                Toast.makeText(requireContext(), "Error al obtener solicitudes", Toast.LENGTH_SHORT).show()
            }
    }

    private fun crearPDF(solicitudes: List<Solicitud>): File {
        val document = Document()

        // Carpeta de Descargas
        val downloadDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)

        // Archivo PDF en la carpeta de Descargas
        val pdfFile = File(downloadDir, "registro_solicitudes.pdf")

        try {
            PdfWriter.getInstance(document, FileOutputStream(pdfFile))
            document.open()

            // Título
            document.add(Paragraph("Registro de Solicitudes\n\n"))

            // Crear tabla con columnas
            val tabla = PdfPTable(6) // Seis columnas: ID, Solicitante, Fecha, Estado, Laboratorio, Hora de Salida
            tabla.addCell("ID")
            tabla.addCell("Solicitante")
            tabla.addCell("Fecha")
            tabla.addCell("Estado")
            tabla.addCell("Laboratorio")
            tabla.addCell("Hora de Salida")

            solicitudes.forEach { solicitud ->
                tabla.addCell(solicitud.id)
                tabla.addCell(solicitud.solicitante.nombre)
                tabla.addCell(solicitud.fechaSolicitud)
                tabla.addCell(solicitud.estado)
                tabla.addCell(solicitud.laboratorio ?: "No disponible") // Nuevo campo
                tabla.addCell(solicitud.horaSalida ?: "No disponible") // Nuevo campo
            }

            document.add(tabla)
            document.add(Paragraph("\n\nGenerado el ${System.currentTimeMillis()}"))

            document.close()

            Log.d("PDF", "PDF guardado en: ${pdfFile.absolutePath}")
            return pdfFile
        } catch (e: DocumentException) {
            Log.e("PDF", "Error al generar el documento", e)
            throw e
        } catch (e: Exception) {
            Log.e("PDF", "Error al guardar el archivo", e)
            throw e
        }
    }
    private fun mostrarDialogoSalir() {
        AlertDialog.Builder(requireContext())
            .setTitle("Salir de la aplicación")
            .setMessage("¿Estás seguro de que deseas salir?")
            .setPositiveButton("Sí") { _, _ ->
                requireActivity().finish() // Finalizar la actividad actual
            }
            .setNegativeButton("No") { dialog, _ ->
                dialog.dismiss() // Cerrar el diálogo
            }
            .show()
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentAdminBinding.bind(view)
        binding.btnDownloadRegister.setOnClickListener {
            mostrarDialogoDescarga()
        }
        // Configurar el comportamiento del botón de retroceso
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                mostrarDialogoSalir()
            }
        })
        // Encuentra el botón por su ID
        val btnVerInfoDocente = view.findViewById<Button>(R.id.btnVerInfoDocentes)

        // Configura el listener para el botón
        btnVerInfoDocente.setOnClickListener {
            // Navega al fragmento AdminInfoDocentesFragment
            findNavController().navigate(R.id.action_adminFragment_to_adminInfoDocentes)
        }
// Configurar RecyclerView
        val solicitudesAdapter = SolicitudesAdapter(
            solicitudes = listOf(), // Lista inicial vacía
            onAceptarClick = { solicitud -> aceptarSolicitud(solicitud) },
            onRechazarClick = { solicitud -> rechazarSolicitud(solicitud) }
        )

        binding.rvSolicitudes.apply {
            adapter = solicitudesAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }


        // Cargar solicitudes desde Firestore
        cargarSolicitudes(solicitudesAdapter)
        requireActivity().title = "Gestión Docentes"

        binding.btnAgregarDocente.setOnClickListener {
            buscarDocenteEnJSON()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
// para cargar usuarios a firestore/////////////////////////////////////
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
