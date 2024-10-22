package com.example.ratensaveandroidapp.network

import com.example.ratensaveandroidapp.datamodel.AuctionRequest
import com.example.ratensaveandroidapp.datamodel.AdResponse
import retrofit2.http.Body
import retrofit2.http.POST


    interface ApiService {
        @POST("auctions")
        suspend fun startAuction(@Body auctionRequest: AuctionRequest): AdResponse
    }

