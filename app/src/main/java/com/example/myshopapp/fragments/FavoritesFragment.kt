package com.example.myshopapp.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myshopapp.R
import com.example.myshopapp.adapters.FavoritesAdapter
import com.example.myshopapp.dataclasses.ProductDataClass
import com.example.myshopapp.dataclasses.Smartphones
import com.example.myshopapp.dataclasses.Tablets
import com.example.myshopapp.dataclasses.TVs
import com.example.myshopapp.dataclasses.Laptops
import com.example.myshopapp.dataclasses.SoftwareAccessories
import com.example.myshopapp.dataclasses.VideoGames
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class FavoritesFragment : Fragment(), FavoritesAdapter.FavoritesEmptyListener {

    private lateinit var recyclerViewFavorites: RecyclerView
    private lateinit var favoritesAdapter: FavoritesAdapter
    private lateinit var emptyTextView: TextView
    private val favoriteProducts = mutableListOf<ProductDataClass>()
    private lateinit var bottomNavigationView: BottomNavigationView

    private lateinit var userId: String // User ID variable

    // Initialize Firestore
    private val db = FirebaseFirestore.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_favorites, container, false)
        // Retrieve current user's ID
        userId = getCurrentUserId()

        bottomNavigationView = requireActivity().findViewById(R.id.bottom_navigation_bar)

        recyclerViewFavorites = view.findViewById(R.id.recyclerViewFavorites)
        emptyTextView = view.findViewById(R.id.textViewEmptyMessage)

        // Use GridLayoutManager with 2 columns
        recyclerViewFavorites.layoutManager = GridLayoutManager(requireContext(), 2)

        favoritesAdapter = FavoritesAdapter(requireContext(), favoriteProducts, this, userId)
        recyclerViewFavorites.adapter = favoritesAdapter

        // Load favorite products
        loadFavoriteProducts()

        // Set OnClickListener to the back arrow ImageView
        val imageViewDetailsArrow: ImageView = view.findViewById(R.id.imageViewDetailsArrow)
        imageViewDetailsArrow.setOnClickListener {
            requireActivity().supportFragmentManager.popBackStack()
        }

        return view
    }

    private fun getCurrentUserId(): String {
        // Example implementation using Firebase Authentication
        val currentUser = FirebaseAuth.getInstance().currentUser
        return currentUser?.uid ?: ""
    }

    private fun loadFavoriteProducts() {
        db.collection("users").document(userId).collection("favorites")
            .get()
            .addOnSuccessListener { documents ->
                val productIds = documents.mapNotNull { it.getString("productId") }
                Log.d("FavoritesFragment", "Favorite product IDs: $productIds")
                fetchProducts(productIds)
            }
            .addOnFailureListener { exception ->
                Log.e("FavoritesFragment", "Error getting favorite products", exception)
                emptyTextView.visibility = View.VISIBLE
                recyclerViewFavorites.visibility = View.GONE
            }
    }

    private fun fetchProducts(productIds: List<String>) {
        if (productIds.isEmpty()) {
            checkEmptyState()
            return
        }

        favoriteProducts.clear()
        var productsFetched = 0
        val totalProducts = productIds.size
        for (productId in productIds) {
            if (productId.isNotBlank()) {
                fetchProductDetails(productId) { product ->
                    if (product != null) {
                        favoriteProducts.add(product)
                        fetchAndDisplayReviewsAndRating(product) {
                            productsFetched++
                            if (productsFetched == totalProducts) {
                                favoritesAdapter.notifyDataSetChanged()
                                checkEmptyState()
                            }
                        }
                    } else {
                        productsFetched++
                        if (productsFetched == totalProducts) {
                            favoritesAdapter.notifyDataSetChanged()
                            checkEmptyState()
                        }
                    }
                }
            } else {
                productsFetched++
                if (productsFetched == totalProducts) {
                    favoritesAdapter.notifyDataSetChanged()
                    checkEmptyState()
                }
            }
        }
    }

    private fun fetchProductDetails(productId: String, callback: (ProductDataClass?) -> Unit) {
        db.collection("products").document(productId)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val category = document.getString("category") ?: ""
                    val product = when (category) {
                        "Smartphones" -> document.toObject(Smartphones::class.java)
                        "Tablets" -> document.toObject(Tablets::class.java)
                        "TVs" -> document.toObject(TVs::class.java)
                        "Laptops" -> document.toObject(Laptops::class.java)
                        "SoftwareAccessories" -> document.toObject(SoftwareAccessories::class.java)
                        "VideoGames" -> document.toObject(VideoGames::class.java)
                        else -> null
                    }
                    product?.isFavorite = true
                    callback(product)
                } else {
                    callback(null)
                }
            }
            .addOnFailureListener { exception ->
                Log.e("FavoritesFragment", "Error getting product details", exception)
                callback(null)
            }
    }

    private fun fetchAndDisplayReviewsAndRating(productItem: ProductDataClass, callback: () -> Unit) {
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

                productItem.reviewRating = averageRating
                productItem.totalReviews = reviewCount

                callback() // Notify the caller that this product's data is fetched
            }
            .addOnFailureListener { e ->
                Log.e("Firestore", "Error fetching reviews", e)
                productItem.reviewRating = 0.0
                productItem.totalReviews = 0

                callback() // Notify the caller that this product's data is fetched
            }
    }

    override fun onFavoritesEmpty() {
        emptyTextView.visibility = View.VISIBLE
        recyclerViewFavorites.visibility = View.GONE
    }

    private fun checkEmptyState() {
        if (favoriteProducts.isEmpty()) {
            emptyTextView.visibility = View.VISIBLE
            recyclerViewFavorites.visibility = View.GONE
        } else {
            emptyTextView.visibility = View.GONE
            recyclerViewFavorites.visibility = View.VISIBLE
        }
    }
}
