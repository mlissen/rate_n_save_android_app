package com.example.ratensaveandroidapp.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ratensaveandroidapp.datamodel.AuctionRequest
import com.example.ratensaveandroidapp.datamodel.AuctionResponse
import com.example.ratensaveandroidapp.repository.AuctionRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import android.util.Log

class AuctionViewModel : ViewModel() {

    // Initialize couponResponse to null or a default value
    private val repository = AuctionRepository()

    private val _auctionResponse = MutableLiveData<AuctionResponse>()
    val auctionResponse: LiveData<AuctionResponse> = _auctionResponse

    fun startAdvertising(placementId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            Log.d("AuctionViewModel", "About to initiate auction call with placementId: $placementId")
            try {
                val auctionRequest = AuctionRequest(placementId)
                val response = repository.startAuction(auctionRequest)
                _auctionResponse.postValue(response)
                Log.d("AuctionViewModel", "API response posted to LiveData, response: $response") // Log success
            } catch (e: Exception) {
                Log.e("AuctionViewModel", "Error in startAuction, response not posted to LiveData", e) // Log error
                // Optionally, you could post a failure response or a specific error message to LiveData here
            }
        }
    }
}

