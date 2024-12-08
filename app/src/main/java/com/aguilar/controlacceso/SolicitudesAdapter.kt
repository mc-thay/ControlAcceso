package com.aguilar.controlacceso

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class SolicitudesAdapter(
    var solicitudes: List<Solicitud>,
    private val onAceptarClick: (Solicitud) -> Unit,
    private val onRechazarClick: (Solicitud) -> Unit
) : RecyclerView.Adapter<SolicitudesAdapter.SolicitudViewHolder>() {

    inner class SolicitudViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvNombreDocente: TextView = itemView.findViewById(R.id.tvNombreDocente)
        val tvLaboratorio: TextView = itemView.findViewById(R.id.tvLaboratorio)
        val tvEstado: TextView = itemView.findViewById(R.id.tvEstado)
        val tvFechaSolicitud: TextView = itemView.findViewById(R.id.tvFechaSolicitud)
        val tvSolicitante: TextView = itemView.findViewById(R.id.tvSolicitante)
        val btnAceptar: Button = itemView.findViewById(R.id.btnAceptar)
        val btnRechazar: Button = itemView.findViewById(R.id.btnRechazar)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SolicitudViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_solicitud, parent, false)
        return SolicitudViewHolder(view)
    }

    override fun onBindViewHolder(holder: SolicitudViewHolder, position: Int) {
        val solicitud = solicitudes[position]

        holder.tvNombreDocente.text = solicitud.solicitante.nombre
        holder.tvLaboratorio.text = "Laboratorio: ${solicitud.laboratorio}" // Puedes ajustar según tus datos
        holder.tvEstado.text = solicitud.estado
        holder.tvFechaSolicitud.text = "Fecha: ${solicitud.fechaSolicitud}"
        holder.tvSolicitante.text = "Solicitante: ${solicitud.solicitante.correo}"

        // Configurar acciones de botones
        holder.btnAceptar.setOnClickListener { onAceptarClick(solicitud) }
        holder.btnRechazar.setOnClickListener { onRechazarClick(solicitud) }
    }

    override fun getItemCount(): Int = solicitudes.size
}
