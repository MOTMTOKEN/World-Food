package com.se.iths.app21.grupp1.myapplication

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.RatingBar
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.se.iths.app21.grupp1.myapplication.databinding.ActivityAddPlaceBinding
import java.util.*


import com.google.android.gms.tasks.OnSuccessListener




class AddPlaceActivity : AppCompatActivity() {
    lateinit var binding: ActivityAddPlaceBinding

    lateinit var auth: FirebaseAuth
    lateinit var db : FirebaseFirestore
    lateinit var storage : FirebaseStorage
    private var selectedPicture: Uri? = null
    private lateinit var activityResultLauncher: ActivityResultLauncher<Intent>
    private lateinit var permissionLaunher : ActivityResultLauncher<String>


    var lat: Double? = null
    var long: Double? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddPlaceBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()
        storage = FirebaseStorage.getInstance()

        val intent = intent
        lat = intent.getDoubleExtra("lat",0.0)
        long = intent.getDoubleExtra("long", 0.0)

        registerLauncher()


        binding.saveButton.setOnClickListener {
           // upload()
            savePlaces()
            val intent = Intent(this, MapsActivity::class.java)
            startActivity(intent)
        }

        binding.selectImage.setOnClickListener {
            givePermission()
        }

    }

    private fun savePlaces(){

        val places = hashMapOf<String, Any>()
       // val rBar = findViewById<RatingBar>(R.id.rBar)

        val uuid = UUID.randomUUID()
        val imageName = "$uuid"
        val reference = storage.reference
        val imageReference = reference.child("images").child(imageName)

        if(auth.currentUser != null && selectedPicture != null){

            imageReference.putFile(selectedPicture!!).addOnSuccessListener {

                val urlTask = imageReference.putFile(selectedPicture!!).continueWithTask { task ->
                    if (!task.isSuccessful) {
                        task.exception?.let {
                            throw it
                        }
                    }
                    imageReference.downloadUrl
                }.addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val downloadUri = task.result
                        places["userEmail"] = auth.currentUser!!.email!!
                        places["name"] = binding.placeNameText.text.toString()
                        places["land"] = binding.landText.text.toString()
                        places["lat"] = lat!!.toDouble()
                        places["long"] = long!!.toDouble()
                        places["beskrivning"] = binding.beskrivningText.text.toString()
                        places["date"] = Timestamp.now()
                        places["rating"] = binding.rBar.numStars
                        places["image"] = downloadUri.toString()

                            db.collection("Places" ).add(places).addOnSuccessListener {
                                finish()
                            }.addOnFailureListener {
                                Toast.makeText(this, it.localizedMessage, Toast.LENGTH_LONG).show()
                            }
                    } else {
                        // Handle failures
                        // ...
                    }
                }


            }.addOnFailureListener{
                Toast.makeText(this, it.localizedMessage, Toast.LENGTH_LONG).show()
            }

        }
    }

   private fun givePermission(){
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.READ_EXTERNAL_STORAGE)
            != PackageManager.PERMISSION_GRANTED){
            if(ActivityCompat.shouldShowRequestPermissionRationale(this,Manifest.permission.READ_EXTERNAL_STORAGE)){
                Snackbar.make(binding.root, "Permission needed for gallery", Snackbar.LENGTH_INDEFINITE).setAction("Give Permission"){
                    permissionLaunher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
                }.show()
            }else{
                permissionLaunher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
            }
        }else{

            val intentToGallery = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            activityResultLauncher.launch(intentToGallery)
        }
    }


    private fun registerLauncher(){

        activityResultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){result ->
            if(result.resultCode == RESULT_OK){
                val intentFromResult = result.data
                if (intentFromResult != null){
                  selectedPicture =  intentFromResult.data
                    selectedPicture?.let {
                        binding.selectImage.setImageURI(it)

                    }
                }
            }

        }

        permissionLaunher = registerForActivityResult(ActivityResultContracts.RequestPermission()){result ->
            if (result){
                val intentToGallery = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                activityResultLauncher.launch(intentToGallery)
            }else{
                Toast.makeText(this, "Permission needed", Toast.LENGTH_LONG).show()
            }

        }
    }

}




