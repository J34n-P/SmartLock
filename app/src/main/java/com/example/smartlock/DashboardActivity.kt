package com.example.smartlock

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.MenuItem
import android.widget.PopupMenu
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.smartlock.databinding.ActivityDashboardBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class DashboardActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDashboardBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseReference
    private val lockId = "cerradura_principal"
    private lateinit var sharedPref: android.content.SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance().getReference("dispositivos").child(lockId)
        sharedPref = getSharedPreferences("SmartLockPrefs", Context.MODE_PRIVATE)

        val tvUsuario = findViewById<TextView>(R.id.tvUsuario)
        tvUsuario.text = auth.currentUser?.email ?: "Usuario no identificado"

        actualizarTextoRol()

        database.child("estado").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val estado = snapshot.getValue(String::class.java) ?: "CERRADO"
                actualizarUI(estado)
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@DashboardActivity, "Error de base de datos: ${error.message}", Toast.LENGTH_LONG).show()
                actualizarUI("CERRADO")
            }
        })

        // 2. Botones de control
        binding.btnAbrir.setOnClickListener {
            database.child("estado").setValue("ABIERTO")
            registrarEvento("APERTURA")
        }

        binding.btnCerrar.setOnClickListener {
            database.child("estado").setValue("CERRADO")
            registrarEvento("CIERRE")
        }

        // 3. Lógica del Menú de Engranaje
        binding.btnMenu.setOnClickListener { view ->
            val popup = PopupMenu(this, view)
            popup.menuInflater.inflate(R.menu.menu_dashboard, popup.menu)

            popup.setOnMenuItemClickListener { item: MenuItem ->
                when (item.itemId) {
                    R.id.action_historial -> {
                        startActivity(Intent(this, HistorialActivity::class.java))
                        true
                    }
                    R.id.action_perfil -> {
                        val email = auth.currentUser?.email ?: "No logueado"
                        val rol = sharedPref.getString("rol_dispositivo", "No asignado")

                        AlertDialog.Builder(this, R.style.CustomDialogTheme)
                            .setTitle("Perfil de Usuario")
                            .setMessage("Correo: $email\n\nRol de este dispositivo: $rol")
                            .setPositiveButton("OK", null)
                            .show()
                        true
                    }
                    R.id.action_configuracion -> {
                        val opciones = arrayOf("Servidor (Maestro)", "Cliente (Invitado)")

                        AlertDialog.Builder(this, R.style.CustomDialogTheme)
                            .setTitle("Vincular Dispositivo")
                            .setItems(opciones) { _, which ->
                                val nuevoRol = if (which == 0) "Servidor" else "Cliente"
                                sharedPref.edit().putString("rol_dispositivo", nuevoRol).apply()
                                actualizarTextoRol()
                                Toast.makeText(this, "Este dispositivo ahora es: $nuevoRol", Toast.LENGTH_SHORT).show()
                            }
                            .show()
                        true
                    }
                    R.id.action_cerrar_sesion -> {
                        auth.signOut()
                        startActivity(Intent(this, LoginActivity::class.java))
                        finishAffinity()
                        true
                    }
                    else -> false
                }
            }
            popup.show()
        }
    }

    private fun actualizarTextoRol() {
        val rol = sharedPref.getString("rol_dispositivo", "No asignado")
        if (rol != "No asignado") {
            Toast.makeText(this, "Modo actual: $rol", Toast.LENGTH_SHORT).show()
        }
    }

    private fun actualizarUI(estado: String) {
        if (estado == "ABIERTO") {
            binding.tvEstado.text = "ABIERTO"
            binding.tvEstado.setTextColor(Color.parseColor("#4CAF50"))
            binding.ivLockIcon.setImageResource(R.drawable.ic_lock_open)
            binding.ivLockIcon.setColorFilter(Color.parseColor("#4CAF50"))
        } else {
            binding.tvEstado.text = "CERRADO"
            binding.tvEstado.setTextColor(Color.parseColor("#000000"))
            binding.ivLockIcon.setImageResource(R.drawable.ic_lock_closed)
            binding.ivLockIcon.setColorFilter(Color.parseColor("#000000"))
        }
    }

    private fun registrarEvento(tipoEvento: String) {
        val historialRef = FirebaseDatabase.getInstance().getReference("historial").child(lockId)
        val rol = sharedPref.getString("rol_dispositivo", "Desconocido")
        val evento = mapOf(
            "tipo" to tipoEvento,
            "usuario" to (auth.currentUser?.email ?: "Desconocido"),
            "rol" to rol,
            "timestamp" to ServerValue.TIMESTAMP
        )
        historialRef.push().setValue(evento).addOnCompleteListener { task ->
            if (!task.isSuccessful) {
                Toast.makeText(this, "Error al guardar historial: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
}