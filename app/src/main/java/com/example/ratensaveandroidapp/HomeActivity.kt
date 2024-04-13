package com.example.ratensaveandroidapp

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.example.ratensaveandroidapp.datamodel.AdResponse
import com.example.ratensaveandroidapp.viewmodel.AuctionViewModel

class HomeActivity : AppCompatActivity() {
    private lateinit var viewModel: AuctionViewModel
    private lateinit var etPlacementId: EditText

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)

        setContentView(R.layout.home_screen)
        etPlacementId = findViewById(R.id.etPlacementId)
        viewModel = ViewModelProvider(this).get(AuctionViewModel::class.java)

        val sharedPref = getSharedPreferences("AppSettings", Context.MODE_PRIVATE)
        val storedPlacementId = sharedPref.getString("placementId", null)

        if (storedPlacementId != null) {
            etPlacementId.setText(storedPlacementId)
        }


        findViewById<Button>(R.id.btnActivatePlacement).setOnClickListener {
            val placementId = etPlacementId.text.toString()
            if (placementId.isNotBlank()) {
                storePlacementId(placementId)
                startAuction(placementId)
            } else {
                Log.d("HomeActivity", "Placement ID is empty")
            }
        }

        viewModel.adResponse.observe(this) { adResponse ->
            navigateToAdActivity(adResponse.placementTypeId, adResponse)
        }
    }

    private fun startAuction(placementId: String) {
        viewModel.startAuction(placementId)
        Log.d("HomeActivity", "Advertising started for placement ID: $placementId")
    }

    private fun storePlacementId(placementId: String) {
        // Use getSharedPreferences with a specific name to ensure it's accessible across the application
        val sharedPref = getSharedPreferences("AppSettings", Context.MODE_PRIVATE)
        with(sharedPref.edit()) {
            putString("placementId", placementId)
            apply()
        }
    }

    private fun navigateToAdActivity(placementTypeId: Int, adResponse: AdResponse) {
        Log.d("HomeActivity", "Attempting to navigate with placementTypeId: $placementTypeId")
        Log.d("HomeActivity", "Attempting to navigate with AdResponse: $adResponse")
        val intent = when (placementTypeId ) {
            1,2 -> {
                Log.d("HomeActivity", "Navigating to MidAisleMediumActivity")
                Intent(this, MidAisleMediumActivity::class.java).apply {
                intent.putExtra("adResponse", adResponse)
                putExtra("isInitialAd", true)
            }
            }
            else -> {
                Log.d("HomeActivity", "No matching placementTypeId found")
                return // Or navigate to a default activity
            }
        }
        startActivity(intent)
    }
}