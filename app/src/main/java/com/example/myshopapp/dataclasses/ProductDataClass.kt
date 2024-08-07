package com.example.myshopapp.dataclasses

sealed class ProductDataClass(
    open val title: String = "",
    open val imageResId: Int = 0,
    open val price1: Float = 0f,
    open val price2: Float = 0f,
    open val category: String = "",
    open val description: String = "",
    open val productId: String = "",
    open var reviewRating: Double = 0.0,
    open var totalReviews: Int = 0,
    open var isFavorite: Boolean = false,
    open var quantity: Int = 0,
    open var availableQuantity: Int = 0
)

data class Smartphones(
    override val title: String = "",
    override val imageResId: Int = 0,
    override val price1: Float = 0f,
    override val price2: Float = 0f,
    override val category: String = "Smartphones",
    override val description: String = "",
    override val productId: String = "",
    override var reviewRating: Double = 0.0,
    override var totalReviews: Int = 0,
    override var isFavorite: Boolean = false,
    override var quantity: Int = 0,
    override var availableQuantity: Int = 0,
    val ramSize: String = "",
    val color: String = "",
    val screenSize: String = "",
    val storage: String = "",
    val operatingSystem: String = "",
) : ProductDataClass(title, imageResId, price1, price2, category, description, productId, reviewRating, totalReviews, isFavorite, quantity, availableQuantity)

data class Tablets(
    override val title: String = "",
    override val imageResId: Int = 0,
    override val price1: Float = 0f,
    override val price2: Float = 0f,
    override val category: String = "Tablets",
    override val description: String = "",
    override val productId: String = "",
    override var reviewRating: Double = 0.0,
    override var totalReviews: Int = 0,
    override var isFavorite: Boolean = false,
    override var quantity: Int = 0,
    override var availableQuantity: Int = 0,
    val ramSize: String = "",
    val color: String = "",
    val screenSize: String = "",
    val storage: String = "",
    val operatingSystem: String = "",
) : ProductDataClass(title, imageResId, price1, price2, category, description, productId, reviewRating, totalReviews, isFavorite, quantity, availableQuantity)

data class TVs(
    override val title: String = "",
    override val imageResId: Int = 0,
    override val price1: Float = 0f,
    override val price2: Float = 0f,
    override val category: String = "TVs",
    override val description: String = "",
    override val productId: String = "",
    override var reviewRating: Double = 0.0,
    override var totalReviews: Int = 0,
    override var isFavorite: Boolean = false,
    override var quantity: Int = 0,
    override var availableQuantity: Int = 0,
    val screenSize: String = "",
    val resolution: String = "",
    val color: String = ""
) : ProductDataClass(title, imageResId, price1, price2, category, description, productId, reviewRating, totalReviews, isFavorite, quantity, availableQuantity)

data class Laptops(
    override val title: String = "",
    override val imageResId: Int = 0,
    override val price1: Float = 0f,
    override val price2: Float = 0f,
    override val category: String = "Laptops",
    override val description: String = "",
    override val productId: String = "",
    override var reviewRating: Double = 0.0,
    override var totalReviews: Int = 0,
    override var isFavorite: Boolean = false,
    override var quantity: Int = 0,
    override var availableQuantity: Int = 0,
    val ramSize: String = "",
    val screenSize: String = "",
    val resolution: String = "",
    val color: String = "",
    val storage: String = "",
    val operatingSystem: String = ""
) : ProductDataClass(title, imageResId, price1, price2, category, description, productId, reviewRating, totalReviews, isFavorite, quantity, availableQuantity)

data class SoftwareAccessories(
    override val title: String = "",
    override val imageResId: Int = 0,
    override val price1: Float = 0f,
    override val price2: Float = 0f,
    override val category: String = "SoftwareAccessories",
    override val description: String = "",
    override val productId: String = "",
    override var reviewRating: Double = 0.0,
    override var totalReviews: Int = 0,
    override var isFavorite: Boolean = false,
    override var quantity: Int = 0,
    override var availableQuantity: Int = 0
) : ProductDataClass(title, imageResId, price1, price2, category, description, productId, reviewRating, totalReviews, isFavorite, quantity, availableQuantity)

data class VideoGames(
    override val title: String = "",
    override val imageResId: Int = 0,
    override val price1: Float = 0f,
    override val price2: Float = 0f,
    override val category: String = "VideoGames",
    override val description: String = "",
    override val productId: String = "",
    override var reviewRating: Double = 0.0,
    override var totalReviews: Int = 0,
    override var isFavorite: Boolean = false,
    override var quantity: Int = 0,
    override var availableQuantity: Int = 0
) : ProductDataClass(title, imageResId, price1, price2, category, description, productId, reviewRating, totalReviews, isFavorite, quantity, availableQuantity)

data class Order(
    val userId: String = "",
    val address: String = "",
    val paymentMethod: String = "",
    val products: List<Product> = emptyList(),
    val totalPrice: String = ""
)
// Product.kt
data class Product(
    val title: String? = null,
    val price1: Double = 0.0,
    val price2: Double = 0.0,
    val productId: String? = null,
    val imageResId: Int = 0,
)




