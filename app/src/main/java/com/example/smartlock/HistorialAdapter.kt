package com.example.smartlock

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView

class HistorialAdapter(context: Context, private val eventos: List<EventoHistorial>) :
    ArrayAdapter<EventoHistorial>(context, 0, eventos) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = convertView ?: LayoutInflater.from(context).inflate(R.layout.item_historial, parent, false)

        val evento = eventos[position]

        val tvFecha = view.findViewById<TextView>(R.id.tvFecha)
        val tvAccion = view.findViewById<TextView>(R.id.tvAccion)
        val tvUsuario = view.findViewById<TextView>(R.id.tvUsuario)
        val tvRol = view.findViewById<TextView>(R.id.tvRol)

        tvFecha.text = evento.fecha
        tvAccion.text = evento.accion
        tvUsuario.text = evento.usuario
        tvRol.text = "(${evento.rol})"

        if (evento.rol == "Servidor") {
            tvRol.setTextColor(context.getColor(android.R.color.holo_green_dark))
        } else {
            tvRol.setTextColor(context.getColor(android.R.color.holo_blue_dark))
        }

        return view
    }
}

data class EventoHistorial(
    val fecha: String,
    val accion: String,
    val usuario: String,
    val rol: String
)