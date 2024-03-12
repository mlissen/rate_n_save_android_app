package com.example.ratensaveandroidapp

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ObjectAnimator
import android.animation.PropertyValuesHolder
import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.*
import android.view.View
import android.view.WindowInsets
import android.view.WindowInsetsController
import android.view.WindowManager
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.example.ratensaveandroidapp.databinding.MidAisleMediumLayoutBinding
import com.example.ratensaveandroidapp.datamodel.AdResponse
import com.example.ratensaveandroidapp.utils.QRCodeGenerator.generateQRCode
import com.example.ratensaveandroidapp.viewmodel.AuctionViewModel
import android.widget.VideoView


class MidAisleMediumActivity : AppCompatActivity() {

    private lateinit var qrCodeImageView: ImageView
    private lateinit var binding: MidAisleMediumLayoutBinding
    private val handler = Handler(Looper.getMainLooper())
    private lateinit var viewModel: AuctionViewModel
    private var placementId: String? = null

    @RequiresApi(Build.VERSION_CODES.R)
    override fun onCreate(savedInstanceState: Bundle?) {
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        super.onCreate(savedInstanceState)
        binding = MidAisleMediumLayoutBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Keep screen on
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        viewModel = ViewModelProvider(this).get(AuctionViewModel::class.java)
        retrievePlacementId()

        observeAdResponse() // Observe the LiveData here
        //qrCodeImageView = binding.qrCodeImageView
        processAdResponse()

        val backButton = findViewById<Button>(R.id.backButton)
        backButton.setOnClickListener {
            Log.d("MidAisleMediumActivity", "Back button clicked!")
            navigateToHomeActivity()
        }


        hideSystemUI()

        /*binding.exitButton.setOnClickListener {
            // Exiting Kiosk Mode and navigating back to HomeActivity
            showSystemUI()
            navigateToHomeActivity()
        }*/
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
    private fun retrievePlacementId() {
        val sharedPref = this.getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        placementId = sharedPref.getString("placementId", "")
        if (placementId.isNullOrBlank()) {
            Log.e("MidAisleMediumActivity", "No placement ID found")
            // Handle the case where no placement ID is found, maybe navigate back or show a message
        } else {
            Log.d("MidAisleMediumActivity", "Retrieved Placement ID: $placementId")
            // You can now use placementId for further operations, like starting an auction
        }
    }

    private fun observeAdResponse() {
        // This ensures you only add the observer once during the Activity lifecycle
        viewModel.adResponse.observe(this) { adResponse ->
            // Update UI with new AdResponse
            updateUIWithAdResponse(adResponse)
            // Decide what to do next based on minutesToNextAuction
            adResponse.minutesToNextAuction?.let {
                if (it > 0) {
                    scheduleNextAuction(it)
                } else {
                    navigateToHomeActivity()
                }
            } ?: run {
                Log.e("MidAisleMediumActivity", "minutesToNextAuction is null or not provided")
                navigateToHomeActivity()
            }
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

    private fun scheduleNextAuction(minutesToNextAuction: Int) {
        val delayMillis = minutesToNextAuction * 60 * 1000L
        handler.postDelayed({
            startNextAuction()
        }, delayMillis)
    }

    // ...

    private fun startNextAuction() {
        val sharedPref = getSharedPreferences("AppSettings", Context.MODE_PRIVATE)
        val placementId = sharedPref.getString("placementId", null) // Use a default value or handle null appropriately


        if (placementId != null && placementId.isNotBlank()) { // Ensure ID exists
            Log.d("MidAisleMediumActivity", "Scheduling next auction with placement ID: $placementId")
            viewModel.startAuction(placementId)
        } else {
            Log.e("MidAisleMediumActivity", "No placement ID found, navigating back to HomeActivity")
            navigateToHomeActivity()
        }
    }

    private fun storePlacementId(placementId: String) {
        val sharedPref = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        with(sharedPref.edit()) {
            putString("placementId", placementId)
            apply()
        }
    }

    private fun navigateToHomeActivity() {
        val intent = Intent(this, HomeActivity::class.java)
        startActivity(intent)
        finish() // Exit this activity to return to HomeActivity
    }

    private fun updateUIWithAdResponse(adResponse: AdResponse) {
        Log.d("MidAisleMediumActivity", "Updating UI with new ad response: $adResponse")

        // Display QR code
        /*adResponse.couponCode?.let {
            try {
                val qrCodeBitmap = generateQRCode(it)
                binding.qrCodeImageView.setImageBitmap(qrCodeBitmap)            } catch (e: Exception) {
                Log.e("MidAisleActivity", "Error generating QR code", e)
            }
        }*/

        // Display offerCode directly
        binding.offerCodeTextView.text = adResponse.couponCode
        binding.offerCodeTextView.visibility = View.VISIBLE

        // Display based on ad type
        when (adResponse.adType) {
            "VERTICAL_VIDEO" -> displayVideo(adResponse.creativeUrl)
            "IMAGE", "GIF" -> displayImage(adResponse.creativeUrl)
            else -> {
                // Handle unsupported types or missing adType
                binding.creativeImageView.visibility = View.GONE
                binding.creativeVideoView.visibility = View.GONE
            }
        }

        // Update text views
        binding.couponHeaderTextView.text = adResponse.couponTextHeader
        binding.couponOfferTextView.text = adResponse.couponTextOffer
        binding.couponFooterTextView.text = adResponse.couponTextFooter

        // Animate the CTA text view
        jiggleTextView(binding.ctaTextView)
    }

    private fun displayImage(url: String) {
        Glide.with(this)
            .load(url)
            .into(binding.creativeImageView)

        binding.creativeImageView.visibility = View.VISIBLE
        binding.creativeVideoView.visibility = View.GONE
    }
    private fun displayVideo(url: String) {
        val videoView = binding.creativeVideoView as VideoView
        val uri = Uri.parse(url)
        videoView.setVideoURI(uri)
        videoView.setOnPreparedListener { mp ->
            mp.isLooping = true
            videoView.start()
        }
        binding.creativeImageView.visibility = View.GONE
        videoView.visibility = View.VISIBLE
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
