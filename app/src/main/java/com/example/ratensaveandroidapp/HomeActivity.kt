package com.example.ratensaveandroidapp

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.content.Intent
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.example.ratensaveandroidapp.datamodel.AdResponse
import com.example.ratensaveandroidapp.viewmodel.AuctionViewModel
import com.example.ratensaveandroidapp.MidAisleMediumActivity
import android.widget.TextView
import android.util.Log


class HomeActivity : AppCompatActivity() {
    private lateinit var viewModel: AuctionViewModel
    private val handler = Handler(Looper.getMainLooper())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.home_screen)

        viewModel = ViewModelProvider(this).get(AuctionViewModel::class.java)

        // Initialize the button and set an OnClickListener
        findViewById<Button>(R.id.btnStartAuction).setOnClickListener {
            // Trigger the advertising process
            startAuction()
        }

        // Observe the LiveData in your ViewModel for AdResponse
        viewModel.adResponse.observe(this) { adResponse ->
            navigateToAdActivity(adResponse.placementTypeId, adResponse)
        }
    }

    private fun startAuction() {
        // Hardcoded placementId for demonstration purposes
        val placementId = "d1a911b8-4f98-4ba8-9a44-c5fc5a953f0c"
        viewModel.startAuction(placementId)
        Log.d("HomeActivity", "Advertising started for placement ID: $placementId")
    }

    private fun navigateToAdActivity(placementTypeId: Int, adResponse: AdResponse) {
        Log.d("HomeActivity", "Attempting to navigate with placementTypeId: $placementTypeId")
        val intent = when (placementTypeId) {
            1 -> {
                Log.d("HomeActivity", "Navigating to MidAisleMediumActivity")
                Intent(this, MidAisleMediumActivity::class.java)
            }
            else -> {
                Log.d("HomeActivity", "No matching placementTypeId found")
                return // Or navigate to a default activity
            }
        }

        intent.putExtra("adResponse", adResponse)
        startActivity(intent)
    }

}
