package com.example.socialmaps

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore


class MainActivity : AppCompatActivity() {

    private val db = FirebaseFirestore.getInstance()
    lateinit var mGoogleSignInClient: GoogleSignInClient
    lateinit var gso:GoogleSignInOptions
    var googleSignInAccount: GoogleSignInAccount? = null
    companion object{
        private const val RC_SIGN_IN = 9001
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        sesionActiva()
        setup()

    }

    private fun setup() {
        // Recursos necesarios para logincon Google
        gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        mGoogleSignInClient = GoogleSignIn.getClient(this,gso)

        // EditText
        val email = findViewById<EditText>(R.id.email)
        val password = findViewById<EditText>(R.id.password)
        val verificar = VerificarInformacion()
        //Botones
        val btnLogin = findViewById<Button>(R.id.btnLogin)
        val btnRegistrarse = findViewById<Button>(R.id.btnRegistrarse)
        val btnGoogle = findViewById<Button>(R.id.btnGoogle)
        // Funcion click
        btnLogin.setOnClickListener {
            if (verificar.validarCamposLogin(email, password)) {
                signIn(email.text.toString(), password.text.toString())
            }

        }
        btnRegistrarse.setOnClickListener {
            val intentar = Intent(this, Registrarse::class.java)
            startActivity(intentar)
        }

        btnGoogle.setOnClickListener {
            mGoogleSignInClient.signOut()
            val signInIntent = mGoogleSignInClient.signInIntent
            startActivityForResult(signInIntent, RC_SIGN_IN)
        }
    }
    //Verificar si ya habia iniciado sesion antes
    private fun sesionActiva(){
        if(FirebaseAuth.getInstance().currentUser != null){
            //FirebaseAuth.getInstance().currentUser?.email.toString()
            showInicio()
        }
    }
    //funcion que muestra la pantalla principal del app
    private  fun showInicio(){
        val intent = Intent(this, Principal::class.java)
        startActivity(intent)
        finish()
    }
    //Verificar que ya haya sido registrado con correo y contrasena
    private fun signIn(email: String, password: String){
        FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password).addOnCompleteListener(this) { task ->
            if (task.isSuccessful) {
                db.collection("usuarios").document(email).get().addOnSuccessListener {
                    val name = it.get("nombre") as String? + " " + it.get("apellido") as String?
                    val prefs =
                        getSharedPreferences(
                            getString(R.string.prefs_file),
                            Context.MODE_PRIVATE
                        ).edit()
                    prefs.putString("name", name)
                    prefs.putString("email", email)
                    prefs.putString("password", password)
                    prefs.apply()
                    showInicio()
                }

            }else{
                showAlert("No fue posible verificar")
            }
        }
    }

    //Muestra alertas
    fun showAlert(mensaje : String){
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Error")
        builder.setMessage(mensaje)
        builder.setPositiveButton("Aceptar", null)
        val dialog : AlertDialog = builder.create()
        dialog.show()
    }
    //Funcion llamada con el btnGoogle
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        //val user = Firebase.auth.currentUser
        if (requestCode == RC_SIGN_IN){
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)

            try{
                val account = task.getResult(ApiException::class.java)
                if(account != null) {
                    val credential = GoogleAuthProvider.getCredential(account.idToken, null)
                    FirebaseAuth.getInstance().signInWithCredential(credential).addOnCompleteListener {
                            if (it.isSuccessful) {
                                val user = FirebaseAuth.getInstance().currentUser
                                val name = user?.displayName.toString()
                                val email = user?.email.toString()
                                val photoUrl = user?.photoUrl.toString()
                                val prefs =
                                    getSharedPreferences(
                                        getString(R.string.prefs_file),
                                        Context.MODE_PRIVATE
                                    ).edit()
                                prefs.putString("name", name)
                                prefs.putBoolean("google", true)
                                prefs.putString("email", email)
                                prefs.putString("photoUrl", photoUrl)
                                prefs.apply()
                                showInicio()
                            } else {
                                showAlert("Error al acceder a la cuenta")
                            }
                        }
                }
            }catch (e:ApiException){
                //"Error: ${e.message}"
                Toast.makeText(this,"Proceso cancelado",Toast.LENGTH_SHORT).show()
            }
        }
    }
}