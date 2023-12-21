package com.example.ratensaveandroidapp.network

import com.example.ratensaveandroidapp.datamodel.RatingRequest
import com.example.ratensaveandroidapp.datamodel.CouponResponse
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

interface ApiService {
    @POST("https://b5d1dtjncd.execute-api.us-east-2.amazonaws.com/dev/submit-rating")
    suspend fun submitRating(@Body ratingRequest: RatingRequest): CouponResponse
}
