package com.example.socialmaps

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.firebase.firestore.FirebaseFirestore

class EditarPerfil : Fragment() {

    private val db = FirebaseFirestore.getInstance()
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View?{

        val prefs = this.requireActivity().getSharedPreferences(getString(R.string.prefs_file),
            AppCompatActivity.MODE_PRIVATE
        )
        val email = prefs.getString("email", null)
        val componentFragment = inflater.inflate(R.layout.fragment_editar_perfil, container, false)
        val nombre = componentFragment.findViewById<EditText>(R.id.editarNombre).text
        val apellido = componentFragment.findViewById<EditText>(R.id.editarApellido).text
        val btnGuardar = componentFragment.findViewById<Button>(R.id.btnGuardar)

        btnGuardar.setOnClickListener{
            if (email != null && nombre.toString().isNotEmpty() && apellido.toString().isNotEmpty()) {
                db.collection("usuarios").document(email).set(
                    hashMapOf("nombre" to nombre.toString(), "apellido" to apellido.toString()))
                val prefs = this.requireActivity().getSharedPreferences(getString(R.string.prefs_file),
                    AppCompatActivity.MODE_PRIVATE
                ).edit()
                prefs.putString("name", nombre.toString() + " " + apellido.toString())
                prefs.apply()
                //Toast.makeText(context,nombre.toString(), Toast.LENGTH_SHORT).show()
                requireActivity().supportFragmentManager.beginTransaction().apply {
                    replace(R.id.fragmentContenedor, MapFragment())
                    commit()
                }
            }else{
                Toast.makeText(context,"Campos vacios", Toast.LENGTH_SHORT).show()
            }
        }
        return componentFragment
    }
}
