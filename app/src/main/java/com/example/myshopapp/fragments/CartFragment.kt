package com.example.myshopapp.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.RadioGroup
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myshopapp.R
import com.example.myshopapp.adapters.CartAdapter
import com.example.myshopapp.dataclasses.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.math.RoundingMode

class CartFragment : Fragment(), CartAdapter.OnCartInteractionListener {

    private lateinit var recyclerViewCart: RecyclerView
    private lateinit var cartAdapter: CartAdapter
    private lateinit var emptyCartTextView: TextView
    private lateinit var etCoupon: EditText
    private lateinit var applyCouponButton: Button
    private lateinit var tvTotalProductsPrice: TextView
    private lateinit var tvCouponDiscountsPrice: TextView
    private lateinit var tvTotalPrice: TextView
    private lateinit var tvTotalProductsCount: TextView
    private lateinit var checkoutButton: Button
    private lateinit var scrollViewCart: ScrollView
    private lateinit var payment: ConstraintLayout
    private var couponApplied = false
    private lateinit var userId: String

    private val cartItems = mutableListOf<ProductDataClass>()

    private val db = FirebaseFirestore.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val view = inflater.inflate(R.layout.fragment_cart, container, false)
        val imageViewDetailsArrow: ImageView = view.findViewById(R.id.imageViewArrow)
        recyclerViewCart = view.findViewById(R.id.CartView)
        emptyCartTextView = view.findViewById(R.id.tvEmptyCart)
        etCoupon = view.findViewById(R.id.etCoupon)
        applyCouponButton = view.findViewById(R.id.CouponButton)
        tvTotalProductsPrice = view.findViewById(R.id.tvTotalProductsPrice)
        tvCouponDiscountsPrice = view.findViewById(R.id.tvCouponDiscountsPrice)
        tvTotalPrice = view.findViewById(R.id.tvTotalPrice)
        tvTotalProductsCount = view.findViewById(R.id.tvTotalProductsCount)
        checkoutButton = view.findViewById(R.id.CheckOut)
        scrollViewCart = view.findViewById(R.id.scrollViewCart)
        payment = view.findViewById(R.id.Payment)
        userId = getCurrentUserId()
        recyclerViewCart.layoutManager = LinearLayoutManager(requireContext())
        cartAdapter = CartAdapter(requireContext(), cartItems, this, emptyCartTextView , scrollViewCart,userId )
        recyclerViewCart.adapter = cartAdapter

        loadCartProducts()

        applyCouponButton.setOnClickListener {
            if (etCoupon.text.toString() == "DISCOUNT24") {
                couponApplied = true
                updateTotalPrice()
            }
        }

        imageViewDetailsArrow.setOnClickListener {
            requireActivity().supportFragmentManager.popBackStack()
        }

        checkoutButton.setOnClickListener {
            showPayment()
        }

        val btnCompleteOrder: Button = view.findViewById(R.id.btnCompleteOrder)
        btnCompleteOrder.setOnClickListener {
            completeOrder()
        }

        return view
    }

    private fun getCurrentUserId(): String {
        // Example implementation using Firebase Authentication
        val currentUser = FirebaseAuth.getInstance().currentUser
        return currentUser?.uid ?: ""
    }


    private fun completeOrder() {
        val address = view?.findViewById<EditText>(R.id.etAddress)?.text.toString()
        val paymentMethod = when (view?.findViewById<RadioGroup>(R.id.rgPaymentMethods)?.checkedRadioButtonId) {
            R.id.rbCard -> "Card"
            R.id.rbCash -> "Cash on Delivery"
            else -> ""
        }

        if (address.isBlank() || paymentMethod.isBlank()) {
            Toast.makeText(requireContext(), R.string.address_payment, Toast.LENGTH_SHORT).show()
            return
        }

        val order = hashMapOf(
            "userId" to userId,
            "address" to address,
            "paymentMethod" to paymentMethod,
            "products" to cartItems,
            "totalPrice" to tvTotalPrice.text.toString()
        )

        db.collection("users").document(userId).collection("orders")
            .add(order)
            .addOnSuccessListener {
                Toast.makeText(requireContext(), R.string.successful_order, Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), R.string.failed_order, Toast.LENGTH_SHORT).show()
            }

        db.collection("orders")
            .add(order)
            .addOnSuccessListener {
                Toast.makeText(requireContext(), R.string.successful_order, Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), R.string.failed_order, Toast.LENGTH_SHORT).show()
            }
    }

    private fun showPayment() {
        checkoutButton.visibility = View.GONE
        payment.visibility = View.VISIBLE
    }

    private fun loadCartProducts() {
        db.collection("users").document(userId).collection("cart")
            .get()
            .addOnSuccessListener { documents ->
                val productIds = documents.mapNotNull { it.getString("productId") }
                Log.d("CartFragment", "Cart product IDs: $productIds")
                fetchProducts(productIds)
            }
            .addOnFailureListener { exception ->
                Log.e("CartFragment", "Error getting cart products", exception)
                emptyCartTextView.visibility = View.VISIBLE
                scrollViewCart.visibility = View.GONE
            }
    }

    private fun fetchProducts(productIds: List<String>) {
        if (productIds.isEmpty()) {
            checkEmptyState()
            return
        }

        cartItems.clear()
        var productsFetched = 0
        val totalProducts = productIds.size
        for (productId in productIds) {
            if (productId.isNotBlank()) {
                fetchProductDetails(productId) { product ->
                    if (product != null) {
                        cartItems.add(product)
                        productsFetched++
                        if (productsFetched == totalProducts) {
                            updateTotalPrice()
                            cartAdapter.notifyDataSetChanged()
                            checkEmptyState()
                        }
                    } else {
                        productsFetched++
                        if (productsFetched == totalProducts) {
                            cartAdapter.notifyDataSetChanged()
                            checkEmptyState()
                        }
                    }
                    updateTotalPrice()
                }
            } else {
                productsFetched++
                if (productsFetched == totalProducts) {
                    cartAdapter.notifyDataSetChanged()
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
                Log.e("CartFragment", "Error getting product details", exception)
                callback(null)
            }
    }

    private fun checkEmptyState() {
        if (cartItems.isEmpty()) {
            emptyCartTextView.visibility = View.VISIBLE
            scrollViewCart.visibility = View.GONE
        } else {
            emptyCartTextView.visibility = View.GONE
            scrollViewCart.visibility = View.VISIBLE
        }
    }

    private fun updateTotalPrice() {
        var totalPrice = 0.0
        var itemsCount = 0
        var couponDiscount = 0.0

        cartItems.forEach { item ->
            totalPrice += item.price2 * item.quantity
            itemsCount += item.quantity
        }

        if (couponApplied) {
            couponDiscount = (totalPrice * 0.24).toBigDecimal().setScale(2, RoundingMode.HALF_EVEN).toDouble()
        }

        val finalPrice: Double = (totalPrice - couponDiscount).toBigDecimal().setScale(2, RoundingMode.HALF_EVEN).toDouble()
        totalPrice = totalPrice.toBigDecimal().setScale(2, RoundingMode.HALF_EVEN).toDouble()
        tvTotalProductsCount.text = itemsCount.toString()
        tvTotalProductsPrice.text = "$${totalPrice}"
        tvTotalPrice.text = "$${finalPrice}"
        tvCouponDiscountsPrice.text = "-$${couponDiscount}"

    }

    override fun onQuantityChanged() {
        updateTotalPrice()
    }

    override fun onResume() {
        super.onResume()
        updateTotalPrice()
    }
}
