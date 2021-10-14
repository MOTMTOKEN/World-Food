package com.se.iths.app21.grupp1.myapplication

import android.app.Activity
import android.app.ProgressDialog
import android.content.Intent
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.se.iths.app21.grupp1.myapplication.databinding.ActivityAddPlaceBinding
import java.text.SimpleDateFormat
import java.util.*
import androidx.annotation.NonNull

import com.google.android.gms.tasks.OnFailureListener

import com.google.firebase.storage.UploadTask

import com.google.android.gms.tasks.OnSuccessListener
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.StorageReference


class AddPlaceActivity : AppCompatActivity() {
    lateinit var binding: ActivityAddPlaceBinding

    lateinit var auth: FirebaseAuth
    lateinit var db : FirebaseFirestore

    var lat: Double? = null
    var long: Double? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddPlaceBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        val intent = intent
        lat = intent.getDoubleExtra("lat",0.0)
        long = intent.getDoubleExtra("long", 0.0)

        println(lat)
        println(long)

        binding.saveButton.setOnClickListener {
            savePlaces()
            saveImage()
            val intent = Intent(this, MapsActivity::class.java)
            startActivity(intent)
        }





        binding.selectImage.setOnClickListener {

            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"

            startActivityForResult(intent, 0)



        }


    }

    lateinit var selectedPhotoUri: Uri

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 0 && resultCode == Activity.RESULT_OK && data != null) {
            val selectedPhotoUri = data.data

            val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, selectedPhotoUri)

            val bitmapDrawable = BitmapDrawable(bitmap)
            binding.selectImage.setBackgroundDrawable(bitmapDrawable)
        }

    }

    private fun saveImage() {

        if (selectedPhotoUri == null) return

        val filename = UUID.randomUUID().toString()

        val ref = FirebaseStorage.getInstance().getReference("/images/$filename")

        ref.putFile(selectedPhotoUri!!)
            .addOnSuccessListener {
                Log.d("!!!", "Succesfully uploaded image: ${it.metadata?.path}")
            }.addOnFailureListener {
                Log.d("!!!", "failed to uploaded image:")
            }


    }












    private fun savePlaces(){

        val places = hashMapOf<String, Any>()

        if(auth.currentUser != null){
            places["userEmail"] = auth.currentUser!!.email!!
            places["name"] = binding.placeNameText.text.toString()
            places["land"] = binding.landText.text.toString()
            places["lat"] = lat!!.toDouble()
            places["long"] = long!!.toDouble()
            places["beskrivning"] = binding.beskrivningText.text.toString()
            places["date"] = Timestamp.now()

            db.collection("Places" ).add(places).addOnSuccessListener {
                finish()
            }.addOnFailureListener {
                Toast.makeText(this, it.localizedMessage, Toast.LENGTH_LONG).show()
            }
        }
    }


    fun givePermissin(){

    }

}