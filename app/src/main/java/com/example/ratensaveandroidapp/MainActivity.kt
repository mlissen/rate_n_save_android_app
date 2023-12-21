package com.example.ratensaveandroidapp

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.example.ratensaveandroidapp.viewmodel.CouponViewModel
import java.util.*
import android.util.Log
import com.example.ratensaveandroidapp.R

class MainActivity : AppCompatActivity() {
    private lateinit var emojis: Array<ImageView>
    private lateinit var viewModel: CouponViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main) // Set the layout for this activity

        viewModel = ViewModelProvider(this@MainActivity).get(CouponViewModel::class.java)

        emojis = arrayOf(
            findViewById(R.id.emoji1),
            findViewById(R.id.emoji2),
            findViewById(R.id.emoji3),
            findViewById(R.id.emoji4),
            findViewById(R.id.emoji5)
        )

        emojis.forEachIndexed { index, emoji ->
            emoji.setOnClickListener {
                val uniqueID = UUID.randomUUID().toString()
                val productRating = index + 1
                viewModel.submitRatingAndSKU(productRating, uniqueID)
            }
        }

        // Call observeViewModel to start observing LiveData changes
        observeViewModel()
    }

    private fun observeViewModel() {
        viewModel.couponResponse.observe(this, { response ->
            Log.d("MainActivity", "LiveData observed: $response")
            response?.let {
                navigateToQRCodeScreen(it.couponID)
            } ?: run {
                Log.d("MainActivity", "No response received in LiveData")
            }
        })
    }

    private fun navigateToQRCodeScreen(couponId: String) {
        val intent = Intent(this, QRCodeActivity::class.java).apply {
            putExtra("COUPON_ID", couponId)
        }
        startActivity(intent)
    }
}
