package com.aguilar.controlacceso

data class Reporte(
    val id: String = "",
    val docente: String = "",
    val laboratorio: String = "",
    val curso: String = "",
    val fecha: String = "",
    val horaEntrada: String = "",
    val horaSalida: String = "",
    val estado: String = "completado" // Valores: completado, en curso
)
