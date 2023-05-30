package com.example.socialmaps

import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.getSystemService
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.example.socialmaps.models.Notificaciones
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage


class MapaFragment : Fragment(),GoogleMap.OnMyLocationButtonClickListener, GoogleMap.OnMarkerClickListener{
    private val db = FirebaseFirestore.getInstance()
    val storageRef = FirebaseStorage.getInstance().reference
    val imagesRef = storageRef.child("images")
    private lateinit var map : GoogleMap
    companion object{
         const val RC_CODE_LOCATION = 0
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View?{
        val componentFragment = inflater.inflate(R.layout.fragment_mapa, container, false)
        createFragment()
        return componentFragment
    }
    fun createFragment(){
        val mapaFragment  =  childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
        mapaFragment?.getMapAsync(callback)
    }
    private val callback = OnMapReadyCallback { googleMap ->
        map = googleMap
        enableUbicacion()
        crearMarcador()
        map.uiSettings.isRotateGesturesEnabled = true
        map.setOnMyLocationButtonClickListener(this)
        //map.setOnMyLocationClickListener ( this )
        map.setOnMarkerClickListener(this)
    }
    fun crearMarcador(){
        db.collection("ubicaciones")
            .addSnapshotListener { queryDocumentSnapshots, e ->
                queryDocumentSnapshots?.documentChanges?.forEach { mDocumentChange ->
                    if(mDocumentChange.type == DocumentChange.Type.ADDED){
                        val nombre = mDocumentChange.document.get("nombre").toString()
                        val latitude = mDocumentChange.document.get("latitude").toString()
                        val longitude = mDocumentChange.document.get("longitude").toString()
                        val ubicacion = LatLng(latitude.toDouble(), longitude.toDouble())
                        val marker = MarkerOptions().position(ubicacion).title(nombre)
                        map.addMarker(marker)
                        map.animateCamera(CameraUpdateFactory.newLatLngZoom(ubicacion, 15f), 4000, null)
                        notificar()
                    }
                }
            }
    }

    private fun permisoUbicacion() = context?.let { ContextCompat.checkSelfPermission(it,android.Manifest.permission.ACCESS_FINE_LOCATION) } == PackageManager.PERMISSION_GRANTED
    private fun enableUbicacion(){
        if(!::map.isInitialized) return
        if(permisoUbicacion()){
            map.isMyLocationEnabled = true
        }else{
            pedirPermiso()
        }
    }

    private fun pedirPermiso(){
        if(ActivityCompat.shouldShowRequestPermissionRationale(requireActivity(), android.Manifest.permission.ACCESS_FINE_LOCATION)){
            Toast.makeText(context,"Aceptar los permisos en ajustes",Toast.LENGTH_SHORT).show()
        }else{
            ActivityCompat.requestPermissions(requireActivity(), arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION), RC_CODE_LOCATION)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        when(requestCode){
            RC_CODE_LOCATION -> if(grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                map.isMyLocationEnabled = true
            }else{
                Toast.makeText(context,"Debes aceptar los permisos",Toast.LENGTH_SHORT).show()
            }
            else ->{}
        }
    }


    override fun onMyLocationButtonClick() :Boolean{
        if (activity is Principal) {
            val mainActivity = activity as Principal
            mainActivity.onTwoDataReceived(map.myLocation.latitude.toString(), map.myLocation.longitude.toString())
        }
       return false
    }


    override fun onMarkerClick(p0: Marker): Boolean {
        showDialog(p0.title)
        return true
    }
    fun showDialog(title: String?, ) {
        val dialogView = LayoutInflater.from(context).inflate(R.layout.info_ubicacion, null)
        val autor = dialogView.findViewById<TextView>(R.id.autor)
        val descripcion = dialogView.findViewById<TextView>(R.id.descripcion)
        val titulo = dialogView.findViewById<TextView>(R.id.tituloInfo)
        if (title != null) {
            db.collection("ubicaciones").document(title).get().addOnSuccessListener {
                autor.text = it.get("autor") as String?
                descripcion.text = it.get("descripcion") as String?
                titulo.text = it.get("nombre") as String?
            }
        }
        val fileName = title
        val fileRef = fileName?.let { imagesRef.child(it) }
        // Descargar la imagen y mostrarla en un ImageView
        val imageView = dialogView.findViewById<ImageView>(R.id.verImg)
        if (fileRef != null) {

            fileRef.downloadUrl.addOnSuccessListener{
                Glide.with(this)
                    .load(it)
                    .into(imageView)
            }
        }
        val dialog = AlertDialog.Builder(requireActivity())
            .setView(dialogView)
            .setCancelable(true)
            .create()
        dialog.show()
    }
    fun notificar() {
        Notificaciones.showNotification(requireContext(), "Descubre un nuevo lugar!")
    }
}




