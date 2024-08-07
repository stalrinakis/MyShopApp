package com.example.myshopapp.adapters

import android.content.Context
import android.graphics.Paint
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.myshopapp.R
import com.example.myshopapp.dataclasses.ProductDataClass
import com.example.myshopapp.fragments.CartFragment
import com.example.myshopapp.fragments.ProductDetailsFragment
import com.google.firebase.firestore.FirebaseFirestore

class CartAdapter(
    private val context: Context,
    private var cartItems: MutableList<ProductDataClass>,
    private val listener: CartFragment,
    private val emptyCartTextView: TextView,
    private val scrollViewCart: ScrollView,
    private val userId: String
) : RecyclerView.Adapter<CartAdapter.CartViewHolder>() {

    // Firestore instance and user ID
    private val db = FirebaseFirestore.getInstance()
    private var userFavorites = mutableSetOf<String>()


    init {
        fetchUserFavorites()
    }
//fetch favorites
    private fun fetchUserFavorites() {
        db.collection("users").document(userId).collection("favorites")
            .get()
            .addOnSuccessListener { documents ->
                userFavorites.clear()
                for (document in documents) {
                    val productId = document.getString("productId")
                    productId?.let { userFavorites.add(it) }
                }
                notifyDataSetChanged()
            }
            .addOnFailureListener { e ->
                Log.e("Firestore", "Error fetching user favorites", e)
            }
    }

    inner class CartViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val imageViewProduct: ImageView = itemView.findViewById(R.id.imageViewCart)
        private val textViewProductTitle: TextView = itemView.findViewById(R.id.tvTitleCart)
        private val textViewPrice1: TextView = itemView.findViewById(R.id.tvPrice)
        private val textViewPrice2: TextView = itemView.findViewById(R.id.tvTotalPrice)
        private val imageViewWishHeart: ImageView = itemView.findViewById(R.id.imageViewWishHeartCart)
        private val buttonMinus: Button = itemView.findViewById(R.id.buttonMinus)
        private val buttonPlus: Button = itemView.findViewById(R.id.buttonPlus)
        private val quantityTextView: TextView = itemView.findViewById(R.id.tvQuantity)
        private val trashcan: ImageView = itemView.findViewById(R.id.trashcan)

        fun bind(productItem: ProductDataClass) {
            Glide.with(itemView)
                .load(productItem.imageResId)  // Assuming productItem.imageResId is a valid URL or drawable resource id
                .placeholder(R.drawable.ic_launcher_foreground) // Placeholder image while loading
                .error(R.drawable.trashcan) // Image to show if loading fails
                .into(imageViewProduct)

            textViewProductTitle.text = productItem.title
            textViewPrice1.text = productItem.price1.toString()
            textViewPrice1.paintFlags = textViewPrice1.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
            textViewPrice2.text = productItem.price2.toString()
            quantityTextView.text = productItem.quantity.toString()


            //change heart if favorites
            if (userFavorites.contains(productItem.productId)) {
                imageViewWishHeart.setImageResource(R.drawable.heart)
                imageViewWishHeart.setColorFilter(ContextCompat.getColor(context, R.color.red))
            } else {
                imageViewWishHeart.setImageResource(R.drawable.wish_heart)
                imageViewWishHeart.setColorFilter(ContextCompat.getColor(context, R.color.black))
            }

            imageViewWishHeart.setOnClickListener {
                if (userFavorites.contains(productItem.productId)) {
                    removeFromFavorites(productItem)
                } else {
                    addToFavorites(productItem)
                }
            }
                //product details fragment on click
            itemView.setOnClickListener {
                val fragment = ProductDetailsFragment()
                val args = Bundle()
                args.putInt("imageResId", productItem.imageResId)
                args.putString("title", productItem.title)
                args.putFloat("price1", productItem.price1)
                args.putFloat("price2", productItem.price2)
                args.putString("productId", productItem.productId)
                fragment.arguments = args

                val transaction = (context as AppCompatActivity).supportFragmentManager.beginTransaction()
                transaction.replace(R.id.fragment_container, fragment)
                transaction.addToBackStack(null)
                transaction.commit()
            }

            buttonMinus.setOnClickListener {
                if (productItem.quantity == 1) {
                    showRemoveItemDialog(productItem, adapterPosition)
                } else {
                    decreaseQuantity(productItem, quantityTextView)
                }
            }

            buttonPlus.setOnClickListener {
                if (productItem.quantity < productItem.availableQuantity) {
                    increaseQuantity(productItem, quantityTextView)
                } else {
                    Toast.makeText(context, R.string.exceed, Toast.LENGTH_SHORT).show()
                }
            }

            trashcan.setOnClickListener {
                showRemoveItemDialog(productItem, adapterPosition)
            }
        }

        private fun showRemoveItemDialog(productItem: ProductDataClass, position: Int) {
            AlertDialog.Builder(context).apply {
                setTitle("Remove Item")
                setMessage("Do you want to remove ${productItem.title} from the cart?")
                setNegativeButton("No", null)
                setPositiveButton("Yes") { _, _ ->
                    removeFromCart(productItem, position)
                }
            }.show()
        }

        private fun removeFromCart(productItem: ProductDataClass, position: Int) {
            db.collection("users").document(userId).collection("cart")
                .document(productItem.productId)
                .delete()
                .addOnSuccessListener {
                    cartItems.removeAt(position)
                    notifyItemRemoved(position)
                    notifyItemRangeChanged(position, cartItems.size)
                    Log.d("Firestore", "Item removed from cart: ${productItem.title}")
                    checkIfCartIsEmpty()
                    listener.onQuantityChanged() // Notify the listener when an item is removed
                }
                .addOnFailureListener { e ->
                    Log.e("Firestore", "Error removing item from cart", e)
                }
        }

        private fun checkIfCartIsEmpty() {
            if (cartItems.isEmpty()) {
                emptyCartTextView.visibility = View.VISIBLE
                scrollViewCart.visibility = View.GONE
                Toast.makeText(context, R.string.empty_cart, Toast.LENGTH_SHORT).show()
            }
        }

        private fun increaseQuantity(productItem: ProductDataClass, quantityTextView: TextView) {
            if (productItem.quantity < productItem.availableQuantity) {
                productItem.quantity++
                quantityTextView.text = productItem.quantity.toString()
                updateProductQuantityInFirestore(productItem)
                listener.onQuantityChanged() // Notify the listener when quantity is increased
            } else {
                Toast.makeText(context, R.string.exceed, Toast.LENGTH_SHORT).show()
            }
        }

        private fun decreaseQuantity(productItem: ProductDataClass, quantityTextView: TextView) {
            if (productItem.quantity > 1) {
                productItem.quantity--
                quantityTextView.text = productItem.quantity.toString()
                updateProductQuantityInFirestore(productItem)
                listener.onQuantityChanged() // Notify the listener when quantity is decreased
            }
        }
        //USERSSSS
        private fun updateProductQuantityInFirestore(productItem: ProductDataClass) {
            db.collection("users").document(userId).collection("cart")
                .document(productItem.productId)
                .update("quantity", productItem.quantity)
                .addOnSuccessListener {
                    Log.d("Firestore", "Quantity updated for ${productItem.title}")
                }
                .addOnFailureListener { e ->
                    Log.e("Firestore", "Error updating quantity", e)
                }
        }

        private fun addToFavorites(productItem: ProductDataClass) {
            val favoriteData = hashMapOf("productId" to productItem.productId)
            db.collection("users").document(userId).collection("favorites")
                .document(productItem.productId)
                .set(favoriteData)
                .addOnSuccessListener {
                    userFavorites.add(productItem.productId)
                    notifyItemChanged(adapterPosition)
                    Log.d("Firestore", "Added to favorites: ${productItem.title}")
                }
                .addOnFailureListener { e ->
                    Log.e("Firestore", "Error adding to favorites", e)
                }
        }

        private fun removeFromFavorites(productItem: ProductDataClass) {
            db.collection("users").document(userId).collection("favorites")
                .document(productItem.productId)
                .delete()
                .addOnSuccessListener {
                    userFavorites.remove(productItem.productId)
                    notifyItemChanged(adapterPosition)
                    Log.d("Firestore", "Removed from favorites: ${productItem.title}")
                }
                .addOnFailureListener { e ->
                    Log.e("Firestore", "Error removing from favorites", e)
                }
        }
    }

    //recycleview
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CartViewHolder {
        val itemView = LayoutInflater.from(context).inflate(R.layout.viewholder_cart, parent, false)
        return CartViewHolder(itemView)
    }

    //display data
    override fun onBindViewHolder(holder: CartViewHolder, position: Int) {
        val productItem = cartItems[position]
        holder.bind(productItem)
    }

    //total items
    override fun getItemCount(): Int {
        return cartItems.size
    }

    interface OnCartInteractionListener {
        fun onQuantityChanged()
    }
}
