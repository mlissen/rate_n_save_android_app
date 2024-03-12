/*package com.example.ratensaveandroidapp

import android.animation.PropertyValuesHolder
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import android.widget.Button
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
import android.os.CountDownTimer
import android.widget.ProgressBar

class MidAisleMediumActivityWCountdown : AppCompatActivity() {

    private lateinit var qrCodeImageView: ImageView
    private lateinit var binding: MidAisleMediumLayoutBinding
    private lateinit var countdownTimer: CountDownTimer
    private lateinit var timerTextView: TextView
    private lateinit var auctionProgressBar: ProgressBar


    @RequiresApi(Build.VERSION_CODES.R)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = MidAisleMediumLayoutBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize qrCodeImageView, timerTextView, and auctionProgressBar right after setContentView
        timerTextView = binding.timerTextView // Ensure you have a timerTextView defined in your layout XML and binding class
 // Ensure you have an auctionProgressBar defined in your layout XML and binding class

        qrCodeImageView = binding.qrCodeImageView
        val adResponse = intent.getSerializableExtra("adResponse") as? AdResponse
        adResponse?.let {
            updateUIWithAdResponse(it)
            // Now inside the 'let' block:
            val minutesToAuction = it.minutesToNextAuction // Get the value
            if (minutesToAuction != null) {
                startCountdownTimer(minutesToAuction)
            } else {
                // Handle the scenario where minutes_to_next_auction is not available
                val intent = Intent(this, HomeActivity::class.java)
                startActivity(intent)  // Navigate back to HomeActivity
            }
        }
        hideSystemUI()


        val exitButton = findViewById<Button>(R.id.exitButton)
        exitButton.setOnClickListener {
            // Code to exit Kiosk Mode and restore system UI
            //finish() // Example action to leave the activity. Adjust as needed.
            val intent = Intent(this, HomeActivity::class.java)
            startActivity(intent)

        }
    }

    @RequiresApi(Build.VERSION_CODES.R)
    private fun hideSystemUI() {
        window.insetsController?.let {
            it.systemBarsBehavior = WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            it.hide(WindowInsets.Type.systemBars())
        }
    }

    private fun startCountdownTimer(minutesToAuction: Int) {
        Log.d("MidAisleActivity", "Starting countdown timer for $minutesToAuction minutes.")
        val timeUntilAuction = minutesToAuction * 60 * 1000 // Convert minutes to milliseconds

        val countDownTimer = object : CountDownTimer(timeUntilAuction.toLong(), 1000) {
            override fun onTick(millisUntilFinished: Long) {
                val secondsRemaining = millisUntilFinished / 1000
                val minutes = secondsRemaining / 60
                val seconds = secondsRemaining % 60

                val timeFormatted = String.format("%02d:%02d", minutes, seconds)
                Log.d("MidAisleActivity", "Time left: $timeFormatted")
                timerTextView.text = timeFormatted

                // Calculate percentage for progress bar
                val progress = (timeUntilAuction - millisUntilFinished) * 100 / timeUntilAuction
                auctionProgressBar.progress = progress.toInt()
            }

            override fun onFinish() {
                timerTextView.text = "Auction Starting!" // Or your desired action
            }
        }.start()
    }

    private fun updateUIWithAdResponse(adResponse: AdResponse) {
        Glide.with(this).load(adResponse.creativeUrl).into(binding.adGifImageView)
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
*/