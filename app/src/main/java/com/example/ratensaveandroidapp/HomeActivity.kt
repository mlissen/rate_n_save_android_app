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
import android.view.View
import android.view.WindowInsets
import android.view.WindowInsetsController

class HomeActivity : AppCompatActivity() {
    private lateinit var viewModel: AuctionViewModel
    private lateinit var etPlacementId: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.home_screen)

        hideSystemUI() // Add this line to hide system UI

        etPlacementId = findViewById(R.id.etPlacementId)
        viewModel = ViewModelProvider(this).get(AuctionViewModel::class.java)

        val sharedPref = getSharedPreferences("AppSettings", Context.MODE_PRIVATE)
        val storedPlacementId = sharedPref.getString("placementId", null)

        storedPlacementId?.let {
            etPlacementId.setText(it)
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
            Log.d("HomeActivity", "Received adResponse from AuctionViewModel: $adResponse")
            navigateToAdActivity(adResponse.placementTypeId, adResponse.templateId, adResponse)
        }
    }

    private fun hideSystemUI() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
            window.insetsController?.apply {
                systemBarsBehavior = WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
                hide(WindowInsets.Type.systemBars())
            }
        } else {
            @Suppress("DEPRECATION")
            window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_FULLSCREEN
                    or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY)
        }
    }

    private fun startAuction(placementId: String) {
        viewModel.startAuction(placementId)
        Log.d("HomeActivity", "Advertising started for placement ID: $placementId")
    }

    private fun storePlacementId(placementId: String) {
        val sharedPref = getSharedPreferences("AppSettings", Context.MODE_PRIVATE)
        with(sharedPref.edit()) {
            putString("placementId", placementId)
            apply()
        }
    }

    private fun navigateToAdActivity(placementTypeId: Int, templateId: Int, adResponse: AdResponse) {
        Log.d("HomeActivity", "Navigating with placementTypeId: $placementTypeId, templateId: $templateId")

        val intent = Intent(this, AdDisplayActivity::class.java).apply {
            putExtra("placementTypeId", placementTypeId)
            putExtra("templateId", templateId)
            putExtra("adResponse", adResponse)
        }

        startActivity(intent)
    }
}
