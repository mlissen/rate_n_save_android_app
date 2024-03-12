package com.example.ratensaveandroidapp

import android.animation.PropertyValuesHolder
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import android.view.View
import android.view.WindowInsets
import android.view.WindowInsetsController
import androidx.appcompat.app.AppCompatActivity
import com.example.ratensaveandroidapp.databinding.MidAisleMediumLayoutBinding
import com.bumptech.glide.Glide
import com.example.ratensaveandroidapp.datamodel.AdResponse
import com.example.ratensaveandroidapp.utils.QRCodeGenerator.generateQRCode
import android.util.*
import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ObjectAnimator
import android.os.Build
import android.view.animation.AccelerateDecelerateInterpolator
import androidx.annotation.RequiresApi
import android.content.Intent
import android.os.Handler
import android.os.Looper
import androidx.lifecycle.ViewModelProvider
import com.example.ratensaveandroidapp.viewmodel.AuctionViewModel

class MidAisleMediumActivity : AppCompatActivity() {

    private lateinit var qrCodeImageView: ImageView
    private lateinit var binding: MidAisleMediumLayoutBinding
    private val handler = Handler(Looper.getMainLooper())
    private lateinit var viewModel: AuctionViewModel

    @RequiresApi(Build.VERSION_CODES.R)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = MidAisleMediumLayoutBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel = ViewModelProvider(this).get(AuctionViewModel::class.java)

        qrCodeImageView = binding.qrCodeImageView
        processAdResponse()

        hideSystemUI()

        binding.exitButton.setOnClickListener {
            // Exiting Kiosk Mode and navigating back to HomeActivity
            showSystemUI()
            navigateToHomeActivity()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacksAndMessages(null) // Cancel all scheduled Runnable tasks
    }

    private fun hideSystemUI() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.insetsController?.apply {
                systemBarsBehavior = WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
                hide(WindowInsets.Type.systemBars())
            }
        } else {
            // Use older APIs for Android versions before R
            @Suppress("DEPRECATION")
            window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_FULLSCREEN
                    or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY)
        }
    }

    private fun showSystemUI() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.insetsController?.show(WindowInsets.Type.systemBars())
        } else {
            // Use older APIs for Android versions before R
            @Suppress("DEPRECATION")
            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_VISIBLE
        }
    }

    private fun processAdResponse() {
        val adResponse = intent.getSerializableExtra("adResponse") as? AdResponse
        adResponse?.let {
            updateUIWithAdResponse(it)
            it.minutesToNextAuction?.let { minutesToAuction ->
                if (minutesToAuction > 0) {
                    scheduleNextAuction(minutesToAuction)
                } else {
                    navigateToHomeActivity()
                }
            }
        } ?: run {
            Log.e("MidAisleMediumActivity", "AdResponse is missing")
            navigateToHomeActivity()
        }
    }
    private fun scheduleNextAuction(minutesToNextAuction: Int?) {
        minutesToNextAuction?.let {
            val delayMillis = it * 60 * 1000L
            handler.postDelayed({
                startNextAuction()
            }, delayMillis)
        } ?: run {
            Log.e("MidAisleMediumActivity", "Minutes to next auction is null")
            navigateToHomeActivity()
        }
    }

    private fun startNextAuction() {
        val placementId = "d1a911b8-4f98-4ba8-9a44-c5fc5a953f0c"
        Log.d("MidAisleMediumActivity", "Starting next auction with placement ID: $placementId")
        viewModel.startAuction(placementId)// Replace with actual logic to start the next auction
        viewModel.adResponse.observe(this) { adResponse ->
            updateUIWithAdResponse(adResponse)
            // Optionally reset the timer based on the new adResponse.minutesToNextAuction
            adResponse.minutesToNextAuction?.let {
                if (it > 0) {
                    scheduleNextAuction(it)
                } else {
                    navigateToHomeActivity()
                }
            }
        }
    }

    private fun navigateToHomeActivity() {
        val intent = Intent(this, HomeActivity::class.java)
        startActivity(intent)
        finish() // Exit this activity to return to HomeActivity
    }

    private fun updateUIWithAdResponse(adResponse: AdResponse) {
        Log.d("MidAisleMediumActivity", "Updating UI with new ad response: $adResponse")
        Glide.with(this).load(adResponse.gifUrl).into(binding.adGifImageView)
        binding.couponHeaderTextView.text = adResponse.couponTextHeader
        binding.couponOfferTextView.text = adResponse.couponTextOffer
        binding.couponFooterTextView.text = adResponse.couponTextFooter
        val couponCode = adResponse.couponCode
        if (couponCode != null) {
            try {
                val bitmap = generateQRCode(couponCode)
                qrCodeImageView.setImageBitmap(bitmap)
            } catch (e: Exception) {
                Log.e("MidAisleActivity", "Error generating QR code", e)
            }
        } else {
            Log.e("MidAisleActivity", "Coupon code is null, cannot generate QR code.")
        }

        // Animate the ctaTextView
        jiggleTextView(binding.ctaTextView)
    }

    private fun jiggleTextView(textView: TextView) {
        // Define the jiggle animation
        val animator = ObjectAnimator.ofFloat(textView, "rotation", -5f, 5f, -5f).apply {
            duration = 1000 // Duration of one jiggle cycle
            repeatMode = ObjectAnimator.REVERSE
            interpolator = AccelerateDecelerateInterpolator()
        }

        // Define the listener to handle the end of the animation
        animator.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                super.onAnimationEnd(animation)
                // Delay before starting the next animation cycle
                textView.postDelayed({
                    animator.start() // Restart the animation after a delay
                }, 3000) // Delay in milliseconds between jiggles
            }
        })

        // Start the first animation cycle
        animator.start()
    }


    private fun animateTextView(textView: TextView) {
        Log.d("MidAisleActivity", "Animating text view")
        // PropertyValuesHolders for scaling up and down
        //val scaleXUp = PropertyValuesHolder.ofFloat("scaleX", 1.4f)
        //val scaleYUp = PropertyValuesHolder.ofFloat("scaleY", 1.4f)
        val scaleXDown = PropertyValuesHolder.ofFloat("scaleX", 0.8f)
        val scaleYDown = PropertyValuesHolder.ofFloat("scaleY", 0.8f)

        // ObjectAnimator to scale up and down
        val animator = ObjectAnimator.ofPropertyValuesHolder(
            textView,  scaleXDown, scaleYDown
        ).apply {
            duration = 1000 // duration for each scale operation
            repeatCount = ObjectAnimator.INFINITE // repeat indefinitely
            repeatMode = ObjectAnimator.REVERSE // reverse at each repeat
            interpolator = AccelerateDecelerateInterpolator() // smooth animation
        }

        animator.start() // Start the animation
    }
}
