package com.se.iths.app21.grupp1.myapplication

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.google.android.gms.maps.model.MarkerOptions

import com.google.firebase.firestore.GeoPoint

import com.google.firebase.firestore.DocumentSnapshot

import androidx.annotation.NonNull
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.model.LatLng

import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.auth.FirebaseAuth

import com.google.firebase.firestore.DocumentReference

import com.google.firebase.firestore.CollectionReference

import com.google.firebase.firestore.FirebaseFirestore
import com.se.iths.app21.grupp1.myapplication.databinding.ActivityMapsBinding


class PlacesActivity : AppCompatActivity() {

    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityMapsBinding

    private lateinit var auth: FirebaseAuth

    private lateinit var db: FirebaseFirestore


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_places)






    }
}