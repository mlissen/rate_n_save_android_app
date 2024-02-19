package com.example.ratensaveandroidapp.datamodel

data class AdResponse(
    val adType: String,
    val gifUrl: String,
    val couponTextHeader: String,
    val couponTextOffer: String,
    val couponTextFooter: String,
    val couponID: String,
    val qrCodeUrl: String
    )

