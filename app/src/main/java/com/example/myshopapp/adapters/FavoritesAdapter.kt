package com.example.myshopapp.adapters

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.myshopapp.R
import com.example.myshopapp.dataclasses.ProductDataClass
import com.example.myshopapp.fragments.ProductDetailsFragment
import com.google.firebase.firestore.FirebaseFirestore

class FavoritesAdapter(
    private val context: Context,
    private var favoriteItems: MutableList<ProductDataClass>,
    private val listener: FavoritesEmptyListener,
    private val userId: String
) : RecyclerView.Adapter<FavoritesAdapter.FavoriteViewHolder>() {

    interface FavoritesEmptyListener {
        fun onFavoritesEmpty()
    }


    inner class FavoriteViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val imageViewProduct: ImageView = itemView.findViewById(R.id.imageViewProduct)
        private val imageViewWishHeart: ImageView = itemView.findViewById(R.id.imageViewWishHeart)
        private val textViewProductTitle: TextView = itemView.findViewById(R.id.textViewProductTitle)
        private val textViewRating: TextView = itemView.findViewById(R.id.textViewRating)
        private val textViewPrice1: TextView = itemView.findViewById(R.id.textViewPrice1)
        private val textViewPrice2: TextView = itemView.findViewById(R.id.textViewPrice2)
        private val textViewReviews: TextView = itemView.findViewById(R.id.textViewReviews)

        private val db = FirebaseFirestore.getInstance()

        fun bind(productItem: ProductDataClass) {
            // Load image with Glide
            Glide.with(itemView.context)
                .load(productItem.imageResId) // Assuming imageResId is a drawable resource ID
                .into(imageViewProduct)

            imageViewWishHeart.setImageResource(if (productItem.isFavorite) R.drawable.heart else R.drawable.wish_heart)
            imageViewWishHeart.setColorFilter(ContextCompat.getColor(context, if (productItem.isFavorite) R.color.red else R.color.black))

            textViewProductTitle.text = productItem.title
            textViewPrice1.text = productItem.price1.toString()
            textViewPrice2.text = productItem.price2.toString()
            textViewRating.text = String.format("%.1f", productItem.reviewRating)
            textViewReviews.text = "${productItem.totalReviews}"

            itemView.setOnClickListener {
                // Create a new instance of ProductDetailsFragment
                val fragment = ProductDetailsFragment()

                // Create arguments bundle and set data
                val args = Bundle()
                args.putInt("imageResId", productItem.imageResId)
                args.putString("title", productItem.title)
                args.putFloat("price1", productItem.price1)
                args.putFloat("price2", productItem.price2)
                args.putString("productId", productItem.productId)
                fragment.arguments = args

                // Get the fragment manager and start a transaction
                val activity = itemView.context as AppCompatActivity
                val fragmentManager = activity.supportFragmentManager
                val fragmentTransaction = fragmentManager.beginTransaction()

                // Replace the current fragment with the new one and add to back stack
                fragmentTransaction.replace(R.id.fragment_container, fragment)
                fragmentTransaction.addToBackStack(null)
                fragmentTransaction.commit()
            }

            imageViewWishHeart.setOnClickListener {
                val isFavorite = !productItem.isFavorite // Toggle favorite status
                productItem.isFavorite = isFavorite // Update favorite status in the product item
                db.collection("users").document(userId).collection("favorites").document(productItem.productId)
                    .delete()
                    .addOnSuccessListener {
                        Toast.makeText(context, R.string.removed_favorites, Toast.LENGTH_SHORT).show()
                        favoriteItems.removeAt(adapterPosition) // Remove the item from the list
                        notifyItemRemoved(adapterPosition) // Notify the adapter about item removal
                        if (favoriteItems.isEmpty()) {
                            listener.onFavoritesEmpty()
                        }
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(context, R.string.fav_rem_failed, Toast.LENGTH_SHORT).show()
                    }
            }
        }
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FavoriteViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.viewholder_product, parent, false)
        return FavoriteViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: FavoriteViewHolder, position: Int) {
        holder.bind(favoriteItems[position])
    }

    override fun getItemCount(): Int {
        return favoriteItems.size
    }
}
