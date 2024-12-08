package com.aguilar.controlacceso

data class Solicitud(
    val id: String = "", // ID generado automáticamente
    val solicitante: Usuario = Usuario(),
    val fechaSolicitud: String = "",
    val estado: String = "PENDIENTE" // Valores posibles: pendiente, aprobada, rechazada
)
