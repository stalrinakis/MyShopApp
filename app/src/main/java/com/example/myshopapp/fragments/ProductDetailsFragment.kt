package com.example.myshopapp.fragments

import android.app.AlertDialog
import android.graphics.Paint
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myshopapp.R
import com.example.myshopapp.adapters.ReviewAdapter
import com.example.myshopapp.dataclasses.ReviewDataClass
import com.google.android.material.button.MaterialButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class ProductDetailsFragment : Fragment() {

    private lateinit var recyclerViewReviews: RecyclerView
    private lateinit var reviewAdapter: ReviewAdapter
    private lateinit var reviewsList: MutableList<ReviewDataClass>

    // Initialize Firestore
    private val db = FirebaseFirestore.getInstance()

    // Firebase Auth
    private lateinit var auth: FirebaseAuth
    private lateinit var currentUserUid: String

    private val userFavorites = mutableSetOf<String>()
    private val userCart = mutableSetOf<String>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_product_details, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        auth = FirebaseAuth.getInstance()
        currentUserUid = auth.currentUser?.uid ?: ""
        Log.d("t1", currentUserUid)

        // Initialize views
        val textViewReviews: TextView = view.findViewById(R.id.textViewReviews)
        val imageViewDetailsProduct: ImageView = view.findViewById(R.id.imageViewDetailsProduct)
        val textViewDetailsProductTitle: TextView = view.findViewById(R.id.textViewDetailsProductTitle)
        val textViewDetailsPrice1: TextView = view.findViewById(R.id.textViewDetailsPrice1)
        val textViewDetailsPrice2: TextView = view.findViewById(R.id.textViewDetailsPrice2)
        val textViewDetailsRatingText: TextView = view.findViewById(R.id.textViewDetailsRatingText)
        val imageViewDetailsArrow: ImageView = view.findViewById(R.id.imageViewDetailsArrow)
        val textViewDetailsReviewsList: TextView = view.findViewById(R.id.textViewDetailsReviewsList)
        val buttonDetailsDescription: MaterialButton = view.findViewById(R.id.buttonDetailsDescription)
        val buttonDetailsReviews: MaterialButton = view.findViewById(R.id.buttonDetailsReviews)
        val buttonAddToCart: Button = view.findViewById(R.id.buttonAddToCart)
        val imageViewWishHeart: ImageView = view.findViewById(R.id.imageViewDetailsWish_Heart)
        val buttonLeaveReview: Button = view.findViewById(R.id.buttonLeaveReview)
        val textViewDetailsFullDescription: TextView = view.findViewById(R.id.textViewDetailsFullDescription)
        recyclerViewReviews = view.findViewById(R.id.recyclerViewReviews)
        val quantityLeft: TextView = view.findViewById(R.id.tvQuantityNumber)

        // Setup RecyclerView for reviews
        reviewsList = mutableListOf()
        reviewAdapter = ReviewAdapter(reviewsList)
        recyclerViewReviews.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = reviewAdapter
        }

        // Set default visibility
        textViewDetailsFullDescription.visibility = View.VISIBLE
        textViewDetailsReviewsList.visibility = View.GONE
        recyclerViewReviews.visibility = View.GONE

        // Retrieve arguments
        val args = arguments
        val imageResId = args?.getString("imageResId") ?: ""
        val title = args?.getString("title")
        val price1 = args?.getFloat("price1").toString()
        val price2 = args?.getFloat("price2").toString()
        val starRating = args?.getDouble("reviewRating")
        val totalReviews = args?.getInt("totalReviews")
        val productId = args?.getString("productId")
        val totalprod = args?.getInt("tvQuantityNumber")
        val description = args?.getString("description")

        // Log the productId for debugging
        Log.d("ProductDetailsFragment", "Product ID: $productId")

        // Set data to views
        val resId = requireContext().resources.getIdentifier(imageResId, "drawable", requireContext().packageName)
        imageViewDetailsProduct.setImageResource(resId)
        textViewDetailsProductTitle.text = title
        textViewDetailsPrice1.text = price1
        textViewDetailsPrice1.paintFlags = textViewDetailsPrice1.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
        textViewDetailsPrice2.text = price2
        textViewDetailsFullDescription.text = description
        textViewDetailsRatingText.text = String.format("%.1f", starRating)

        textViewReviews.text = totalReviews.toString()
        quantityLeft.text = totalprod.toString()

        // Set OnClickListener to the back arrow ImageView
        imageViewDetailsArrow.setOnClickListener {
            requireActivity().supportFragmentManager.popBackStack()
        }

        // Set OnClickListener to the Description Button
        buttonDetailsDescription.setOnClickListener {
            textViewDetailsFullDescription.visibility = View.VISIBLE
            textViewDetailsReviewsList.visibility = View.GONE
            recyclerViewReviews.visibility = View.GONE
        }

        // Set OnClickListener to the Reviews Button
        buttonDetailsReviews.setOnClickListener {
            textViewDetailsFullDescription.visibility = View.GONE
            textViewDetailsReviewsList.visibility = View.VISIBLE
            recyclerViewReviews.visibility = View.VISIBLE
        }

        // Set OnClickListener to the Add to Cart Button
        buttonAddToCart.setOnClickListener {
            if (productId != null) {
                if (userCart.contains(productId)) {
                    removeFromCart(productId)
                } else {
                    addToCart(productId)
                }
            }
        }

        imageViewWishHeart.setOnClickListener {
            Log.d("ProductDetailsFragment", "Wish heart clicked")
            if (productId != null) {
                if (userFavorites.contains(productId)) {
                    removeFromFavorites(productId)
                } else {
                    addToFavorites(productId)
                }
            } else {
                Log.e("ProductDetailsFragment", "Product ID is null")
            }
        }

        // Set OnClickListener to the Leave Review Button
        buttonLeaveReview.setOnClickListener {
            showLeaveReviewDialog(productId)
        }

        // Fetch user favorites and cart, update icons accordingly
        fetchUserFavorites()
        fetchUserCart()

        // Fetch product reviews
        fetchReviews(productId)

        // Add dynamic product details based on productId
        if (productId != null) {
            addProductDetails(productId, textViewDetailsFullDescription, description)
        }
    }

    private fun addProductDetails(
        productId: String,
        textViewDetailsFullDescription: TextView,
        description: String?
    ) {
        // Fetch additional product details based on productId
        db.collection("products").document(productId)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val productCategory = document.getString("category")
                    when (productCategory) {
                        "Smartphones" -> {
                            val screenSize = document.getString("screenSize") ?: ""
                            val ramSize = document.getString("ramSize") ?: ""
                            val color = document.getString("color") ?: ""
                            val storage = document.getString("storage") ?: ""
                            val operatingSystem = document.getString("operatingSystem") ?: ""

                            val details = "Screen Size: $screenSize\n" +
                                    "RAM Size: $ramSize\n" +
                                    "Color: $color\n" +
                                    "Storage: $storage\n" +
                                    "Operating System: $operatingSystem\n"

                            textViewDetailsFullDescription.text = description + "\n" + details

                        }
                        "Tablets" -> {
                            val screenSize = document.getString("screenSize") ?: ""
                            val storage = document.getString("storage") ?: ""
                            val ramSize = document.getString("ramSize") ?: ""
                            val operatingSystem = document.getString("operatingSystem") ?: ""
                            val color = document.getString("color") ?: ""

                            val details = "Screen Size: $screenSize\n" +
                                    "Storage: $storage\n" +
                                    "RAM Size: $ramSize\n" +
                                    "Operating System: $operatingSystem\n" +
                                    "Color: $color\n"

                            textViewDetailsFullDescription.text = description + "\n" + details
                        }
                        "TVs" -> {
                            val screenSize = document.getString("screenSize") ?: ""
                            val resolution = document.getString("resolution") ?: ""
                            val smartTV = document.getBoolean("smartTV") ?: false
                            val color = document.getString("color") ?: ""

                            val details = "Screen Size: $screenSize\n" +
                                    "Resolution: $resolution\n" +
                                    "Smart TV: ${if (smartTV) "Yes" else "No"}\n" +
                                    "Color: $color\n"

                            textViewDetailsFullDescription.text = description + "\n" + details
                        }
                        "Laptops" -> {
                            val screenSize = document.getString("screenSize") ?: ""
                            val resolution = document.getString("resolution") ?: ""
                            val ramSize = document.getString("ramSize") ?: ""
                            val storage = document.getString("storage") ?: ""
                            val operatingSystem = document.getString("operatingSystem") ?: ""
                            val color = document.getString("color") ?: ""

                            val details = "Screen Size: $screenSize\n" +
                                    "Resolution: $resolution\n" +
                                    "RAM Size: $ramSize\n" +
                                    "Storage: $storage\n" +
                                    "Operating System: $operatingSystem\n" +
                                    "Color: $color\n"

                            textViewDetailsFullDescription.text = description + "\n" + details
                        }
                        else -> {
                            textViewDetailsFullDescription.text = description
                        }
                    }
                } else {
                    Log.d("ProductDetailsFragment", "No such document")
                }
            }
            .addOnFailureListener { exception ->
                Log.d("ProductDetailsFragment", "get failed with ", exception)
            }
    }

    private fun fetchReviews(productId: String?) {
        if (productId == null) return
        db.collection("products").document(productId).collection("reviews")
            .get()
            .addOnSuccessListener { documents ->
                reviewsList.clear()
                for (document in documents) {
                    val reviewerName = document.getString("reviewerName") ?: "Anonymous"
                    val rating = document.getDouble("rating")?.toFloat() ?: 0f
                    Log.d("test1234", rating.toString())
                    val reviewText = document.getString("reviewText") ?: ""
                    reviewsList.add(ReviewDataClass(reviewerName, rating, reviewText))
                }
                reviewAdapter.notifyDataSetChanged()
            }
            .addOnFailureListener { e ->
                Log.e("Firestore", "Error fetching reviews", e)
                Toast.makeText(requireContext(), "${R.string.error_fetching_reviews}: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun showLeaveReviewDialog(productId: String?) {
        if (productId == null) return

        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.leave_review, null)
        val ratingBar: RatingBar = dialogView.findViewById(R.id.ratingBar)
        val editTextReview: EditText = dialogView.findViewById(R.id.editTextReview)

        AlertDialog.Builder(requireContext())
            .setTitle(R.string.write_review)
            .setView(dialogView)
            .setPositiveButton(R.string.submit) { _, _ ->
                val rating = ratingBar.rating
                val reviewText = editTextReview.text.toString()
                submitReview(productId, rating, reviewText)
            }
            .setNegativeButton(R.string.cancel, null)
            .create()
            .show()
    }

    private fun submitReview(productId: String, rating: Float, reviewText: String) {
        val review = hashMapOf(
            "reviewerName" to currentUserUid, // Store current user's ID
            "rating" to rating,
            "reviewText" to reviewText
        )

        db.collection("products").document(productId).collection("reviews").add(review)
            .addOnSuccessListener {
                Toast.makeText(requireContext(), R.string.review_submiteed, Toast.LENGTH_SHORT).show()
                fetchReviews(productId)
            }
            .addOnFailureListener { e ->
                Log.e("Firestore", "Error submitting review", e)
                Toast.makeText(requireContext(), "${R.string.review_submiteed_error}: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun fetchUserCart() {
        db.collection("users").document(currentUserUid).collection("cart")
            .get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    val productId = document.getString("productId")
                    if (productId != null) {
                        userCart.add(productId)
                    }
                }
                updateCartIcon()
            }
            .addOnFailureListener { e ->
                Log.e("Firestore", "Error fetching cart", e)
                Toast.makeText(requireContext(), "${R.string.fetching_cart_error}: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun updateCartIcon() {
        val buttonAddToCart: Button = view?.findViewById(R.id.buttonAddToCart) ?: return
        val productId = arguments?.getString("productId")
        if (productId != null && userCart.contains(productId)) {
            buttonAddToCart.text = getString(R.string.remove_from_cart)
        } else {
            buttonAddToCart.text = getString(R.string.add_to_cart)
        }
    }

    private fun addToCart(productId: String) {
        val cartItem = hashMapOf("productId" to productId)
        db.collection("users").document(currentUserUid).collection("cart").document(productId)
            .set(cartItem)
            .addOnSuccessListener {
                userCart.add(productId)
                updateCartIcon()
                Toast.makeText(requireContext(), R.string.added_to_cart, Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Log.e("Firestore", "Error adding to cart", e)
                Toast.makeText(requireContext(), "${R.string.error_add_cart}: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun removeFromCart(productId: String) {
        db.collection("users").document(currentUserUid).collection("cart").document(productId)
            .delete()
            .addOnSuccessListener {
                userCart.remove(productId)
                updateCartIcon()
                Toast.makeText(requireContext(), R.string.remove_from_cart, Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Log.e("Firestore", "Error removing from cart", e)
                Toast.makeText(requireContext(), "${R.string.error_remove_cart}: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun fetchUserFavorites() {
        db.collection("users").document(currentUserUid).collection("favorites")
            .get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    val productId = document.getString("productId")
                    if (productId != null) {
                        userFavorites.add(productId)
                    }
                }
                updateHeartIcon()
            }
            .addOnFailureListener { e ->
                Log.e("Firestore", "Error fetching favorites", e)
                Toast.makeText(requireContext(), "${R.string.error_fetching_fav}: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun updateHeartIcon() {
        val imageViewWishHeart: ImageView = view?.findViewById(R.id.imageViewDetailsWish_Heart) ?: return
        val productId = arguments?.getString("productId")
        if (productId != null && userFavorites.contains(productId)) {
            imageViewWishHeart.setImageResource(R.drawable.heart)
            imageViewWishHeart.setColorFilter(ContextCompat.getColor(requireContext(), R.color.red))
        } else {
            imageViewWishHeart.setImageResource(R.drawable.wish_heart)
            imageViewWishHeart.setColorFilter(ContextCompat.getColor(requireContext(), R.color.black))
        }
    }

    private fun addToFavorites(productId: String) {
        val favorite = hashMapOf("productId" to productId)
        db.collection("users").document(currentUserUid).collection("favorites").document(productId)
            .set(favorite)
            .addOnSuccessListener {
                userFavorites.add(productId)
                updateHeartIcon()
                Toast.makeText(requireContext(), R.string.add_to_fav, Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Log.e("Firestore", "Error adding to favorites", e)
                Toast.makeText(requireContext(), "${R.string.error_adding_wish}: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun removeFromFavorites(productId: String) {
        db.collection("users").document(currentUserUid).collection("favorites").document(productId)
            .delete()
            .addOnSuccessListener {
                userFavorites.remove(productId)
                updateHeartIcon()
                Toast.makeText(requireContext(), R.string.removed_favorites, Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Log.e("Firestore", "Error removing from favorites", e)
                Toast.makeText(requireContext(), "${R.string.fav_rem_failed}: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}
