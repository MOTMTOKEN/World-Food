package com.se.iths.app21.grupp1.myapplication.view

import android.Manifest
import android.annotation.SuppressLint
import android.app.ActionBar
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.graphics.drawable.ColorDrawable
import android.location.Geocoder
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.se.iths.app21.grupp1.myapplication.CuisineSelectListener
import com.se.iths.app21.grupp1.myapplication.adapter.CuisinesRecycleAdapter
import com.se.iths.app21.grupp1.myapplication.adapter.PlaceInfoAdapter
import com.se.iths.app21.grupp1.myapplication.model.Places
import com.se.iths.app21.grupp1.myapplication.R
import com.se.iths.app21.grupp1.myapplication.databinding.ActivityMapsBinding
import java.util.*

class MapsActivity : AppCompatActivity(), OnMapReadyCallback, GoogleMap.OnMapLongClickListener,
    GoogleMap.OnMapClickListener, GoogleMap.OnInfoWindowClickListener {

    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityMapsBinding

    private lateinit var auth: FirebaseAuth

    private lateinit var db: FirebaseFirestore

    private lateinit var locationManager: LocationManager
    private lateinit var locationListener: LocationListener

    private lateinit var permissionLauncher: ActivityResultLauncher<String>

    private lateinit var sharedPreferences: SharedPreferences

    private var trackBoolean: Boolean? = null

    private var selectedLatitude: Double? = null
    private var selectedLongtitude: Double? = null

    private var infoMaps = false

    lateinit var recyclerView: RecyclerView

    var adapter: CuisinesRecycleAdapter? = null

    var isClicked = false

    private var directionOfPlaces = false
    private var placeList = ArrayList<Places>()

    @SuppressLint("RestrictedApi", "WrongConstant")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)


        supportActionBar?.setBackgroundDrawable(ColorDrawable(0xff00ACED.toInt()))
        actionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Maps"

        supportActionBar?.displayOptions = ActionBar.DISPLAY_SHOW_CUSTOM
        supportActionBar?.setCustomView(R.layout.abs_layout)
        registerLauncher()


        placeList = ArrayList<Places>()
        auth= FirebaseAuth.getInstance()
        db= FirebaseFirestore.getInstance()


        sharedPreferences =
            this.getSharedPreferences("com.se.iths.app21.grupp1.myapplication", MODE_PRIVATE)
        trackBoolean = false

        selectedLatitude = 0.0
        selectedLongtitude = 0.0

        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        recyclerView = findViewById(R.id.recyclerView)


        recyclerView.layoutManager = LinearLayoutManager(this)
        this.adapter = CuisinesRecycleAdapter(this)

        recyclerView.adapter = this.adapter

        val categoryFAB = binding.categoryFloatingActionButton
        val categoryRecyclerView = binding.recyclerView

        categoryFAB.setOnClickListener {
            categoryRecyclerView.isVisible = !categoryRecyclerView.isVisible
        }

        if (isClicked) {
            categoryFAB.setOnClickListener {
                categoryRecyclerView.isVisible = !categoryRecyclerView.isVisible
                isClicked = false
            }
        }

        adapter!!.CuisineSelectListener = CuisineSelectListener {
            this.getData()

        }


    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val menuInflater = menuInflater
        menuInflater.inflate(R.menu.place_menu, menu)

        val signIn = menu?.findItem(R.id.signIn)
        val signOut = menu?.findItem(R.id.signOut)

        if (auth.currentUser == null) {
            signOut?.isVisible = false
            signIn?.isVisible = true
        } else {
            signIn?.isVisible = false
            signOut?.isVisible = true
        }

        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        when (item.itemId) {
            R.id.signIn -> {

                val intent = Intent(this, InloggningActivity::class.java)
                startActivity(intent)
            }
            R.id.signOut -> {
                auth.signOut()
                val intent = Intent(this@MapsActivity, MapsActivity::class.java)
                startActivity(intent)
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        mMap.setOnMapLongClickListener(this)

        mMap.setOnInfoWindowClickListener(this)

        mMap.setOnMapClickListener(this)

        getData()
        locationManager = this.getSystemService(LOCATION_SERVICE) as LocationManager

        locationListener = object : LocationListener {
            override fun onLocationChanged(location: Location) {

                trackBoolean = sharedPreferences.getBoolean("trackBoolean", false)

                if (trackBoolean == false) {
                    val userLocation = LatLng(location.latitude, location.longitude)
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLocation, 15f))
                    mMap.uiSettings.isZoomControlsEnabled = true
                    sharedPreferences.edit().putBoolean("trackBoolean", true).apply()

                }

            }

            override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {
                super.onStatusChanged(provider, status, extras)
            }

        }

        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {


            if (ActivityCompat.shouldShowRequestPermissionRationale(
                    this,
                    Manifest.permission.ACCESS_FINE_LOCATION
                )
            ) {
                Snackbar.make(
                    binding.root,
                    "Permission needed for location",
                    Snackbar.LENGTH_INDEFINITE
                ).setAction("Give Permission") {


                    permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                }.show()
            } else {
                permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
            }

        } else {


            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0f, locationListener)

            val lastlocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
            if (lastlocation != null) {
                val lastUserLocation = LatLng(lastlocation.latitude, lastlocation.longitude)
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(lastUserLocation, 15f))
            }
            mMap.isMyLocationEnabled = true
            mMap.uiSettings.isZoomControlsEnabled = true
        }
    }

    private fun registerLauncher() {
        permissionLauncher =
            registerForActivityResult(ActivityResultContracts.RequestPermission()) { result ->

                if (result) {
                    if (ContextCompat.checkSelfPermission(
                            this,
                            Manifest.permission.ACCESS_FINE_LOCATION
                        ) == PackageManager.PERMISSION_GRANTED
                    ) {
                        locationManager.requestLocationUpdates(
                            LocationManager.GPS_PROVIDER,
                            0,
                            0f,
                            locationListener
                        )
                        val lastlocation =
                            locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
                        if (lastlocation != null) {
                            val lastUserLocation =
                                LatLng(lastlocation.latitude, lastlocation.longitude)
                            mMap.moveCamera(
                                CameraUpdateFactory.newLatLngZoom(
                                    lastUserLocation,
                                    15f
                                )
                            )
                        }
                        mMap.isMyLocationEnabled = true
                        mMap.uiSettings.isZoomControlsEnabled = true
                    }

                } else {
                    Toast.makeText(this@MapsActivity, "Permission Needed", Toast.LENGTH_LONG).show()
                }


            }

    }

    override fun onMapLongClick(p0: LatLng) {
        directionOfPlaces = true
        mMap.clear()

        infoMaps = true
        mMap.addMarker(
            MarkerOptions().position(p0).title("Vill du spara den plats? Tryck här")
                .snippet(getAddress(p0.latitude, p0.longitude))
        )

        selectedLatitude = p0.latitude
        selectedLongtitude = p0.longitude
    }

    private fun getAddress(lat: Double, lon: Double): String? {

        val geoCoder = Geocoder(this, Locale.getDefault())
        val address = geoCoder.getFromLocation(lat, lon, 1)
        return address[0].getAddressLine(0).toString()
    }


    override fun onInfoWindowClick(p0: Marker) {
        if (directionOfPlaces) {
            if (auth.currentUser != null) {
                if (infoMaps) {
                    val intent = Intent(this, AddPlaceActivity::class.java)
                    intent.putExtra("lat", selectedLatitude)
                    intent.putExtra("long", selectedLongtitude)
                    startActivity(intent)
                }
            } else {
                Snackbar.make(binding.root, "Please first sign in ", Snackbar.LENGTH_INDEFINITE)
                    .setAction("Go to inloggning sida") {
                        val intent = Intent(this, InloggningActivity::class.java)
                        startActivity(intent)
                    }.show()
            }
        } else {

            val place = p0.tag as Places
            val intent = Intent(this, PlacesActivity::class.java)
            intent.putExtra("docId", place.id)
            startActivity(intent)
        }
    }

    override fun onMapClick(p0: LatLng) {
        mMap.clear()
        getData()
    }

    private fun getData() {

        directionOfPlaces = false

        db.collection("Places").addSnapshotListener { value, error ->

            if (error != null){
                Toast.makeText(this, error.localizedMessage, Toast.LENGTH_LONG).show()
            } else{

                if(value != null){
                    if(!value.isEmpty){
                        val documents = value.documents
                        for (document in documents){
                            val place = document.toObject(Places::class.java)

                            placeList.add(Places(place!!.id, place.name, place.land, place.beskrivning,place.lat, place.long, place.userEmail, place.image))
                            place.land?.let { adapter!!.addCuisine(it) }
                        }

                    }
                }
            }

            for (place in placeList){
                if(adapter!!.selectedCountries.isEmpty() || adapter!!.selectedCountries.contains(place.land)){

                    val marker = mMap.addMarker(MarkerOptions().position(LatLng(place.lat!!.toDouble(), place.long!!.toDouble())))
                    marker!!.tag = place

                    val placeAdapter = PlaceInfoAdapter(this@MapsActivity)
                    mMap.setInfoWindowAdapter(placeAdapter)
                }}
        }

    }}

