package com.example.ratensaveandroidapp.datamodel

import java.io.Serializable
class AdResponse(
    val gifUrl: String,
    val couponTextHeader: String,
    val couponTextOffer: String,
    val couponTextFooter: String,
    val couponCode: String,
    val qrCodeUrl: String,
    val minutesToNextAuction: Int,
    val placementTypeId: Int
) : Serializable