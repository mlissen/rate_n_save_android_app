package com.example.ratensaveandroidapp.network

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class ApiClient {
    companion object {
        const val BASE_URL = "https://b5d1dtjncd.execute-api.us-east-2.amazonaws.com/dev/"
        val retrofit: Retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val apiService: ApiService by lazy {
            retrofit.create(ApiService::class.java)
        }
    }
}
