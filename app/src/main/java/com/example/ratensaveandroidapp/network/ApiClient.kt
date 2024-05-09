package com.example.ratensaveandroidapp.network

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor

class ApiClient {
    companion object {
        const val RATING_BASE_URL = "https://b5d1dtjncd.execute-api.us-east-2.amazonaws.com/dev/"
        const val AUCTION_BASE_URL = "https://2ykhuinjv0.execute-api.us-west-2.amazonaws.com/Dev/"
        // Add more BASE_URL constants as new microservices come online

        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY // Set log level to include request/response bodies
        }

        val okHttpClient = OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .build()

        val ratingRetrofit: Retrofit = Retrofit.Builder()
            .baseUrl(RATING_BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .client(okHttpClient) // Add OkHttpClient with the interceptor
            .build()

        val auctionRetrofit: Retrofit = Retrofit.Builder()
            .baseUrl(AUCTION_BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .client(okHttpClient) // Reuse OkHttpClient
            .build()

        // Create ApiService interfaces dynamically
        inline fun <reified T> createApiService(retrofit: Retrofit): T =
            retrofit.create(T::class.java)
    }
}
