package com.example.ratensaveandroidapp.utils

interface AdFragment {
    fun isAdActive(): Boolean
    fun getAdType(): String?
    fun resumeAd()  // Add this method to handle resuming the ad
    fun pauseAd()
}