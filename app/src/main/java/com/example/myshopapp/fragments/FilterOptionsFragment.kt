package com.example.myshopapp.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.DialogFragment
import com.example.myshopapp.R
import com.google.android.material.slider.RangeSlider
import com.google.android.material.slider.Slider

class FilterOptionsFragment : DialogFragment() {

    private var listener: OnFilterApplyListener? = null

    interface OnFilterApplyListener {
        fun onFilterApply(filters: Map<String, Any>)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.filter_options, container, false)

        // Set up the filter options and apply button
        val checkboxCategorySmartphones: CheckBox = view.findViewById(R.id.checkboxCategorySmartphones)
        val checkboxCategoryTablets: CheckBox = view.findViewById(R.id.checkboxCategoryTablets)
        val checkboxCategoryTVs: CheckBox = view.findViewById(R.id.checkboxCategoryTVs)
        val checkboxCategoryLaptops: CheckBox = view.findViewById(R.id.checkboxCategoryLaptops)
        val checkboxCategorySoftwareAccessories: CheckBox = view.findViewById(R.id.checkboxCategorySoftwareAccessories)
        val checkboxCategoryVideoGames: CheckBox = view.findViewById(R.id.checkboxCategoryVideoGames)
        val rangeSliderPrice: RangeSlider = view.findViewById(R.id.rangeSliderPrice)
        val textViewPriceRange: TextView = view.findViewById(R.id.textViewPriceRange)
        val sliderRating: Slider = view.findViewById(R.id.sliderRating)
        val spinnerSmartphonesRamSize: Spinner = view.findViewById(R.id.spinnerSmartphonesRamSize)
        val spinnerSmartphonesColor: Spinner = view.findViewById(R.id.spinnerSmartphonesColor)
        val spinnerSmartphonesScreenSize : Spinner = view.findViewById(R.id.spinnerSmartphonesScreenSize)
        val spinnerSmartphonesStorage : Spinner = view.findViewById(R.id.spinnerSmartphonesStorage)
        val spinnerSmartphonesOS : Spinner = view.findViewById(R.id.spinnerSmartphonesOS)
        val spinnerTabletsRamSize : Spinner = view.findViewById(R.id.spinnerTabletsRamSize)
        val spinnerTabletsColor : Spinner = view.findViewById(R.id.spinnerTabletsColor)
        val spinnerTabletsScreenSize : Spinner = view.findViewById(R.id.spinnerTabletsScreenSize)
        val spinnerTabletsStorage : Spinner = view.findViewById(R.id.spinnerTabletsStorage)
        val spinnerTabletsOS : Spinner = view.findViewById(R.id.spinnerTabletsOS)
        val spinnerTVColors : Spinner = view.findViewById(R.id.spinnerTVColors)
        val spinnerTVScreenSize: Spinner = view.findViewById(R.id.spinnerTVScreenSize)
        val spinnerTVResolution: Spinner = view.findViewById(R.id.spinnerTVResolution)
        val spinnerLaptopRamSize : Spinner = view.findViewById(R.id.spinnerLaptopRamSize)
        val spinnerLaptopColor : Spinner = view.findViewById(R.id.spinnerLaptopColor)
        val spinnerLaptopScreenSize : Spinner = view.findViewById(R.id.spinnerLaptopScreenSize)
        val spinnerLaptopStorage : Spinner = view.findViewById(R.id.spinnerLaptopStorage)
        val spinnerLaptopOS : Spinner = view.findViewById(R.id.spinnerLaptopOS)
        val applyButton: Button = view.findViewById(R.id.buttonApplyFilters)

        // Set default values for the price range slider
        rangeSliderPrice.setValues(0f, 1000f)

        // Update the price range text when the slider values change
        rangeSliderPrice.addOnChangeListener { slider, _, _ ->
            val values = slider.values
            val minPrice = values[0].toInt()
            val maxPrice = values[1].toInt()
            textViewPriceRange.text = "${getString(R.string.price_range)}: $$minPrice - $$maxPrice"
        }

        applyButton.setOnClickListener {
            // Create a map of selected filters
            val filters = mutableMapOf<String, Any>()

            val selectedCategories = mutableListOf<String>()
            if (checkboxCategorySmartphones.isChecked) selectedCategories.add("Smartphones")
            if (checkboxCategoryTablets.isChecked) selectedCategories.add("Tablets")
            if (checkboxCategoryTVs.isChecked) selectedCategories.add("TVs")
            if (checkboxCategoryLaptops.isChecked) selectedCategories.add("Laptops")
            if (checkboxCategorySoftwareAccessories.isChecked) selectedCategories.add("Software & Accessories")
            if (checkboxCategoryVideoGames.isChecked) selectedCategories.add("Video Games")

            filters["categories"] = selectedCategories

            val values = rangeSliderPrice.values
            filters["minPrice"] = values[0]
            filters["maxPrice"] = values[1]

            filters["minRating"] = sliderRating.value

            // Smartphones Specific Filters
            filters["smartphoneRamSize"] = spinnerSmartphonesRamSize.selectedItem.toString()
            filters["smartphoneColor"] = spinnerSmartphonesColor.selectedItem.toString()
            filters["smartphoneScreenSize"] = spinnerSmartphonesScreenSize.selectedItem.toString()
            filters["smartphoneStorage"] = spinnerSmartphonesStorage.selectedItem.toString()
            filters["smartphoneOS"] = spinnerSmartphonesOS.selectedItem.toString()

            // Tablets Specific Filters
            filters["tabletRamSize"] = spinnerTabletsRamSize.selectedItem.toString()
            filters["tabletColor"] = spinnerTabletsColor.selectedItem.toString()
            filters["tabletScreenSize"] = spinnerTabletsScreenSize.selectedItem.toString()
            filters["tabletStorage"] = spinnerTabletsStorage.selectedItem.toString()
            filters["tabletOS"] = spinnerTabletsOS.selectedItem.toString()

            // TVs Specific Filters
            filters["tvColors"] = spinnerTVColors.selectedItem.toString()
            filters["tvScreenSize"] = spinnerTVScreenSize.selectedItem.toString()
            filters["tvResolution"] = spinnerTVResolution.selectedItem.toString()

            // Laptops Specific Filters
            filters["laptopRamSize"] = spinnerLaptopRamSize.selectedItem.toString()
            filters["laptopColor"] = spinnerLaptopColor.selectedItem.toString()
            filters["laptopScreenSize"] = spinnerLaptopScreenSize.selectedItem.toString()
            filters["laptopStorage"] = spinnerLaptopStorage.selectedItem.toString()
            filters["laptopOS"] = spinnerLaptopOS.selectedItem.toString()

            // Notify the listener
            listener?.let { it.onFilterApply(filters) }

            // Dismiss the dialog
            dismiss()
        }

        return view
    }

    fun setOnFilterApplyListener(listener: OnFilterApplyListener) {
        this.listener = listener
    }
}
