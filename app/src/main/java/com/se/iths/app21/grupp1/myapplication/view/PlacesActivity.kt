package com.se.iths.app21.grupp1.myapplication.view

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.drawable.ColorDrawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import com.google.android.material.snackbar.Snackbar
import com.se.iths.app21.grupp1.myapplication.adapter.DescriptionRecyclerAdapter
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.se.iths.app21.grupp1.myapplication.R
import com.se.iths.app21.grupp1.myapplication.model.Comments
import com.se.iths.app21.grupp1.myapplication.model.Place
import com.se.iths.app21.grupp1.myapplication.databinding.ActivityPlacesBinding
import com.se.iths.app21.grupp1.myapplication.model.Places
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_places.*
import java.util.*


class PlacesActivity : AppCompatActivity() {

    lateinit var binding: ActivityPlacesBinding

    var commentslist : MutableList<Comments> = mutableListOf()
    var placeslist : MutableList<Place> = mutableListOf()
    lateinit var auth: FirebaseAuth
    lateinit var db : FirebaseFirestore
    lateinit var storage : FirebaseStorage
    var mAdapter: DescriptionRecyclerAdapter? = null
    lateinit var collectionRef: CollectionReference
    var lat : Double? = null
    var long : Double? = null

    private var placeId: String? = null
    private var userName : String? = null
    private var userDocumentId: String? = null
    private var commentList = ArrayList<Comments>()

    @SuppressLint("WrongConstant")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPlacesBinding.inflate(layoutInflater)
        setContentView(binding.root)


        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()
        storage = FirebaseStorage.getInstance()

        supportActionBar?.setBackgroundDrawable(ColorDrawable(R.drawable.background_color))
        actionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val currentUser = auth.currentUser
        if (currentUser != null){
            commentList = ArrayList<Comments>()

            commentText.visibility= View.GONE
            commentButton.visibility = View.GONE
            cancelButton.visibility = View.GONE


        }else{

            commentText.visibility= View.GONE
            commentButton.visibility = View.GONE
            cancelButton.visibility = View.GONE
            addCommentButton.visibility = View.GONE
            Snackbar.make(binding.root, "Please first sign in to type a comment ", Snackbar.LENGTH_INDEFINITE).setAction("Go to inloggning sida",){
                val intent = Intent(this, InloggningActivity::class.java)
                startActivity(intent)
            }.show()
        }



        val docId = intent.getStringExtra("docId")


        getPlacesInfo()

        addCommentButton.setOnClickListener {
            commentText.visibility= View.VISIBLE
            addCommentButton.visibility = View.GONE
            commentButton.visibility = View.VISIBLE
            cancelButton.visibility = View.VISIBLE
        }

        cancelButton.setOnClickListener {
            commentText.visibility= View.GONE
            addCommentButton.visibility = View.VISIBLE
            commentButton.visibility = View.GONE
            cancelButton.visibility = View.GONE
        }

    }

   private fun getPlacesInfo() {

       val docId = intent.getStringExtra("docId")

       val uuid = UUID.randomUUID()
       val imageName = "$uuid"
       val reference = storage.reference
       val imageReference = reference.child("images").child(imageName)


       if (docId != null) {
           db.collection("Places").document(docId)
               .get()
               .addOnSuccessListener {  task ->
                   if (task != null)
                        {
                            val place = task.toObject(Places::class.java)
                            supportActionBar?.title= place!!.name!!.toUpperCase() + " RESTAURANGEN "
                            landPlacesText.text = place!!.land
                            beskrivningPlacesText.text = place.beskrivning
                             db.collection("Places").document(docId)
                                 .get()
                                 .addOnCompleteListener {
                                     if (task != null) {
                                         val result: StringBuffer = StringBuffer()
                                         if(it.isSuccessful) {
                                             var url = result.append(it.result!!.data!!.getValue("image")).toString()
                                             Picasso.get().load(url).into(selectImage)
                                                }
                                     }
                                 }
                        }

               }
       }
   }


fun addComment(view: View){

    val comments = hashMapOf<String, Any>()

    comments["comment"] = commentText.text.toString()
    comments["userDocumentId"] = userDocumentId.toString()
    comments["placeId"] = placeId.toString()
    comments["userName"] = userName.toString()
    comments["email"] = auth.currentUser!!.email.toString()
    comments["date"] = Timestamp.now()

    db.collection("Comments").add(comments).addOnSuccessListener {
        Toast.makeText(this, "Successfully", Toast.LENGTH_LONG).show()

        commentText.visibility= View.GONE
        addCommentButton.visibility = View.VISIBLE
        commentButton.visibility = View.GONE
        cancelButton.visibility = View.GONE
        commentText.setText("")

    }.addOnFailureListener {
        Toast.makeText(this, it.localizedMessage, Toast.LENGTH_LONG).show()
    }
}



}