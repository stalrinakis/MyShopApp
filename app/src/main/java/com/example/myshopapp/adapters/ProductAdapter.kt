package com.example.myshopapp.adapters

import android.content.Context
import android.graphics.Paint
import android.os.Bundle
import android.util.Log
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
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class ProductAdapter(private val context: Context, private var productItems: MutableList<ProductDataClass>) : RecyclerView.Adapter<ProductAdapter.ProductViewHolder>() {

    // Initialize Firestore
    private val db = FirebaseFirestore.getInstance()

    // User ID variable
    private var userId: String = ""

    private var userFavorites = mutableSetOf<String>()

    init {
        // Fetch user's favorite products from Firestore
        fetchUserFavorites()
    }

    private fun getCurrentUserId(): String {
        // Example implementation using Firebase Authentication
        val currentUser = FirebaseAuth.getInstance().currentUser
        return currentUser?.uid ?: ""
    }

    private fun fetchUserFavorites() {
        // Ensure userId is not empty or null
        userId = getCurrentUserId()
        if (userId.isNotEmpty()) {
            db.collection("users").document(userId).collection("favorites")
                .get()
                .addOnSuccessListener { documents ->
                    userFavorites.clear()
                    for (document in documents) {
                        val productId = document.getString("productId")
                        productId?.let { userFavorites.add(it) }
                    }
                    notifyDataSetChanged() // Update adapter once favorites are fetched
                }
                .addOnFailureListener { e ->
                    Log.e("Firestore", "Error fetching user favorites", e)
                }
        } else {
            Log.e("Firestore", "Invalid userId: $userId")
        }
    }


    inner class ProductViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val imageViewProduct: ImageView = itemView.findViewById(R.id.imageViewProduct)
        private val textViewProductTitle: TextView = itemView.findViewById(R.id.textViewProductTitle)
        private val textViewRating: TextView = itemView.findViewById(R.id.textViewRating)
        private val textViewPrice1: TextView = itemView.findViewById(R.id.textViewPrice1)
        private val textViewPrice2: TextView = itemView.findViewById(R.id.textViewPrice2)
        private val textViewReviews: TextView = itemView.findViewById(R.id.textViewReviews)
        private val imageViewWishHeart: ImageView = itemView.findViewById(R.id.imageViewWishHeart)

        fun bind(productItem: ProductDataClass) {
            /// Load image using Glide
            Glide.with(itemView)
                .load(productItem.imageResId)  // Assuming productItem.imageResId is a valid URL or drawable resource id
                .placeholder(R.drawable.ic_launcher_foreground) // Placeholder image while loading
                .error(R.drawable.trashcan) // Image to show if loading fails
                .into(imageViewProduct)

            textViewProductTitle.text = productItem.title
            textViewPrice1.text = productItem.price1.toString()
            textViewPrice1.paintFlags = textViewPrice1.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
            textViewPrice2.text = productItem.price2.toString()

            // Fetch and update reviews and rating
            fetchAndDisplayReviewsAndRating(productItem)

            // Set heart icon color based on favorite status
            if (userFavorites.contains(productItem.productId)) {
                imageViewWishHeart.setImageResource(R.drawable.heart)
                imageViewWishHeart.setColorFilter(ContextCompat.getColor(context, R.color.red))
            } else {
                imageViewWishHeart.setImageResource(R.drawable.wish_heart)
                imageViewWishHeart.setColorFilter(ContextCompat.getColor(context, R.color.black))
            }

            imageViewWishHeart.setOnClickListener {
                if (userFavorites.contains(productItem.productId)) {
                    // Already favorited, so remove from favorites
                    removeFromFavorites(productItem)
                } else {
                    // Not favorited, so add to favorites
                    addToFavorites(productItem)
                }
            }

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
                args.putDouble("reviewRating", productItem.reviewRating)
                args.putString("description", productItem.description)
                args.putInt("totalReviews", productItem.totalReviews)
                args.putInt("tvQuantityNumber", productItem.availableQuantity)

                fragment.arguments = args

                // Replace the current fragment with ProductDetailsFragment
                val transaction = (context as AppCompatActivity).supportFragmentManager.beginTransaction()
                transaction.replace(R.id.fragment_container, fragment)
                transaction.addToBackStack(null)  // Optional: Add to back stack if needed
                transaction.commit()
            }
        }

        private fun fetchAndDisplayReviewsAndRating(productItem: ProductDataClass) {
            db.collection("products").document(productItem.productId).collection("reviews")
                .get()
                .addOnSuccessListener { documents ->
                    var totalRating = 0.0
                    val reviewCount = documents.size()
                    for (document in documents) {
                        try {
                            val rating = document.getDouble("rating") ?: 0.0
                            totalRating += rating
                        } catch (e: Exception) {
                            Log.e("Firestore", "Error parsing rating", e)
                        }
                    }
                    val averageRating = if (reviewCount > 0) (totalRating / reviewCount) else 0.0

                    // Format averageRating to one decimal place
                    val formattedRating = String.format("%.1f", averageRating)

                    textViewRating.text = formattedRating
                    textViewReviews.text = reviewCount.toString()

                    // Update productItem with fetched data
                    productItem.reviewRating = averageRating
                    productItem.totalReviews = reviewCount
                }
                .addOnFailureListener { e ->
                    Log.e("Firestore", "Error fetching reviews", e)
                    textViewRating.text = "N/A"
                    textViewReviews.text = "0"
                    productItem.reviewRating = 0.0
                    productItem.totalReviews = 0
                }
        }

        private fun addToFavorites(product: ProductDataClass) {
            val favorite = hashMapOf(
                "productId" to product.productId
            )
            val docRef = db.collection("users").document(userId).collection("favorites").document(product.productId)

            docRef.set(favorite)
                .addOnSuccessListener {
                    Log.d("Firestore", "Successfully added to favorites")
                    Toast.makeText(context, R.string.add_to_fav, Toast.LENGTH_SHORT).show()
                    userFavorites.add(product.productId) // Update local set of favorites
                    notifyItemChanged(adapterPosition) // Update only the changed item
                }
                .addOnFailureListener { e ->
                    Log.e("Firestore", "Error adding to favorites", e)
                    Toast.makeText(context, "${R.string.error_adding_wish}: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }

        private fun removeFromFavorites(product: ProductDataClass) {
            db.collection("users").document(userId).collection("favorites")
                .document(product.productId)
                .delete()
                .addOnSuccessListener {
                    Log.d("Firestore", "Successfully removed from favorites")
                    Toast.makeText(context, R.string.removed_favorites, Toast.LENGTH_SHORT).show()
                    userFavorites.remove(product.productId) // Update local set of favorites
                    notifyItemChanged(adapterPosition) // Update only the changed item
                }
                .addOnFailureListener { e ->
                    Log.e("Firestore", "Error removing from favorites", e)
                    Toast.makeText(context, "${R.string.fav_rem_failed}: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.viewholder_product, parent, false)
        return ProductViewHolder(view)
    }

    override fun onBindViewHolder(holder: ProductViewHolder, position: Int) {
        val productItem = productItems[position]
        holder.bind(productItem)
    }

    override fun getItemCount(): Int {
        return productItems.size
    }

    fun updateData(newData: List<ProductDataClass>) {
        productItems.clear()
        productItems.addAll(newData)
        notifyDataSetChanged()
    }
}
