import android.content.Context
import android.graphics.Paint
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.myshopapp.R
import com.example.myshopapp.dataclasses.Product

data class OrderSection(
    val orderId: String,
    val products: List<Product>
)

class OrdersAdapter(private val context: Context) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var sections = mutableListOf<OrderSection>()
    private var isEmptyState = false
    private var totalPrice: Double = 0.0 // Variable to hold total price

    fun submitList(orders: List<OrderSection>) {
        sections.clear()
        sections.addAll(orders)
        isEmptyState = sections.isEmpty()
        calculateTotalPrice() // Calculate total price when data is updated
        notifyDataSetChanged()
    }
    // Calculate total price of all orders
    private fun calculateTotalPrice() {
        totalPrice = sections.sumByDouble { section ->
            section.products.sumByDouble { it.price2 }
        }
    }

    // Method to get total price
    fun getTotalPrice(): Double {
        return totalPrice
    }

    override fun getItemViewType(position: Int): Int {
        return if (isEmptyState) {
            VIEW_TYPE_EMPTY_STATE
        } else {
            var count = 0
            for (section in sections) {
                if (position == count) {
                    return VIEW_TYPE_ORDER_ID
                }
                count += 1 + section.products.size
                if (position < count) {
                    return VIEW_TYPE_PRODUCT
                }
            }
            throw IllegalStateException("Invalid view type for position $position")
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            VIEW_TYPE_ORDER_ID -> {
                val view = LayoutInflater.from(parent.context).inflate(R.layout.item_order, parent, false)
                OrderIdViewHolder(view)
            }
            VIEW_TYPE_PRODUCT -> {
                val view = LayoutInflater.from(parent.context).inflate(R.layout.item_product, parent, false)
                ProductViewHolder(view)
            }
            VIEW_TYPE_EMPTY_STATE -> {
                val view = LayoutInflater.from(parent.context).inflate(R.layout.item_empty_state, parent, false)
                EmptyStateViewHolder(view)
            }
            else -> throw IllegalArgumentException("Unknown view type: $viewType")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        var count = 0
        for (section in sections) {
            if (position == count) {
                if (holder is OrderIdViewHolder) {
                    holder.bind(section.orderId)
                }
                return
            }
            count++ // Increment count for order ID

            // Check if position is within the range of products for this section
            if (position < count + section.products.size) {
                if (holder is ProductViewHolder) {
                    val productIndex = position - count
                    holder.bind(section.products[productIndex])
                }
                return
            }
            count += section.products.size // Increment count for products
        }
    }



    override fun getItemCount(): Int {
        return if (isEmptyState) {
            1 // Show empty state
        } else {
            var count = 0
            for (section in sections) {
                count += 1 + section.products.size
            }
            count
        }
    }

    inner class OrderIdViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val orderIdTextView: TextView = itemView.findViewById(R.id.textViewOrderId)

        fun bind(orderId: String) {
            orderIdTextView.text = context.getString(R.string.orderID) + orderId
        }
    }

    inner class ProductViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val productImage: ImageView = itemView.findViewById(R.id.imageViewProduct)
        private val productTitle: TextView = itemView.findViewById(R.id.textViewProductTitle)
        private val productPrice1: TextView = itemView.findViewById(R.id.textViewPrice1)
        private val productPrice2: TextView = itemView.findViewById(R.id.textViewPrice2)

        fun bind(product: Product) {
            productTitle.text = product.title.toString()
            productPrice1.text = product.price1.toFloat().toString()
            productPrice1.paintFlags = productPrice1.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
            productPrice2.text = product.price2.toFloat().toString()





            // Load image with Glide
            Glide.with(itemView)
                .load(product.imageResId)  // Assuming productItem.imageResId is a valid URL or drawable resource id
                .placeholder(R.drawable.ic_launcher_foreground) // Placeholder image while loading
                .error(R.drawable.trashcan) // Image to show if loading fails
                .into(productImage)
        }
    }

    inner class EmptyStateViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val emptyStateTextView: TextView = itemView.findViewById(R.id.textViewEmptyState)

        fun bind() {
            emptyStateTextView.text = context.getString(R.string.no_orders_message)
        }
    }

    companion object {
        private const val VIEW_TYPE_ORDER_ID = 0
        private const val VIEW_TYPE_PRODUCT = 1
        private const val VIEW_TYPE_EMPTY_STATE = 2
    }
}
