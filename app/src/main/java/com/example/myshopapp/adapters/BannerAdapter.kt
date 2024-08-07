package com.example.myshopapp.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.viewpager.widget.PagerAdapter
import com.example.myshopapp.R
import com.example.myshopapp.dataclasses.BannerDataClass

class BannerAdapter(private val context: Context) : PagerAdapter() {

    private val bannerItems = mutableListOf<BannerDataClass>()

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        val view = LayoutInflater.from(context).inflate(R.layout.activity_banner, container, false)
        val imageView: ImageView = view.findViewById(R.id.bannerItem)

        // Bind data to view
        val bannerItem = bannerItems[position]
        imageView.setImageResource(bannerItem.imageResId)

        container.addView(view)
        return view
    }

    override fun getCount(): Int {
        return bannerItems.size
    }

    override fun isViewFromObject(view: View, `object`: Any): Boolean {
        return view == `object`
    }

    override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
        container.removeView(`object` as View)
    }

    fun updateData(newData: List<BannerDataClass>) {
        bannerItems.clear()
        bannerItems.addAll(newData)
        notifyDataSetChanged()
    }
}
