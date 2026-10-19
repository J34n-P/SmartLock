package com.example.smartlock

import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import android.widget.ListView
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.*

class HistorialActivity : AppCompatActivity() {

    private lateinit var listView: ListView
    private lateinit var database: DatabaseReference
    private lateinit var adapter: HistorialAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_historial)

        val btnBack = findViewById<ImageButton>(R.id.btnBack)
        btnBack.setOnClickListener {
            finish() // Vuelve al Dashboard
        }

        listView = findViewById(R.id.lvHistorial)
        val lockId = "cerradura_principal"
        database = FirebaseDatabase.getInstance().getReference("historial").child(lockId)

        cargarHistorial()
    }

    private fun cargarHistorial() {
        database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val listaEventos = mutableListOf<EventoHistorial>()

                if (!snapshot.exists()) {
                    listaEventos.add(EventoHistorial("--/--/----", "No hay eventos", "Realiza una acción en el Dashboard", ""))
                } else {
                    val children = snapshot.children.toList().reversed()

                    for (evento in children) {
                        val tipo = evento.child("tipo").getValue(String::class.java) ?: "Desconocido"
                        val usuario = evento.child("usuario").getValue(String::class.java) ?: "Anónimo"
                        val rol = evento.child("rol").getValue(String::class.java) ?: "Desconocido"
                        val timestamp = evento.child("timestamp").getValue(Long::class.java) ?: 0L

                        val fecha = java.text.SimpleDateFormat("dd/MM/yyyy HH:mm", java.util.Locale.getDefault())
                            .format(java.util.Date(timestamp))

                        listaEventos.add(EventoHistorial(fecha, tipo, usuario, rol))
                    }
                }

                adapter = HistorialAdapter(this@HistorialActivity, listaEventos)
                listView.adapter = adapter
            }

            override fun onCancelled(error: DatabaseError) {
            }
        })
    }
}