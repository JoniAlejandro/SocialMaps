package com.example.socialmaps

import android.app.Activity.RESULT_OK
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.socialmaps.models.Notificaciones
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage


class MapFragment : Fragment() {
    private val db = FirebaseFirestore.getInstance()
    val PICK_IMAGE_REQUEST = 1
    lateinit var uri: Uri
    private lateinit var imagen: ImageView
    val storageRef = FirebaseStorage.getInstance().reference
    val imagesRef = storageRef.child("images")

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val componentFragment = inflater.inflate(R.layout.fragment_map, container, false)
        // Crear una instancia del Fragmento anidado
        val anidadoFragment = MapaFragment()
        val chatFragment = ChatFragment()
        // Agregar el Fragmento anidado al contenedor en el layout XML del Fragmento contenedor
        childFragmentManager.beginTransaction()
            .add(R.id.contenedor_anidado, anidadoFragment)
            .commit()
        childFragmentManager.beginTransaction()
            .add(R.id.chat_anidado, chatFragment)
            .commit()
        val btnAgregar = componentFragment.findViewById<FloatingActionButton>(R.id.btnAgregar)
        btnAgregar.setOnClickListener {
            //\onDestroy()
            if (activity is Principal) {
                val mainActivity = activity as Principal
                val latitude = mainActivity.receivedData1
                val longitude = mainActivity.receivedData2
                if (latitude == null && longitude == null) {
                    Toast.makeText(context, "Ubicacion no seleccionada", Toast.LENGTH_SHORT).show()
                } else {
                    showDialog(latitude, longitude)
                }
            }

        }
        return componentFragment
    }

    fun showDialog(latitude: String?, longitude: String?) {
        val prefs = this.requireActivity().getSharedPreferences(
            getString(R.string.prefs_file),
            AppCompatActivity.MODE_PRIVATE
        )
        val autor = prefs.getString("name", null)
        val dialogView = LayoutInflater.from(context).inflate(R.layout.dialogo_ubicacion, null)

        val dialog = AlertDialog.Builder(requireActivity())
            .setView(dialogView)
            .setCancelable(true)
            .create()
        val nombre = dialogView.findViewById<EditText>(R.id.nombre).text
        val info = dialogView.findViewById<EditText>(R.id.info).text
        val btnClose = dialogView.findViewById<Button>(R.id.btn_close)
        imagen = dialogView.findViewById(R.id.img)
        imagen.setOnClickListener {
            val intent = Intent()
            intent.type = "image/*"
            intent.action = Intent.ACTION_GET_CONTENT
            startActivityForResult(
                Intent.createChooser(intent, "Selecciona una imagen"),
                PICK_IMAGE_REQUEST
            )
        }

        btnClose.setOnClickListener {

            try {
                if (uri != null) {
                    db.collection("ubicaciones").document(nombre.toString()).set(
                        hashMapOf(
                            "latitude" to latitude,
                            "longitude" to longitude,
                            "nombre" to nombre.toString(),
                            "autor" to autor,
                            "descripcion" to info.toString()
                        )
                    )
                    uploadImageToFirebase(uri, nombre.toString())
                } else {
                    Toast.makeText(context, "Imagen no seleccionada", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {

            }
            dialog.dismiss()
        }
        dialog.show()
    }

    // Función para subir una imagen a Firebase Storage
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.data != null) {
            val imageUri = data.data
            //val imageView :
            imagen.setImageURI(imageUri)
            if (imageUri != null) {
                uri = imageUri
            }
        }
    }


    fun uploadImageToFirebase(imageUri: Uri, nombre: String) {
        val imageFileName = nombre.toString() // Nombre aleatorio para la imagen
        val imageRef = imagesRef.child("" + imageFileName)
        val uploadTask = imageRef.putFile(imageUri)
        uploadTask.addOnSuccessListener { taskSnapshot ->
            imageRef.downloadUrl.addOnSuccessListener { uri ->
                val downloadUrl = uri.toString()
            }
        }.addOnFailureListener { exception ->
            // Maneja el error según sea necesario
        }
    }
}

