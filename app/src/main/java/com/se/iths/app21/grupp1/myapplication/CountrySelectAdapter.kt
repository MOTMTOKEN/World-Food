package com.se.iths.app21.grupp1.myapplication

import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.activity_add_place.view.*
import kotlinx.android.synthetic.main.list_country_select.view.*

class CountrySelectAdapter(
    private val context: Context,
    private val images: List<Image>,
    var landText: Button,
    var recyclerView: RecyclerView
) : RecyclerView.Adapter<CountrySelectAdapter.ViewHolder>() {


    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val img = itemView.findViewById<ImageView>(R.id.flagImageView)
        val imgTitle = itemView.findViewById<TextView>(R.id.countrySelectTextView)



        fun bindView(image: Image) {
            img.setImageResource(image.imageSrc)
            imgTitle.text = image.countryTitle
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder =
        ViewHolder(
            LayoutInflater.from(context).inflate(R.layout.list_country_select, parent, false)
        )

    override fun getItemCount(): Int = images.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bindView(images[position])


        holder.itemView.countrySelectTextView.setOnClickListener {
            landText.text = images[position].countryTitle
            recyclerView.visibility = View.GONE


        }



    }
}
