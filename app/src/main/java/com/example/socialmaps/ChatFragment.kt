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
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.socialmaps.adapters.MessageAdapter
import com.example.socialmaps.models.Message
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.ktx.Firebase

class ChatFragment : Fragment() {
    private lateinit var lstMensajes : ArrayList<Message>
    private var db = Firebase.firestore
    private lateinit var rvMensajes : RecyclerView
    private lateinit var mensaj : EditText
    private lateinit var mAdapterMensaje: MessageAdapter
    private lateinit var btnEnviar : FloatingActionButton
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View?{
        val componentFragment = inflater.inflate(R.layout.fragment_chat, container, false)
        // Crear una instancia del Fragmento anidado
        rvMensajes = componentFragment.findViewById(R.id.mensajesRecylerView)
        mensaj = componentFragment.findViewById(R.id.mensaje)
        btnEnviar = componentFragment.findViewById(R.id.btnEnviar)
        lstMensajes = ArrayList()
        mAdapterMensaje = MessageAdapter(lstMensajes)
        rvMensajes.layoutManager = LinearLayoutManager(context)
        rvMensajes.adapter = mAdapterMensaje
        rvMensajes.setHasFixedSize(true)
        db.collection("Chat").orderBy( "dob")
            .addSnapshotListener { queryDocumentSnapshots, e ->
                queryDocumentSnapshots?.documentChanges?.forEach { mDocumentChange ->
                    if(mDocumentChange.type == DocumentChange.Type.ADDED){
                        lstMensajes.add(mDocumentChange.document.toObject(Message::class.java))
                        mAdapterMensaje.notifyDataSetChanged()
                        rvMensajes.smoothScrollToPosition(lstMensajes.size)
                    }
                }
            }
        btnEnviar.setOnClickListener{
            if(mensaj.text.toString().isNotEmpty()){
                val prefs = this.requireActivity().getSharedPreferences(getString(R.string.prefs_file),
                    AppCompatActivity.MODE_PRIVATE
                )
                val nombre = prefs.getString("name", null)
                val mMensaje = Message().apply {
                    mensaje = mensaj.text.toString()
                    if (nombre != null) {
                        name = nombre
                    }
                }
                db.collection("Chat").add(mMensaje)
                mensaj.setText("")
            }
        }
        return componentFragment
    }


}

