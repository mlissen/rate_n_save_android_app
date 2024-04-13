package com.example.ratensaveandroidapp

import android.animation.Animator
import android.animation.ObjectAnimator
import android.animation.PropertyValuesHolder
import android.view.animation.AccelerateDecelerateInterpolator
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsAnimationCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.ActivityInfo
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.view.WindowInsets
import android.view.WindowInsetsController
import android.view.WindowManager
import android.widget.Button
import android.widget.TextView
import androidx.annotation.OptIn
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.common.MediaItem;
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DefaultDataSourceFactory
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.exoplayer.source.MediaSource;
import androidx.media3.exoplayer.source.ProgressiveMediaSource;
import com.bumptech.glide.Glide
import com.example.ratensaveandroidapp.databinding.MidAisleMediumLayoutBinding
import com.example.ratensaveandroidapp.datamodel.AdResponse
import com.example.ratensaveandroidapp.viewmodel.AuctionViewModel
import java.time.Duration
import java.time.LocalDateTime
import java.time.LocalDate
import java.time.LocalTime
import java.util.Timer
import java.util.TimerTask

class MidAisleMediumActivity : AppCompatActivity() {

    private lateinit var binding: MidAisleMediumLayoutBinding
    private lateinit var exoPlayer: ExoPlayer
    private val handler = Handler(Looper.getMainLooper())
    private lateinit var viewModel: AuctionViewModel
    private var placementId: String? = null
    private lateinit var timer: Timer

    @OptIn(UnstableApi::class) @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        binding = MidAisleMediumLayoutBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize the timer as early as possible
        timer = Timer()

        // Create and set up the ExoPlayer object
        exoPlayer = ExoPlayer.Builder(this).build()
        binding.videoView.player = exoPlayer
        binding.videoView.useController = false

        exoPlayer.addListener(object : Player.Listener {
            override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
                Log.d("ExoPlayer", "Player State Changed: $playbackState")
                super.onPlayerStateChanged(playWhenReady, playbackState)
            }
            override fun onPlayerError(error: PlaybackException) {
                Log.e("ExoPlayer", "Playback error: ${error.message}", error)
            }
        })

        // Keep screen on
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        viewModel = ViewModelProvider(this).get(AuctionViewModel::class.java)
        retrievePlacementId()

        Log.d("MidAisleMediumActivity", "LISS starting observer")
        observeAdResponse() // Observe the LiveData here

        val adResponse = intent.getSerializableExtra("adResponse") as AdResponse?
        Log.d("MidAisleMediumActivity", "LISS ${adResponse?.storeCloseTime} ${adResponse?.storeOpenTime} ${adResponse?.nextOpeningTime}")
        if (adResponse != null) {
            updateUIWithAdResponse(adResponse)
            adResponse.storeOpenTime?.let { openTime ->
                adResponse.storeCloseTime?.let { closeTime ->
                    adResponse.nextOpeningTime?.let { nextOpeningTime ->
                        if (isStoreOpen(openTime, closeTime)) {
                            scheduleNextAuction(adResponse.minutesToNextAuction, openTime, closeTime, nextOpeningTime)
                        } else {
                            pauseAdvertisement()
                        }
                    }
                }
            }
        } else {
            startNextAuction()
        }

        val backButton = findViewById<Button>(R.id.backButton)
        backButton.setOnClickListener {
            Log.d("MidAisleMediumActivity", "Back button clicked!")
            navigateToHomeActivity()
        }

        hideSystemUI()
    }

    private fun pauseAdvertisement() {
        // Pause the video
        exoPlayer.pause()

        // Hide the video player and any relevant UI components
        binding.videoView.visibility = View.GONE  // Assuming this is your video player view
        binding.ctaTextView.visibility = View.GONE       // Assuming ctaTextView is the CTA text view
        binding.creativeImageView.visibility = View.GONE // Just in case this is also on the screen

        // Make screen black
        makeScreenBlack()
    }

    private fun makeScreenBlack() {
        // Set the background color of the root view to black
        binding.root.setBackgroundColor(Color.BLACK)
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
        val sharedPref = this.getSharedPreferences("MyPrefs", MODE_PRIVATE)
        placementId = sharedPref.getString("placementId", "")
        if (placementId.isNullOrBlank()) {
            Log.e("MidAisleMediumActivity", "No placement ID found")
            // Handle the case where no placement ID is found, maybe navigate back or show a message
        } else {
            Log.d("MidAisleMediumActivity", "Retrieved Placement ID: $placementId")
            // You can now use placementId for further operations, like starting an auction
        }
    }


    @RequiresApi(Build.VERSION_CODES.O)
    private fun observeAdResponse() {
        // This ensures you only add the observer once during the Activity lifecycle
        viewModel.adResponse.observe(this) { adResponse ->
            Log.d("MidAisleMediumActivity", "LISS Observer triggered with response: $adResponse")
            Log.d("MidAisleMediumActivity", "LISS creativeUrl: ${adResponse.creativeUrl}")
            if (adResponse != null) {
                Log.d("MidAisleMediumActivity", "LISS Updating UI with new ad response: $adResponse")
                Log.d("MidAisleMediumActivity", "LISS Auction results received: $adResponse")
                Log.d("MidAisleMediumActivity", "LISS Updating UI with new ad response:")
                Log.d("MidAisleMediumActivity", "LISS adType: ${adResponse.adType}")
                Log.d("MidAisleMediumActivity", "LISS creativeUrl: ${adResponse.creativeUrl}")
                Log.d("MidAisleMediumActivity", "LISS creativeUrl: ${adResponse.creativeUrl}")
                updateUIWithAdResponse(adResponse)  // Your method to update UI
            } else {
                Log.e("MidAisleMediumActivity", "LISS Failed to receive auction results")
            }

            // Decide what to do next based on minutesToNextAuction

                if (adResponse != null) {
                    updateUIWithAdResponse(adResponse)
                    adResponse.storeOpenTime?.let { openTime ->
                        adResponse.storeCloseTime?.let { closeTime ->
                            adResponse.nextOpeningTime?.let { nextOpeningTime ->
                                if (isStoreOpen(openTime, closeTime)) {
                                    scheduleNextAuction(adResponse.minutesToNextAuction, closeTime, openTime, nextOpeningTime)
                                } else {
                                    pauseAdvertisement()
                                }
                            }
                        }
                    }
                } else {
                    navigateToHomeActivity()
                }

        }
    }

    private fun updateUIWithAdResponse(adResponse: AdResponse) {
        Log.d("MidAisleMediumActivity", "LISS Updating UI with new ad response: $adResponse")
        Log.d("MidAisleMediumActivity", "LISS adType: ${adResponse.adType}")
        Log.d("MidAisleMediumActivity", "LISS creativeUrl: ${adResponse.creativeUrl}")
        Log.d("MidAisleMediumActivity", "LISS minutesToNextAuction: ${adResponse.minutesToNextAuction}")
        Log.d("MidAisleMediumActivity", "LISS storeOpenTime: ${adResponse.storeOpenTime}")
        Log.d("MidAisleMediumActivity", "LISS storeCloseTime: ${adResponse.storeCloseTime}")
        Log.d("MidAisleMediumActivity", "LISS nextOpeningTime: ${adResponse.nextOpeningTime}")

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
                binding.videoView.visibility = View.GONE
            }
        }

        // Update text views
        binding.couponHeaderTextView.text = adResponse.couponTextHeader
        binding.couponOfferTextView.text = adResponse.couponTextOffer
        binding.couponFooterTextView.text = adResponse.couponTextFooter

        // Animate the CTA text view
        jiggleTextView(binding.ctaTextView)

        adResponse.storeOpenTime?.let { openTime ->
            adResponse.storeCloseTime?.let { closeTime ->
                startPeriodicTimeCheck(openTime, closeTime)
            }
        }
    }

    private fun displayImage(url: String) {
        Glide.with(this)
            .load(url)
            .into(binding.creativeImageView)

        binding.creativeImageView.visibility = View.VISIBLE
        binding.videoView.visibility = View.GONE
    }

    private fun displayVideo(url: String) {
        Log.d("MidAisleMediumActivity", "Displaying video: ${url}")
        val dataSourceFactory = DefaultHttpDataSource.Factory().setUserAgent("ScanFan App")
        val mediaSource: MediaSource = ProgressiveMediaSource.Factory(dataSourceFactory)
            .createMediaSource(MediaItem.fromUri(url))

        exoPlayer.setMediaSource(mediaSource)
        exoPlayer.prepare()
        exoPlayer.play()
        exoPlayer.repeatMode = Player.REPEAT_MODE_ONE

        binding.creativeImageView.visibility = View.GONE
        binding.videoView.visibility = View.VISIBLE
    }

    private fun jiggleTextView(textView: TextView) {
        // Define the jiggle animation
        val animator = ObjectAnimator.ofFloat(textView, "rotation", -5f, 5f, -5f).apply {
            duration = 1000 // Duration of one jiggle cycle
            // Set repeat count to repeat indefinitely
            repeatCount = ObjectAnimator.INFINITE
            interpolator = AccelerateDecelerateInterpolator()
        }

        animator.addListener(object : Animator.AnimatorListener {
            override fun onAnimationStart(animation: Animator) {
                // This method is called when the animation starts
            }

            override fun onAnimationEnd(animation: Animator) {
                // Delay before starting the next animation cycle
                textView.postDelayed({
                    animator.start() // Restart the animation after a delay
                }, 3000) // Delay in milliseconds between jiggles
            }

            override fun onAnimationCancel(animation: Animator) {
                // This method is called when the animation is canceled
            }

            override fun onAnimationRepeat(animation: Animator) {
                // This method is called when the animation repeats
            }
        })

        // Start the first animation cycle
        animator.start()
    }

    private fun startNextAuction() {
        val sharedPref = getSharedPreferences("AppSettings", MODE_PRIVATE)
        val placementId = sharedPref.getString("placementId", null) // Use a default value or handle null appropriately

        if (placementId != null && placementId.isNotBlank()) { // Ensure ID exists
            Log.d("MidAisleMediumActivity", "LISS Starting next auction with placement ID: $placementId")
            viewModel.startAuction(placementId)
        } else {
            Log.e("MidAisleMediumActivity", "LISS No placement ID found, navigating back to HomeActivity")
            navigateToHomeActivity()
        }
    }

    @SuppressLint("InlinedApi")
    private fun hideSystemUi() {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        WindowInsetsControllerCompat(window, binding.videoView).let { controller ->
            controller.hide(WindowInsetsCompat.Type.systemBars())
            controller.systemBarsBehavior =
                WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }
    }

    private fun startPeriodicTimeCheck(openTime: String, closeTime: String) {
        Log.e("MidAisleMediumActivity", "LISS Starting time check")
        val timerTask = object : TimerTask() {
            @RequiresApi(Build.VERSION_CODES.O)
            override fun run() {
                if (isStoreOpen(openTime, closeTime)) {
                    // Store is open, resume the advertisement if it was paused
                    Log.e("MidAisleMediumActivity", "LISS Store is open ")
                    runOnUiThread {
                        resumeAdvertisement()
                    }
                } else {
                    // Store is closed, pause the advertisement and make the screen black
                    Log.e("MidAisleMediumActivity", "LISS Store is closed ")
                    runOnUiThread {
                        pauseAdvertisement()
                        makeScreenBlack()
                    }
                }
            }
        }

        // Schedule the timer task to run every minute
        timer.scheduleAtFixedRate(timerTask, 0, 60000)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun scheduleNextAuction(minutesToNextAuction: Int, storeCloseTime: String, storeOpenTime: String, nextOpeningTime: String) {
        Log.d("MidAisleMediumActivity", "LISS Scheduling next auction. Current time: ${LocalDateTime.now()}")

        // Get the current date and time
        val currentTime = LocalDateTime.now()

        // Calculate the next auction time by adding the minutes to the current time
        val nextAuctionTime = currentTime.plusMinutes(minutesToNextAuction.toLong())
        Log.d("MidAisleMediumActivity", "LISS Calculated next auction time: $nextAuctionTime")

        // Parse the store's closing time from string to LocalTime
        val storeCloseTimeLocal = storeCloseTime.let { LocalTime.parse(it) }
        val closingDateTime = LocalDateTime.of(currentTime.toLocalDate(), storeCloseTimeLocal)
        Log.d("MidAisleMediumActivity", "LISS Store closing time (DateTime): $closingDateTime")

        // Parse the next day's opening time from string to LocalTime
        val nextOpeningTimeLocal = LocalTime.parse(nextOpeningTime)
        Log.d("MidAisleMediumActivity", "LISS Next opening time: $nextOpeningTimeLocal")

        // Create a LocalDateTime object using the next opening time and today's date
        val nextOpeningDateTime = LocalDateTime.of(LocalDate.now(), nextOpeningTimeLocal)

        // Calculate the delay in milliseconds
        val delayMillis = if (nextAuctionTime.isAfter(closingDateTime)) {
            Log.d("MidAisleMediumActivity", "LISS Next auction time is after store closing time.")
            val durationUntilClose = Duration.between(currentTime, closingDateTime)
            val durationUntilNextOpen = Duration.between(closingDateTime, nextOpeningDateTime)
            // Add the duration until next open to the original delay (minutesToNextAuction)
            durationUntilClose.plus(durationUntilNextOpen).toMillis()
        } else {
            Log.d("MidAisleMediumActivity", "LISS Next auction time is before store closing time.")
            Duration.between(currentTime, nextAuctionTime).toMillis()
        }
        Log.d("MidAisleMediumActivity", "LISS Total delay until next auction: $delayMillis ms")

        // Schedule the startNextAuction() function to be called after the calculated delay
        handler.postDelayed({ startNextAuction() }, delayMillis)
    }


    private fun resumeAdvertisement() {
        // Resume the video
        exoPlayer.play()
        binding.videoView.visibility = View.VISIBLE
        binding.root.setBackgroundColor(Color.WHITE)
    }
    private fun storePlacementId(placementId: String) {
        val sharedPref = getSharedPreferences("MyPrefs", MODE_PRIVATE)
        with(sharedPref.edit()) {
            putString("placementId", placementId)
            apply()
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun isStoreOpen(openTime: String, closeTime: String): Boolean {
        val currentTime = LocalTime.now()
        Log.d("MidAisleMediumActivity", "LISS Local time: $currentTime")
        val openTimeLocal = LocalTime.parse(openTime)
        val closeTimeLocal = LocalTime.parse(closeTime)
        Log.d("MidAisleMediumActivity", "LISS Open time local: $openTimeLocal")
        Log.d("MidAisleMediumActivity", "LISS Closing time local: $closeTimeLocal")

        return if (closeTimeLocal.isAfter(openTimeLocal)) {
            // Normal case: the store closes later on the same day it opens
            currentTime.isAfter(openTimeLocal) && currentTime.isBefore(closeTimeLocal)
        } else {
            // Overnight case: the store closes after midnight on the following day
            currentTime.isAfter(openTimeLocal) || currentTime.isBefore(closeTimeLocal)
        }
    }


    private fun navigateToHomeActivity() {
        val intent = Intent(this, HomeActivity::class.java)
        startActivity(intent)
        finish() // Exit this activity to return to HomeActivity
    }
    override fun onDestroy() {
        super.onDestroy()
        exoPlayer.release()
        handler.removeCallbacksAndMessages(null)
        timer.cancel()
    }

}