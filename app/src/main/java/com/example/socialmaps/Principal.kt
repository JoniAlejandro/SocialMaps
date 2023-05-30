package com.example.socialmaps

import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.PersistableBundle
import android.view.MenuItem
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.socialmaps.models.Notificaciones
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth

class Principal : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {
    private lateinit var drawer : DrawerLayout
    private  lateinit var  toogle : ActionBarDrawerToggle
    var receivedData1: String? = null
    var receivedData2: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_principal)
        datosPerfil()
        val navigationView : NavigationView = findViewById(R.id.nav_view)
        val toolbar :  androidx.appcompat.widget.Toolbar = findViewById(R.id.toolbar_main)
        setSupportActionBar(toolbar)
        drawer = findViewById(R.id.drawer_layout)
        toogle = ActionBarDrawerToggle(this,drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close)
        drawer.addDrawerListener(toogle)
        toogle.syncState()
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeButtonEnabled(true)
        navigationView.setNavigationItemSelectedListener (this)
    }
    fun datosPerfil(){
        val navigationView : NavigationView = findViewById(R.id.nav_view)
        val prefs = getSharedPreferences(getString(R.string.prefs_file), MODE_PRIVATE)
        val name = prefs.getString("name", null)
        val email = prefs.getString("email", null)
        val photoUrl = prefs.getString("photoUrl", null)
        if (photoUrl != null) {
            Glide.with(this)
                .load(photoUrl).apply(RequestOptions.circleCropTransform())
                .into(navigationView.getHeaderView(0).findViewById<ImageView>(R.id.nav_header_imagen))
        }
        navigationView.getHeaderView(0).findViewById<TextView>(R.id.nav_header_nombre).text = name
        navigationView.getHeaderView(0).findViewById<TextView>(R.id.nav_header_correo).text = email
    }
    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.nav_item_inicio -> {
                supportFragmentManager.beginTransaction().apply {
                    replace(R.id.fragmentContenedor, MapFragment())
                    commit()
                }
            }
            R.id.nav_item_cerrarSesion -> {
                val intentar = Intent(this, MainActivity::class.java)
                startActivity(intentar)
                FirebaseAuth.getInstance().signOut()
                val prefs = this.getSharedPreferences(getString(R.string.prefs_file), Context.MODE_PRIVATE).edit()
                prefs.clear()
                prefs.apply()
                finish()
            }
            R.id.nav_item_editarPerfil -> {
                supportFragmentManager.beginTransaction().apply {
                    val prefs = getSharedPreferences(getString(R.string.prefs_file), MODE_PRIVATE)
                    val google = prefs.getBoolean("google", false)
                    if(google){
                        Toast.makeText(applicationContext,"Modifica tu cuenta de google", Toast.LENGTH_SHORT).show()
                    }else {
                        replace(R.id.fragmentContenedor, EditarPerfil())
                        commit()
                    }
                }
            }

        }
        drawer.closeDrawer(GravityCompat.START)
        return true
    }

    override fun onPostCreate(savedInstanceState: Bundle?, persistentState: PersistableBundle?) {
        super.onPostCreate(savedInstanceState, persistentState)
        toogle.syncState()
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        toogle.onConfigurationChanged(newConfig)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return super.onOptionsItemSelected(item)
        if(toogle.onOptionsItemSelected(item)){
            return true
        }
    }

    fun onTwoDataReceived(data1: String, data2: String) {
        receivedData1 = data1
        receivedData2 = data2
    }


}