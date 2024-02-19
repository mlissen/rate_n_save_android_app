package com.example.ratensaveandroidapp.repository

import com.example.ratensaveandroidapp.datamodel.RatingRequest
import com.example.ratensaveandroidapp.datamodel.CouponResponse
import com.example.ratensaveandroidapp.network.ApiClient
import com.example.ratensaveandroidapp.network.ApiService

class CouponCodeRepository {
    private val apiService: ApiService = ApiClient.ratingRetrofit.create(ApiService::class.java)

    suspend fun submitRating(ratingRequest: RatingRequest): CouponResponse {
        // Make a network call using the ApiClient and return the response
        return apiService.submitRating(ratingRequest)
    }
}