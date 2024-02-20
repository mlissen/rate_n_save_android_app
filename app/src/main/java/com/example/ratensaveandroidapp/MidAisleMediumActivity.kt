package com.example.ratensaveandroidapp

import android.os.Bundle
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.example.ratensaveandroidapp.databinding.MidAisleMediumLayoutBinding
import com.bumptech.glide.Glide
import com.example.ratensaveandroidapp.datamodel.AdResponse
import com.example.ratensaveandroidapp.utils.QRCodeGenerator.generateQRCode
import android.util.*

    class MidAisleMediumActivity : AppCompatActivity() {

        //Declare the QR Code ImageView
        private lateinit var qrCodeImageView: ImageView

        // Declare the binding
        private lateinit var binding: MidAisleMediumLayoutBinding

        override fun onCreate(savedInstanceState: Bundle?) {
            // Make the activity full screen by hiding the system bars


            super.onCreate(savedInstanceState)
            // Initialize the binding
            binding = MidAisleMediumLayoutBinding.inflate(layoutInflater)
            setContentView(binding.root)

            qrCodeImageView = binding.qrCodeImageView
            // Assuming AdResponse is Serializable or Parcelable
            val adResponse = intent.getSerializableExtra("adResponse") as? AdResponse
            adResponse?.let {
                updateUIWithAdResponse(it)
            }
        }


        private fun updateUIWithAdResponse(adResponse: AdResponse) {
            // Use Glide to load images into ImageView widgets
            Glide.with(this).load(adResponse.gifUrl).into(binding.adGifImageView)

            // Log message to indicate that we have the adResponse
            Log.d("MidAisleActivity", "Received AdResponse with couponCode: ${adResponse.couponCode}")

        // Set text on TextViews
        binding.couponHeaderTextView.text = adResponse.couponTextHeader
        binding.couponOfferTextView.text = adResponse.couponTextOffer
        binding.couponFooterTextView.text = adResponse.couponTextFooter
            Log.d("MidAisleActivity", "Received AdResponse with coupontextheader: ${adResponse.couponTextHeader}")


            val couponCode = adResponse.couponCode
            if (couponCode != null) {
                try {
                    // Generate QR Code bitmap
                    val bitmap = generateQRCode(couponCode)
                    // Log message to indicate the QR Code was generated successfully
                    Log.d("MidAisleActivity", "QR Code bitmap generated successfully.")

                    // Set the QR Code bitmap to the ImageView
                    qrCodeImageView.setImageBitmap(bitmap)
                    // Log message to indicate the QR Code was set to the ImageView
                    Log.d("MidAisleActivity", "QR Code bitmap set to ImageView.")
                } catch (e: Exception) {
                    // Log an error message if there's an exception
                    Log.e("MidAisleActivity", "Error generating QR code", e)
                }
            } else {
                // Log an error message if the coupon code is null
                Log.e("MidAisleActivity", "Coupon code is null, cannot generate QR code.")
            }
    }
}
