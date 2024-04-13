package com.example.ratensaveandroidapp.datamodel

import java.io.Serializable
class AdResponse(
    val creativeUrl: String,
    val couponTextHeader: String,
    val couponTextOffer: String,
    val couponTextFooter: String,
    val couponCode: String,
    val minutesToNextAuction: Int,
    val placementTypeId: Int,
    val adType: String,
    val storeOpenTime: String?,
    val storeCloseTime: String?,
    val nextOpeningTime: String?,
) : Serializable