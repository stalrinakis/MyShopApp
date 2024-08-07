package com.example.myshopapp.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RatingBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.myshopapp.R
import com.example.myshopapp.dataclasses.ReviewDataClass

class ReviewAdapter(private val reviews: List<ReviewDataClass>) : RecyclerView.Adapter<ReviewAdapter.ReviewViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReviewViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_review, parent, false)
        return ReviewViewHolder(view)
    }

    override fun onBindViewHolder(holder: ReviewViewHolder, position: Int) {
        val review = reviews[position]
        holder.bind(review)
    }

    override fun getItemCount(): Int {
        return reviews.size
    }

    inner class ReviewViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val textViewReviewerName: TextView = itemView.findViewById(R.id.textViewReviewerName)
        private val textViewReviewText: TextView = itemView.findViewById(R.id.textViewReviewText)
        private val ratingBarReview: RatingBar = itemView.findViewById(R.id.ratingBarReview)

        fun bind(review: ReviewDataClass) {
            textViewReviewerName.text = review.reviewerName
            textViewReviewText.text = review.reviewText
            ratingBarReview.rating = review.rating
        }
    }
}
