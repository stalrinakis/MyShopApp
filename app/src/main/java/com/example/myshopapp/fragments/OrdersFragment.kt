import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myshopapp.R
import com.example.myshopapp.dataclasses.Product
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class OrdersFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: OrdersAdapter
    private lateinit var tvTotalPrice: TextView
    private lateinit var currentUserUid: String

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.order_list, container, false)

        recyclerView = view.findViewById(R.id.CartViews)
        recyclerView.layoutManager = LinearLayoutManager(context)
        adapter = OrdersAdapter(requireContext())
        recyclerView.adapter = adapter

        tvTotalPrice = view.findViewById(R.id.textViewTotalPrice)

        // Get current user ID
        currentUserUid = FirebaseAuth.getInstance().currentUser?.uid ?: ""

        if (currentUserUid.isNotEmpty()) {
            fetchUserOrders(currentUserUid)
        } else {
            Log.e("OrdersFragment", "Current user UID is empty")
            // Handle the case where current user UID is empty or user is not authenticated
        }

        return view
    }

    private fun fetchUserOrders(userId: String) {
        val db = FirebaseFirestore.getInstance()
        db.collection("users").document(userId).collection("orders")
            .get()
            .addOnSuccessListener { querySnapshot ->
                val orders = mutableListOf<OrderSection>()

                for (document in querySnapshot.documents) {
                    val orderId = document.id
                    val data = document.data
                    val productsList = data?.get("products") as? List<Map<String, Any>>

                    val products = productsList?.map { productData ->
                        val title = productData["title"] as? String ?: ""
                        val price1 = (productData["price1"] as? Double) ?: 0.0
                        val price2 = (productData["price2"] as? Double) ?: 0.0
                        val productId = productData["productId"] as? String ?: ""
                        val imageResId = (productData["imageResId"] as? Int) ?: 0

                        Product(
                            title = title,
                            price1 = price1,
                            price2 = price2,
                            productId = productId,
                            imageResId = imageResId
                        )
                    } ?: emptyList()

                    orders.add(OrderSection(orderId, products))
                }

                displayUserOrders(orders)
            }
            .addOnFailureListener { e ->
                Log.e("OrdersFragment", "Error fetching orders", e)
                // Handle failure, show error message, etc.
            }
    }

    private fun displayUserOrders(orders: List<OrderSection>) {
        if (orders.isEmpty()) {
            val emptyStateOrder = listOf(OrderSection("", emptyList()))
            adapter.submitList(emptyStateOrder)
        } else {
            adapter.submitList(orders)
        }

        // Update total price after displaying orders
        val totalPrice = adapter.getTotalPrice()
        tvTotalPrice.text = String.format("Total Price: %.2f", totalPrice)

    }
}
