package com.aguilar.controlacceso

data class Usuario(
    val nombre: String = "",
    val correo: String = "",
    val telefono: String = "",
    val direccion: String = "",
    val fechaNacimiento: String = "",
    val genero: String = "",
    val estadoCivil: String = "",
    val especialidades: List<String> = emptyList(),
    val añosExperiencia: Int = 0,
    val gradosAcademicos: GradosAcademicos = GradosAcademicos(),
    val idiomas: List<String> = emptyList(),
    val rol: String = "",
    val horarios: Map<String, List<Horario>>? = null // Solo si es docente
)

data class GradosAcademicos(
    val licenciatura: String = "",
    val maestria: String = "",
    val doctorado: String = ""
)

data class Horario(
    val horaInicio: String = "",
    val horaFin: String = "",
    val aula: String = ""
)
