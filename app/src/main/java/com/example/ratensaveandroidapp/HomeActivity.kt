package com.example.ratensaveandroidapp
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.example.ratensaveandroidapp.viewmodel.AuctionViewModel
import java.util.*
import android.widget.Button
import android.widget.TextView
import com.example.ratensaveandroidapp.R


import android.util.Log

class HomeActivity : AppCompatActivity() {
    private lateinit var viewModel: AuctionViewModel // Assuming your ViewModel is called AuctionViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.home_screen) // Layout with the "Start Advertising" button

        viewModel = ViewModelProvider(this).get(AuctionViewModel::class.java)

        findViewById<Button>(R.id.btnStartAdvertising).setOnClickListener {
            // Trigger API call to get adID
            val placementId = "d1a911b8-4f98-4ba8-9a44-c5fc5a953f0c"
            viewModel.startAdvertising(placementId)
        }

        // Observe the LiveData from the ViewModel for adID response
        viewModel.auctionResponse.observe(this) { auctionResponse ->
            // Display the received adID in the TextView
            val adIdTextView = findViewById<TextView>(R.id.tvAdId)
            adIdTextView.text = "Ad ID: ${auctionResponse.ad_id}"

            // Handle different ad types based on adID
            // when (adResponse.adType) {
            //   "samplingwfeedback" -> launchSamplingStationAd(adResponse.adID)
            // Add cases for other ad types as necessary
            //  }
        }
    }
}

    // Additional methods to handle other ad types...
