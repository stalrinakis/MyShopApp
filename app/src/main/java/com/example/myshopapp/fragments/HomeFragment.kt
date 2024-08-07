package com.example.myshopapp.fragments

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.ScrollView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager.widget.ViewPager
import com.example.myshopapp.R
import com.example.myshopapp.adapters.BannerAdapter
import com.example.myshopapp.adapters.ProductAdapter
import com.example.myshopapp.dataclasses.BannerDataClass
import com.example.myshopapp.dataclasses.Laptops
import com.example.myshopapp.dataclasses.ProductDataClass
import com.example.myshopapp.dataclasses.Smartphones
import com.example.myshopapp.dataclasses.SoftwareAccessories
import com.example.myshopapp.dataclasses.TVs
import com.example.myshopapp.dataclasses.Tablets
import com.example.myshopapp.dataclasses.VideoGames
import com.google.firebase.firestore.FirebaseFirestore
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.tbuonomo.viewpagerdotsindicator.DotsIndicator
import java.io.InputStreamReader

class HomeFragment : Fragment() {

    private lateinit var bannerContainer: ViewPager
    private lateinit var dotsIndicator: DotsIndicator
    private lateinit var progressBarBanner: ProgressBar
    private lateinit var progressBarProducts: ProgressBar
    private lateinit var scrollView2: ScrollView
    private lateinit var recyclerViewProducts: RecyclerView
    private lateinit var productAdapter: ProductAdapter
    private lateinit var searchBar: EditText
    private lateinit var firestore: FirebaseFirestore
    private lateinit var buttonFilter: Button
    private var allProducts = mutableListOf<ProductDataClass>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_home, container, false)

        // Initialize Firestore
        firestore = FirebaseFirestore.getInstance()

        // Initialize views
        bannerContainer = view.findViewById(R.id.bannerContainer)
        dotsIndicator = view.findViewById(R.id.dotdotdot)
        progressBarBanner = view.findViewById(R.id.progressBarBanner)
        progressBarProducts = view.findViewById(R.id.progressBarProducts)

        scrollView2 = view.findViewById(R.id.scrollView2)
        recyclerViewProducts = view.findViewById(R.id.recyclerViewProducts)

        searchBar = view.findViewById(R.id.textViewSearch_bar)
        buttonFilter = view.findViewById(R.id.buttonFilter)

        // Set up the RecyclerView and ProductAdapter with an empty list
        productAdapter = ProductAdapter(requireContext(), mutableListOf())
        recyclerViewProducts.layoutManager = GridLayoutManager(requireContext(), 2)
        recyclerViewProducts.adapter = productAdapter

        // Set up the adapter for the ViewPager
        val bannerAdapter = BannerAdapter(requireContext())
        bannerContainer.adapter = bannerAdapter

        // Bind the DotsIndicator with the ViewPager
        dotsIndicator.setViewPager(bannerContainer)

        // Request focus on the ScrollView to prevent the EditText from gaining focus
        scrollView2.requestFocus()

        // Handle filter button click
        buttonFilter.setOnClickListener {
            val filterDialog = FilterOptionsFragment()
            filterDialog.setOnFilterApplyListener(object : FilterOptionsFragment.OnFilterApplyListener {
                override fun onFilterApply(filters: Map<String, Any>) {
                    // Apply the filters to the product list
                    applyFilters(filters)
                }
            })
            filterDialog.show(parentFragmentManager, "FilterDialogFragment")
        }

        // Add TextWatcher to the search bar
        searchBar.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                filterProducts(s.toString())
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        return view
    }

    fun applyFilters(filters: Map<String, Any>) {
        // Extract general filter values
        val selectedCategories = filters["categories"] as List<String>
        val minPrice = filters["minPrice"] as Float
        val maxPrice = filters["maxPrice"] as Float
        val minRating = filters["minRating"] as Float

        // Apply the filters to the product list
        val filteredProducts = allProducts.filter { product ->
            // Filter by category
            val isInCategory = selectedCategories.isEmpty() || product.category in selectedCategories

            // Filter by price range
            val isInPriceRange = product.price1 in minPrice..maxPrice || product.price2 in minPrice..maxPrice

            // Filter by rating
            val hasMinRating = product.reviewRating >= minRating

            // Filter by product type
            when (product) {
                is Smartphones -> {
                    val smartphoneRamSize = filters["smartphoneRamSize"] as? String ?: ""
                    val smartphoneColor = filters["smartphoneColor"] as? String ?: ""
                    val smartphoneScreenSize = filters["smartphoneScreenSize"] as? String ?: ""
                    val smartphoneStorage = filters["smartphoneStorage"] as? String ?: ""
                    val smartphoneOS = filters["smartphoneOS"] as? String ?: ""
                    product.category == "Smartphones" &&
                            (smartphoneRamSize == "-" || smartphoneRamSize.contains(product.ramSize)) &&
                            (smartphoneColor == "-" || smartphoneColor.contains(product.color)) &&
                            (smartphoneScreenSize == "-" || smartphoneScreenSize.contains(product.screenSize)) &&
                            (smartphoneStorage == "-" || smartphoneStorage.contains(product.storage)) &&
                            (smartphoneOS == "-" || smartphoneOS.contains(product.operatingSystem))
                }
                is Tablets -> {
                    val tabletRamSize = filters["tabletRamSize"] as? String ?: ""
                    val tabletColor = filters["tabletColor"] as? String ?: ""
                    val tabletScreenSize = filters["tabletScreenSize"] as? String ?: ""
                    val tabletStorage = filters["tabletStorage"] as? String ?: ""
                    val tabletOS = filters["tabletOS"] as? String ?: ""
                    product.category == "Tablets" &&
                            (tabletRamSize == "-" || tabletRamSize.contains(product.ramSize)) &&
                            (tabletColor == "-" || tabletColor.contains(product.color)) &&
                            (tabletScreenSize == "-" || tabletScreenSize.contains(product.screenSize)) &&
                            (tabletStorage == "-" || tabletStorage.contains(product.storage)) &&
                            (tabletOS == "-" || tabletOS.contains(product.operatingSystem))
                }
                is TVs -> {
                    val tvColors = filters["tvColors"] as? String ?: ""
                    val tvScreenSize = filters["tvScreenSize"] as? String ?: ""
                    val tvResolution = filters["tvResolution"] as? String ?: ""
                    product.category == "TVs" &&
                            (tvColors == "-" || tvColors.contains(product.color)) &&
                            (tvScreenSize == "-" || tvScreenSize.contains(product.screenSize)) &&
                            (tvResolution == "-" || tvResolution.contains(product.resolution))
                }
                is Laptops -> {
                    val laptopRamSize = filters["laptopRamSize"] as? String ?: ""
                    val laptopColor = filters["laptopColor"] as? String ?: ""
                    val laptopScreenSize = filters["laptopScreenSize"] as? String ?: ""
                    val laptopStorage = filters["laptopStorage"] as? String ?: ""
                    val laptopOS = filters["laptopOS"] as? String ?: ""
                    product.category == "Laptops" &&
                            (laptopRamSize == "-" || laptopRamSize.contains(product.ramSize)) &&
                            (laptopColor == "-" || laptopColor.contains(product.color)) &&
                            (laptopScreenSize == "-" || laptopScreenSize.contains(product.screenSize)) &&
                            (laptopStorage == "-" || laptopStorage.contains(product.storage)) &&
                            (laptopOS == "-" || laptopOS.contains(product.operatingSystem))
                }
                else -> true // Include all other product types by default
            } && isInCategory && isInPriceRange && hasMinRating
        }

        // Update the adapter with the filtered products
        productAdapter.updateData(filteredProducts)
    }




    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize lists and adapters

        // Upload JSON data to Firestore and then load data into the app
        readJsonFileAndUploadToFirestore(object : FirestoreUploadCallback {
            override fun onUploadComplete() {
                // Load data into the RecyclerViews
                loadProductData()

                // Load data into the ViewPager
                loadBannerData()
            }
        })
    }

    private fun filterProducts(query: String) {
        Log.d("HomeFragment", "Filtering products with query: $query")
        val filteredProducts = if (query.isEmpty()) {
            allProducts // A list containing all products initially loaded from Firestore
        } else {
            allProducts.filter { product ->
                product.title.contains(query, ignoreCase = true)
            }
        }

        Log.d("HomeFragment", "Filtered products count: ${filteredProducts.size}")
        productAdapter.updateData(filteredProducts)
    }

    private fun loadBannerData() {
        progressBarBanner.visibility = View.VISIBLE
        bannerContainer.visibility = View.GONE

        val bannerData = getBannerData()

        if (bannerData.isNotEmpty()) {
            (bannerContainer.adapter as BannerAdapter).updateData(bannerData)
            progressBarBanner.visibility = View.GONE
            bannerContainer.visibility = View.VISIBLE
        } else {
            // Handle empty state if necessary
        }
    }

    private fun getBannerData(): List<BannerDataClass> {
        return listOf(
            BannerDataClass("Banner 1", R.drawable.image_background),
            BannerDataClass("Banner 2", R.drawable.ic_launcher_foreground)
        )
    }

    private fun loadProductData() {
        progressBarProducts.visibility = View.VISIBLE
        fetchProductsFromFirestore()
    }

    private fun fetchProductsFromFirestore() {
        firestore.collection("products")
            .get()
            .addOnSuccessListener { documents ->
                val products = documents.mapNotNull { document ->
                    val category = document.getString("category") ?: ""
                    when (category) {
                        "Smartphones" -> document.toObject(Smartphones::class.java)
                        "Tablets" -> document.toObject(Tablets::class.java)
                        "TVs" -> document.toObject(TVs::class.java)
                        "Laptops" -> document.toObject(Laptops::class.java)
                        "SoftwareAccessories" -> document.toObject(SoftwareAccessories::class.java)
                        "VideoGames" -> document.toObject(VideoGames::class.java)
                        else -> null
                    }
                }
                allProducts.clear() // Clear existing products
                allProducts.addAll(products) // Add new products from Firestore
                productAdapter.updateData(allProducts) // Update the adapter
                progressBarProducts.visibility = View.GONE
            }
            .addOnFailureListener { exception ->
                Log.w("HomeFragment", "Error getting documents: ", exception)
                progressBarProducts.visibility = View.GONE
            }
    }

    private fun readJsonFileAndUploadToFirestore(callback: FirestoreUploadCallback) {
        val jsonFile = requireContext().assets.open("products.json")
        val productListType = object : TypeToken<List<Map<String, Any>>>() {}.type
        val products: List<Map<String, Any>> = Gson().fromJson(InputStreamReader(jsonFile), productListType)

        val productList = products.mapNotNull { productMap ->
            val category = productMap["category"] as String
            when (category) {
                "Smartphones" -> productMap.toSmartphones()
                "Tablets" -> productMap.toTablets()
                "TVs" -> productMap.toTVs()
                "Laptops" -> productMap.toLaptops()
                "SoftwareAccessories" -> productMap.toSoftwareAccessories()
                "VideoGames" -> productMap.toVideoGames()
                else -> null
            }
        }

        // Remove null values and convert to list
        val validProductList = productList.filterNotNull()

        // Upload each product to Firestore
        val batch = firestore.batch()

        validProductList.forEach { product ->
            val documentRef = firestore.collection("products").document(product.productId)
            batch.set(documentRef, product)
        }

        // Commit the batch write
        batch.commit()
            .addOnCompleteListener {
                if (it.isSuccessful) {
                    callback.onUploadComplete()
                } else {
                    Log.e("HomeFragment", "Error uploading data to Firestore", it.exception)
                }
            }
    }

// Extension functions to convert Map<String, Any> to respective data classes

    private fun Map<String, Any>.toSmartphones(): Smartphones? {
        return try {
            Smartphones(
                title = this["title"] as String,
                imageResId = R.drawable.phone,
                price1 = (this["price1"] as Double).toFloat(),
                price2 = (this["price2"] as Double).toFloat(),
                category = this["category"] as String,
                description = this["description"] as String,
                productId = this["productId"] as String,
                reviewRating = (this["reviewRating"] as Double),
                totalReviews = (this["totalReviews"] as Double).toInt(),
                isFavorite = this["isFavorite"] as Boolean,
                quantity = (this["quantity"] as Double).toInt(),
                availableQuantity = (this["availableQuantity"] as Double).toInt(),
                ramSize = this["ramSize"] as String,
                color = this["color"] as String,
                screenSize = this["screenSize"] as String,
                storage = this["storage"] as String,
                operatingSystem = this["operatingSystem"] as String
            )
        } catch (e: Exception) {
            Log.e("HomeFragment", "Error parsing Smartphone", e)
            null
        }
    }

    private fun Map<String, Any>.toTablets(): Tablets? {
        return try {
            Tablets(
                title = this["title"] as String,
                imageResId = R.drawable.tablet,
                price1 = (this["price1"] as Double).toFloat(),
                price2 = (this["price2"] as Double).toFloat(),
                category = this["category"] as String,
                description = this["description"] as String,
                productId = this["productId"] as String,
                reviewRating = (this["reviewRating"] as Double),
                totalReviews = (this["totalReviews"] as Double).toInt(),
                isFavorite = this["isFavorite"] as Boolean,
                quantity = (this["quantity"] as Double).toInt(),
                availableQuantity = (this["availableQuantity"] as Double).toInt(),
                ramSize = this["ramSize"] as String,
                color = this["color"] as String,
                screenSize = this["screenSize"] as String,
                storage = this["storage"] as String,
                operatingSystem = this["operatingSystem"] as String
            )
        } catch (e: Exception) {
            Log.e("HomeFragment", "Error parsing Tablet", e)
            null
        }
    }

    private fun Map<String, Any>.toTVs(): TVs? {
        return try {
            TVs(
                title = this["title"] as String,
                imageResId = R.drawable.tv,
                price1 = (this["price1"] as Double).toFloat(),
                price2 = (this["price2"] as Double).toFloat(),
                category = this["category"] as String,
                description = this["description"] as String,
                productId = this["productId"] as String,
                reviewRating = (this["reviewRating"] as Double),
                totalReviews = (this["totalReviews"] as Double).toInt(),
                isFavorite = this["isFavorite"] as Boolean,
                quantity = (this["quantity"] as Double).toInt(),
                availableQuantity = (this["availableQuantity"] as Double).toInt(),
                color = this["color"] as String,
                screenSize = this["screenSize"] as String,
                resolution = this["resolution"] as String
            )
        } catch (e: Exception) {
            Log.e("HomeFragment", "Error parsing TV", e)
            null
        }
    }

    private fun Map<String, Any>.toLaptops(): Laptops? {
        return try {
            Laptops(
                title = this["title"] as String,
                imageResId = R.drawable.laptop,
                price1 = (this["price1"] as Double).toFloat(),
                price2 = (this["price2"] as Double).toFloat(),
                category = this["category"] as String,
                description = this["description"] as String,
                productId = this["productId"] as String,
                reviewRating = (this["reviewRating"] as Double),
                totalReviews = (this["totalReviews"] as Double).toInt(),
                isFavorite = this["isFavorite"] as Boolean,
                quantity = (this["quantity"] as Double).toInt(),
                availableQuantity = (this["availableQuantity"] as Double).toInt(),
                color = this["color"] as String,
                screenSize = this["screenSize"] as String,
                resolution = this["resolution"] as String,
                ramSize = this["ramSize"] as String,
                storage = this["storage"] as String,
                operatingSystem = this["operatingSystem"] as String
            )
        } catch (e: Exception) {
            Log.e("HomeFragment", "Error parsing Laptop", e)
            null
        }
    }

    private fun Map<String, Any>.toSoftwareAccessories(): SoftwareAccessories? {
        return try {
            SoftwareAccessories(
                title = this["title"] as String,
                imageResId = R.drawable.trashcan,
                price1 = (this["price1"] as Double).toFloat(),
                price2 = (this["price2"] as Double).toFloat(),
                category = this["category"] as String,
                description = this["description"] as String,
                productId = this["productId"] as String,
                reviewRating = (this["reviewRating"] as Double),
                totalReviews = (this["totalReviews"] as Double).toInt(),
                isFavorite = this["isFavorite"] as Boolean,
                quantity = (this["quantity"] as Double).toInt(),
                availableQuantity = (this["availableQuantity"] as Double).toInt()
            )
        } catch (e: Exception) {
            Log.e("HomeFragment", "Error parsing SoftwareAccessories", e)
            null
        }
    }

    private fun Map<String, Any>.toVideoGames(): VideoGames? {
        return try {
            VideoGames(
                title = this["title"] as String,
                imageResId = R.drawable.image_foreground,
                price1 = (this["price1"] as Double).toFloat(),
                price2 = (this["price2"] as Double).toFloat(),
                category = this["category"] as String,
                description = this["description"] as String,
                productId = this["productId"] as String,
                reviewRating = (this["reviewRating"] as Double),
                totalReviews = (this["totalReviews"] as Double).toInt(),
                isFavorite = this["isFavorite"] as Boolean,
                quantity = (this["quantity"] as Double).toInt(),
                availableQuantity = (this["availableQuantity"] as Double).toInt()
            )
        } catch (e: Exception) {
            Log.e("HomeFragment", "Error parsing VideoGames", e)
            null
        }
    }


    interface FirestoreUploadCallback {
        fun onUploadComplete()
    }
}
