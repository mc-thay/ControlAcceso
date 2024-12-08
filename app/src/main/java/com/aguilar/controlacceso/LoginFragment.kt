package com.aguilar.controlacceso

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class LoginFragment : Fragment() {

    private lateinit var etEmail: EditText
    private lateinit var etPassword: EditText
    private lateinit var btnLogin: Button
    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance() // Referencia a Firestore

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_login, container, false)

        // Mapear elementos
        etEmail = view.findViewById(R.id.etEmail)
        etPassword = view.findViewById(R.id.etPassword)
        btnLogin = view.findViewById(R.id.btnLogin)

        btnLogin.setOnClickListener {
            realizarLogin()
        }

        return view
    }

    private fun realizarLogin() {
        val email = etEmail.text.toString().trim()
        val password = etPassword.text.toString().trim()

        // Validar que los campos no estén vacíos
        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(requireContext(), "Por favor, completa todos los campos", Toast.LENGTH_SHORT).show()
            return
        }

        // Intentar autenticar con FirebaseAuth
        autenticarUsuario(email, password)
    }

    private fun autenticarUsuario(email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // Autenticación exitosa, verificar el rol en Firestore
                    verificarRolEnFirestore(email)
                } else {
                    Toast.makeText(requireContext(), "Error al autenticar las credenciales", Toast.LENGTH_SHORT).show()
                    task.exception?.printStackTrace()
                }
            }
    }

    private fun verificarRolEnFirestore(email: String) {
        firestore.collection("usuarios").document(email).get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    val rol = document.getString("rol")
                    if (rol == "admin") {
                        // Redireccionar al fragmento de Admin
                        findNavController().navigate(R.id.action_loginFragment_to_adminFragment)
                    } else if (rol == "docente") {
                        // Redireccionar al fragmento de Docente
                        findNavController().navigate(R.id.action_loginFragment_to_docenteFragment)
                    } else {
                        Toast.makeText(requireContext(), "Rol desconocido", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(requireContext(), "No se encontró información de usuario en la base de datos", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { exception ->
                Toast.makeText(requireContext(), "Error al obtener información del usuario", Toast.LENGTH_SHORT).show()
                exception.printStackTrace()
            }
    }
}
