package com.aguilar.controlacceso
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.aguilar.controlacceso.R
import com.aguilar.controlacceso.Usuario
import com.aguilar.controlacceso.Horario

class DocentesAdapter(
    private var docentes: List<Usuario>
) : RecyclerView.Adapter<DocentesAdapter.DocenteViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DocenteViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_detalle_docente, parent, false)
        return DocenteViewHolder(view)
    }

    override fun onBindViewHolder(holder: DocenteViewHolder, position: Int) {
        val docente = docentes[position]
        holder.bind(docente)
    }

    override fun getItemCount(): Int = docentes.size

    fun updateData(newDocentes: List<Usuario>) {
        this.docentes = newDocentes
        notifyDataSetChanged()
    }

    inner class DocenteViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvNombre: TextView = itemView.findViewById(R.id.tvNombre)
        private val tvCorreo: TextView = itemView.findViewById(R.id.tvCorreo)
        private val tvTelefono: TextView = itemView.findViewById(R.id.tvTelefono)
        private val tvDireccion: TextView = itemView.findViewById(R.id.tvDireccion)
        private val tvFechaNacimiento: TextView = itemView.findViewById(R.id.tvFechaNacimiento)
        private val tvGenero: TextView = itemView.findViewById(R.id.tvGenero)
        private val tvEstadoCivil: TextView = itemView.findViewById(R.id.tvEstadoCivil)
        private val tvEspecialidades: TextView = itemView.findViewById(R.id.tvEspecialidades)
        private val tvAniosExperiencia: TextView = itemView.findViewById(R.id.tvAniosExperiencia)
        private val tvGradosAcademicos: TextView = itemView.findViewById(R.id.tvGradosAcademicos)
        private val tvIdiomas: TextView = itemView.findViewById(R.id.tvIdiomas)
        private val tvRol: TextView = itemView.findViewById(R.id.tvRol)
        private val layoutHorarios: ViewGroup = itemView.findViewById(R.id.layoutHorarios)

        fun bind(docente: Usuario) {
            tvNombre.text = "Nombre: ${docente.nombre}"
            tvCorreo.text = "Correo Electrónico: ${docente.correo}"
            tvTelefono.text = "Teléfono: ${docente.telefono}"
            tvDireccion.text = "Dirección: ${docente.direccion}"
            tvFechaNacimiento.text = "Fecha de Nacimiento: ${docente.fechaNacimiento}"
            tvGenero.text = "Género: ${docente.genero}"
            tvEstadoCivil.text = "Estado Civil: ${docente.estadoCivil}"
            tvEspecialidades.text = "Especialidades: ${docente.especialidades.joinToString(", ")}"
            tvAniosExperiencia.text = "Años de Experiencia: ${docente.añosExperiencia}"

            // Mapear los grados académicos
            tvGradosAcademicos.text = buildString {
                append("Licenciatura: ${docente.gradosAcademicos.licenciatura}\n")
                append("Maestría: ${docente.gradosAcademicos.maestria}\n")
                append("Doctorado: ${docente.gradosAcademicos.doctorado}")
            }

            tvIdiomas.text = "Idiomas: ${docente.idiomas.joinToString(", ")}"
            tvRol.text = "Rol: ${docente.rol}"

            // Mapear los horarios
            layoutHorarios.removeAllViews()
            docente.horarios?.forEach { (dia, horarios) ->
                val diaTextView = TextView(itemView.context).apply {
                    text = "$dia:"
                    textSize = 16f
                    setPadding(0, 10, 0, 5)
                }
                layoutHorarios.addView(diaTextView)

                horarios.forEach { horario ->
                    val horarioTextView = TextView(itemView.context).apply {
                        text = "    ${horario.horaInicio} - ${horario.horaFin} en ${horario.aula}"
                        textSize = 14f
                        setPadding(0, 0, 0, 5)
                    }
                    layoutHorarios.addView(horarioTextView)
                }
            }
        }
    }
}
