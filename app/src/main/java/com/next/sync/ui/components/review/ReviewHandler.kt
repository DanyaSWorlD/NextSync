package com.next.sync.ui.components.review

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.core.net.toUri
import com.google.android.play.core.review.ReviewManagerFactory

object ReviewHandler {
    /**
     * Initiates the in-app review flow.
     *
     * @param activity The current Activity, required to launch the review flow.
     */
    fun launchInAppReview(activity: Activity) {
        val reviewManager = ReviewManagerFactory.create(activity)
        val request = reviewManager.requestReviewFlow()
        request.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val reviewInfo = task.result
                val flow = reviewManager.launchReviewFlow(activity, reviewInfo)
                flow.addOnCompleteListener { _ -> }
            } else {
                openPlayStoreForRating(activity)
            }
        }
    }

    /**
     * Opens the app's page on the Google Play Store for rating as a fallback.
     *
     * @param context The Context used to start the intent.
     */
    fun openPlayStoreForRating(context: Context) {
        val packageName = context.packageName
        val intent = Intent(
            Intent.ACTION_VIEW,
            "market://details?id=$packageName".toUri()
        )
        context.startActivity(intent)
    }
}