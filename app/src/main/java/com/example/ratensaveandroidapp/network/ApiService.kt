package com.example.ratensaveandroidapp.network

import com.example.ratensaveandroidapp.datamodel.RatingRequest
import com.example.ratensaveandroidapp.datamodel.CouponResponse
import com.example.ratensaveandroidapp.datamodel.AuctionRequest
import com.example.ratensaveandroidapp.datamodel.AuctionResponse
import retrofit2.http.Body
import retrofit2.http.POST


    interface ApiService {
        @POST("submit-rating") // URL is relative to the specific Retrofit baseURL
        suspend fun submitRating(@Body ratingRequest: RatingRequest): CouponResponse

        @POST("auction")
        suspend fun startAuction(@Body auctionRequest: AuctionRequest): AuctionResponse
    }

