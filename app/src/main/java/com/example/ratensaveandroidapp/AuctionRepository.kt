package com.example.ratensaveandroidapp.repository

import com.example.ratensaveandroidapp.datamodel.AuctionRequest
import com.example.ratensaveandroidapp.datamodel.AdResponse
import com.example.ratensaveandroidapp.network.ApiClient
import com.example.ratensaveandroidapp.network.ApiService

class AuctionRepository {
    private val apiService: ApiService = ApiClient.auctionRetrofit.create(ApiService::class.java)

    suspend fun startAuction(auctionRequest: AuctionRequest): AdResponse {
        // Make a network call using the ApiClient and return the response
        return apiService.startAuction(auctionRequest)
    }
}