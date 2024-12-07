package com.aguilar.controlacceso

data class Solicitud(
    val id: String = "", // ID generado automáticamente
    val solicitante: Usuario = Usuario(),
    val fechaSolicitud: String = "",
    val descripcion: String = "",
    val estado: String = "pendiente" // Valores posibles: pendiente, aprobada, rechazada
)
