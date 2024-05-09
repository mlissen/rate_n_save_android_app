package com.example.ratensaveandroidapp.datamodel

import java.io.Serializable
data class AdResponse(
    val creativeUrl: String,
    val fallbackLogo: Boolean,
    val minutesToNextAuction: Int,
    val placementTypeId: Int,
    val storeCloseTime: String,
    val storeOpenTime: String,
    val nextOpeningTime: String,
    val couponCode: String? = null,
    val couponTextHeader: String? = null,
    val couponTextOffer: String? = null,
    val couponTextFooter: String? = null,
    val adType: String? = null
) : Serializable