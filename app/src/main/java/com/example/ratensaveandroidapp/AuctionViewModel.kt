package com.example.ratensaveandroidapp

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ratensaveandroidapp.datamodel.AuctionRequest
import com.example.ratensaveandroidapp.datamodel.AdResponse
import com.example.ratensaveandroidapp.repository.AuctionRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import android.util.Log

class AuctionViewModel : ViewModel() {
    private val repository = AuctionRepository()

    private val _adResponse = MutableLiveData<AdResponse>()
    val adResponse: LiveData<AdResponse> = _adResponse

    fun startAuction(placementId: String, storeTimezone: String) {
        viewModelScope.launch(Dispatchers.IO) {
            Log.d("AuctionViewModel", "About to initiate auction call with placementId: $placementId")
            try {
                val auctionRequest = AuctionRequest(placementId, storeTimezone)
                val response = repository.startAuction(auctionRequest)
                Log.d("AuctionViewModel", "Received response from repository: $response")
                _adResponse.postValue(response)
                Log.d("AuctionViewModel", "Posted response to LiveData")
                Log.d("AuctionViewModel", "LISS creativeUrl: ${response.creativeUrl}")
            } catch (e: Exception) {
                Log.e("AuctionViewModel", "Error in startAuction: ${e.javaClass.simpleName}", e)
                Log.e("AuctionViewModel", "Error message: ${e.message}")
                Log.e("AuctionViewModel", "Stack trace: ${e.stackTraceToString()}")
                // Optionally post an error state to LiveData
                // _adResponse.postValue(AdResponse.Error(e.message ?: "Unknown error occurred"))
            }
        }
    }
}