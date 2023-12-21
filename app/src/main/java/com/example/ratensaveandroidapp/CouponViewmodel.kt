package com.example.ratensaveandroidapp.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ratensaveandroidapp.datamodel.RatingRequest
import com.example.ratensaveandroidapp.datamodel.CouponResponse
import com.example.ratensaveandroidapp.repository.CouponCodeRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import android.util.Log

class CouponViewModel : ViewModel() {

    // Initialize couponResponse to null or a default value
    private val repository = CouponCodeRepository()

    private val _couponResponse = MutableLiveData<CouponResponse>()
    val couponResponse: LiveData<CouponResponse> = this._couponResponse

    fun submitRatingAndSKU(rating: Int, sku: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val ratingRequest = RatingRequest(rating, sku)
                val response = repository.submitRating(ratingRequest)
                _couponResponse.postValue(response)
                Log.d("CouponViewModel", "API response posted to LiveData, response: $response") // Log success
            } catch (e: Exception) {
                Log.e("CouponViewModel", "Error in submitRatingAndSKU, response not posted to LiveData", e) // Log error
                // Optionally, you could post a failure response or a specific error message to LiveData here
            }
        }
    }
}
