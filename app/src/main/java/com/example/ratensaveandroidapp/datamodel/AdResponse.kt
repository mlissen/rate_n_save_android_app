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
    val offerCode: String? = null,
    //val couponTextHeader: String? = null,
    //val couponTextOffer: String? = null,
    //val couponTextFooter: String? = null,
    val adType: String? = null,
    val content: Content? = null, // Add content field to accommodate for additional text,
    val templateId: Int
) : Serializable

data class Content(
    val header: String,
    val body: String,
    val terms: String,
    val offer_code: String,
) : Serializable
