package com.example.ratensaveandroidapp.repository

import android.util.Log
import com.example.ratensaveandroidapp.datamodel.AuctionRequest
import com.example.ratensaveandroidapp.datamodel.AdResponse
import com.example.ratensaveandroidapp.network.ApiClient
import com.example.ratensaveandroidapp.network.ApiService
import retrofit2.HttpException
import java.io.IOException

class AuctionRepository {
    private val apiService: ApiService = ApiClient.createApiService(ApiClient.auctionRetrofit)

    suspend fun startAuction(auctionRequest: AuctionRequest): AdResponse {
        try {
            Log.d("AuctionRepository", "Starting auction with request: $auctionRequest")
            val response = apiService.startAuction(auctionRequest)
            Log.d("AuctionRepository", "Received response: $response")
            return response
        } catch (e: HttpException) {
            Log.e("AuctionRepository", "HTTP error ${e.code()}: ${e.message()}")
            Log.e("AuctionRepository", "Error body: ${e.response()?.errorBody()?.string()}")
            throw e
        } catch (e: IOException) {
            Log.e("AuctionRepository", "Network error: ${e.message}")
            throw e
        } catch (e: Exception) {
            Log.e("AuctionRepository", "Unexpected error: ${e.message}")
            Log.e("AuctionRepository", "Stack trace: ${e.stackTraceToString()}")
            throw e
        }
    }
}