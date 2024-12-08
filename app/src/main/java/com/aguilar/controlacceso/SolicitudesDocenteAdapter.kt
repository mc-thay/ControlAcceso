package com.aguilar.controlacceso

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore

class SolicitudesDocenteAdapter(
    var solicitudes: List<Solicitud> = listOf(),
    private val onSalirDelAulaClick: (solicitud: Solicitud) -> Unit
) : RecyclerView.Adapter<SolicitudesDocenteAdapter.SolicitudViewHolder>() {

    inner class SolicitudViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvIdSolicitud: TextView = view.findViewById(R.id.tvIdSolicitud)
        val tvSolicitante: TextView = view.findViewById(R.id.tvSolicitante)
        val tvFechaSolicitud: TextView = view.findViewById(R.id.tvFechaSolicitud)
        val tvEstado: TextView = view.findViewById(R.id.tvEstado)
        val btnSalir: Button = view.findViewById(R.id.btnSalir)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SolicitudViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_solicitud_vista, parent, false)

        return SolicitudViewHolder(view)
    }

    override fun onBindViewHolder(holder: SolicitudViewHolder, position: Int) {
        val solicitud = solicitudes[position]

        holder.tvIdSolicitud.text = "ID: ${solicitud.id}"
        holder.tvSolicitante.text = "Solicitante: ${solicitud.solicitante.nombre}"
        holder.tvFechaSolicitud.text = "Fecha de Solicitud: ${solicitud.fechaSolicitud}"
        holder.tvEstado.text = "Estado: ${solicitud.estado}"

        // Configurar el evento del botón
        holder.btnSalir.setOnClickListener {
            onSalirDelAulaClick(solicitud)
        }
    }

    override fun getItemCount(): Int = solicitudes.size
}
