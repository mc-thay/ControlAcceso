package com.aguilar.controlacceso

data class Solicitud(
    val id: String = "", // ID generado automáticamente
    val solicitante: Usuario = Usuario(),
    val fechaSolicitud: String = "",
    val estado: String = "PENDIENTE", // Valores posibles: pendiente, aprobada, rechazada
    val laboratorio: String = "", // Nombre del laboratorio asignado aleatoriamente
    val horaSalida: String? = null // Hora de salida, puede ser nula si aún no se ha asignado
)
