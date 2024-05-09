package com.example.ratensaveandroidapp

import android.animation.Animator
import android.animation.ObjectAnimator
import android.view.animation.AccelerateDecelerateInterpolator
import androidx.core.view.WindowCompat
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
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.source.ProgressiveMediaSource
import com.bumptech.glide.Glide
import com.example.ratensaveandroidapp.databinding.MidAisleMediumLayoutBinding
import com.example.ratensaveandroidapp.datamodel.AdResponse
import com.example.ratensaveandroidapp.viewmodel.AuctionViewModel
import java.time.Duration
import java.time.LocalDateTime
import java.time.LocalDate
import java.time.LocalTime
import androidx.media3.datasource.cache.CacheDataSource
import androidx.media3.datasource.cache.LeastRecentlyUsedCacheEvictor
import androidx.media3.datasource.cache.SimpleCache
import androidx.media3.exoplayer.DefaultLoadControl
import androidx.media3.exoplayer.trackselection.DefaultTrackSelector
import androidx.media3.exoplayer.upstream.DefaultAllocator
import androidx.media3.common.C
import java.io.File
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import android.graphics.drawable.Drawable
import androidx.media3.datasource.DefaultDataSourceFactory


class MidAisleMediumActivity : AppCompatActivity() {

    private lateinit var binding: MidAisleMediumLayoutBinding
    private lateinit var exoPlayer: ExoPlayer
    private lateinit var viewModel: AuctionViewModel
    private var placementId: String? = null
    //private lateinit var timer: Timer
    private lateinit var handler: Handler
    //private lateinit var checkStoreStatusRunnable: Runnable
    private lateinit var periodicTimeCheckRunnable: Runnable
    private lateinit var simpleCache: SimpleCache

    @OptIn(UnstableApi::class) @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        binding = MidAisleMediumLayoutBinding.inflate(layoutInflater)
        setContentView(binding.root)
        // Create the SimpleCache instance with a DatabaseProvider
        // Initialize the SimpleCache instance
        val cacheDirectory = File(applicationContext.cacheDir, "video-cache")
        val cacheSize = 100 * 1024 * 1024 // 100 MB
        val lruCacheEvictor = LeastRecentlyUsedCacheEvictor(cacheSize.toLong())
        simpleCache = SimpleCache(cacheDirectory, lruCacheEvictor)


        // Initialize the timer as early as possible
        //timer = Timer()
        // Initialize the timer and handler
        handler = Handler(Looper.getMainLooper())

        // Setup the periodic check
        //setupPeriodicCheck()

        // Create and set up the ExoPlayer object
        val loadControl = DefaultLoadControl.Builder()
            .setAllocator(DefaultAllocator(true, C.DEFAULT_BUFFER_SEGMENT_SIZE))
            .setBufferDurationsMs(
                DefaultLoadControl.DEFAULT_MIN_BUFFER_MS / 2,
                DefaultLoadControl.DEFAULT_MAX_BUFFER_MS / 2,
                DefaultLoadControl.DEFAULT_BUFFER_FOR_PLAYBACK_MS / 2,
                DefaultLoadControl.DEFAULT_BUFFER_FOR_PLAYBACK_AFTER_REBUFFER_MS / 2
            )
            .setTargetBufferBytes(DefaultLoadControl.DEFAULT_TARGET_BUFFER_BYTES / 2)
            .setPrioritizeTimeOverSizeThresholds(true)
            .build()

        val trackSelector = DefaultTrackSelector(this)
        exoPlayer = ExoPlayer.Builder(this)
            .setTrackSelector(trackSelector)
            .setLoadControl(loadControl)
            .build()

        binding.videoView.player = exoPlayer
        binding.videoView.useController = false
        exoPlayer.addListener(object : Player.Listener {
            @Deprecated("Deprecated in Java")
            override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
                if (playbackState == Player.STATE_BUFFERING) {
                    // Handle buffering state if needed
                } else if (playbackState == Player.STATE_READY) {
                    // Handle ready state if needed
                }
            }

            override fun onPlayerError(error: PlaybackException) {
                Log.e("ExoPlayer", "Playback error: ${error.message}", error)
                // Consider retrying the playback or loading alternative content
            }
        })


        // Keep screen on
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        viewModel = ViewModelProvider(this).get(AuctionViewModel::class.java)
        retrievePlacementId()

        Log.d("MidAisleMediumActivity", "LISS starting observer")
        observeAdResponse() // Observe the LiveData here

        val adResponse = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getSerializableExtra("adResponse", AdResponse::class.java)
        } else {
            @Suppress("DEPRECATION")
            intent.getSerializableExtra("adResponse") as? AdResponse
        }
        Log.d("MidAisleMediumActivity", "LISS Received adResponse: $adResponse")
        Log.d("MidAisleMediumActivity", "LISS ${adResponse?.storeCloseTime} ${adResponse?.storeOpenTime} ${adResponse?.nextOpeningTime}")
        if (adResponse != null) {
            Log.d("MidAisleMediumActivity", "LISS Updating UI with adResponse")
            updateUIWithAdResponse(adResponse)
            adResponse.storeOpenTime.let { openTime ->
                adResponse.storeCloseTime.let { closeTime ->
                    adResponse.nextOpeningTime.let { nextOpeningTime ->
                        if (isStoreOpen(openTime, closeTime)) {
                            scheduleNextAuction(adResponse.minutesToNextAuction, openTime, closeTime, nextOpeningTime)
                        } else {
                            scheduleNextAuction(adResponse.minutesToNextAuction, openTime, closeTime, nextOpeningTime)
                            pauseAdvertisement()
                        }
                    }
                }
            }
        } else {
            Log.d("MidAisleMediumActivity", "LISS adResponse is null, starting next auction")
            startNextAuction()
        }

        val backButton = findViewById<Button>(R.id.backButton)
        backButton.setOnClickListener {
            Log.d("MidAisleMediumActivity", "Back button clicked!")
            navigateToHomeActivity()
        }

        hideSystemUI()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun startPeriodicTimeCheck(adType: String, openTime: String, closeTime: String) {
        Log.e("MidAisleMediumActivity", "LISS Starting time check")
        periodicTimeCheckRunnable = Runnable {
            if (isStoreOpen(openTime, closeTime)) {
                // Store is open, resume the advertisement if it was paused
                Log.e("MidAisleMediumActivity", "LISS Store is open")
                resumeAdvertisement(adType)
            } else {
                // Store is closed, pause the advertisement and make the screen black
                Log.e("MidAisleMediumActivity", "LISS Store is closed")
                pauseAdvertisement()
                makeScreenBlack()
            }
            // Schedule the next periodic time check
            handler.postDelayed(periodicTimeCheckRunnable, 60000)
        }

        // Start the periodic time check
        handler.post(periodicTimeCheckRunnable)
    }
    /*private fun setupPeriodicCheck() {
        checkStoreStatusRunnable = Runnable {
            val adResponse = intent.getSerializableExtra("adResponse") as AdResponse?
            adResponse?.let {
                val openTime = it.storeOpenTime ?: "08:00"  // Example default open time
                val closeTime = it.storeCloseTime ?: "21:00" // Example default close time
                if (isStoreOpen(openTime, closeTime)) {
                    runOnUiThread { resumeAdvertisement() }
                } else {
                    runOnUiThread { pauseAdvertisement() }
                }
            }
            handler.postDelayed(checkStoreStatusRunnable, 60000) // Re-post the Runnable every 60 seconds
        }
        handler.post(checkStoreStatusRunnable) // Start the Runnable immediately
    }*/

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
            if (adResponse != null) {
                Log.d("MidAisleMediumActivity", "LISS Updating UI with new ad response: $adResponse")
                Log.d("MidAisleMediumActivity", "LISS Auction results received: $adResponse")
                Log.d("MidAisleMediumActivity", "LISS Updating UI with new ad response:")
                Log.d("MidAisleMediumActivity", "LISS adType: ${adResponse.adType}")
                Log.d("MidAisleMediumActivity", "LISS creativeUrl: ${adResponse.creativeUrl}")
                updateUIWithAdResponse(adResponse)  // Your method to update UI
            } else {
                Log.e("MidAisleMediumActivity", "LISS Failed to receive auction results")
            }

            // Decide what to do next based on minutesToNextAuction

                if (adResponse != null) {
                    adResponse.storeOpenTime.let { openTime ->
                        adResponse.storeCloseTime.let { closeTime ->
                            adResponse.nextOpeningTime.let { nextOpeningTime ->
                                if (isStoreOpen(openTime, closeTime)) {
                                    scheduleNextAuction(adResponse.minutesToNextAuction, openTime, closeTime, nextOpeningTime)
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
        Log.d("MidAisleMediumActivity", "LISS fallback logo: ${adResponse.fallbackLogo}")

        if (adResponse.fallbackLogo == true) {
            // Display the fallback logo
            Log.d("MidAisleMediumActivity", "LISS fallback logo url: ${adResponse.creativeUrl}")
            displayImage(adResponse.creativeUrl, true)
        } else {
            // Display offerCode directly
            binding.offerCodeTextView.text = adResponse.couponCode
            binding.offerCodeTextView.visibility = View.VISIBLE

            // Display based on ad type
            when (adResponse.adType) {
                "VERTICAL_VIDEO" -> displayVideo(adResponse.creativeUrl)
                "IMAGE", "GIF" -> displayImage(adResponse.creativeUrl, false)
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
        }

        adResponse.storeOpenTime.let { openTime ->
            adResponse.storeCloseTime.let { closeTime ->
                startPeriodicTimeCheck(adResponse.adType ?: "IMAGE", openTime, closeTime)
            }
        }
    }

    private fun displayImage(url: String, isFallbackLogo: Boolean) {
        Glide.with(this)
            .load(url)
            .listener(object : RequestListener<Drawable> {
                override fun onResourceReady(resource: Drawable?, model: Any?, target: Target<Drawable>?, dataSource: DataSource?, isFirstResource: Boolean): Boolean {
                    Log.d("Glide", "LISS Image loaded successfully")
                    return false
                }

                override fun onLoadFailed(e: GlideException?, model: Any?, target: Target<Drawable>?, isFirstResource: Boolean): Boolean {
                    Log.e("Glide", "LISS Failed to load image", e)
                    return false
                }
            })
            .into(binding.creativeImageView)

        //binding.creativeImageView.visibility = View.VISIBLE
        //binding.videoView.visibility = View.GONE

        // Hide other views if it's a fallback logo
       if (isFallbackLogo) {
            binding.offerCodeTextView.visibility = View.GONE
            binding.couponHeaderTextView.visibility = View.GONE
            binding.couponOfferTextView.visibility = View.GONE
            binding.couponFooterTextView.visibility = View.GONE
            binding.ctaTextView.visibility = View.GONE
        } else {
            binding.offerCodeTextView.visibility = View.VISIBLE
            binding.couponHeaderTextView.visibility = View.VISIBLE
            binding.couponOfferTextView.visibility = View.VISIBLE
            binding.couponFooterTextView.visibility = View.VISIBLE
            binding.ctaTextView.visibility = View.VISIBLE
        }
    }

    @SuppressLint("UnsafeOptInUsageError")
    private fun displayVideo(url: String) {
        Log.d("MidAisleMediumActivity", "Displaying video: $url")

        val cacheKey = "video_cache_key_$url"
        val mediaItem = MediaItem.Builder()
            .setUri(url)
            .setCustomCacheKey(cacheKey)
            .build()

        val defaultDataSourceFactory = DefaultDataSourceFactory(this, "exoplayer-codelab")
        val cacheDataSourceFactory = CacheDataSource.Factory()
            .setCache(simpleCache)
            .setUpstreamDataSourceFactory(defaultDataSourceFactory)
            .setFlags(CacheDataSource.FLAG_IGNORE_CACHE_ON_ERROR)

        val mediaSource = ProgressiveMediaSource.Factory(cacheDataSourceFactory)
            .createMediaSource(mediaItem)

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

    @RequiresApi(Build.VERSION_CODES.O)
    private fun scheduleNextAuction(minutesToNextAuction: Int, openTime: String, closeTime: String, nextOpeningTime: String) {
        Log.d("MidAisleMediumActivity", "LISS Scheduling next auction. Current time: ${LocalDateTime.now()}")

        // Get the current date and time
        val currentTime = LocalDateTime.now()

        // Calculate the next auction time by adding the minutes to the current time
        val nextAuctionTime = currentTime.plusMinutes(minutesToNextAuction.toLong())
        Log.d("MidAisleMediumActivity", "LISS Calculated next auction time: $nextAuctionTime")

        // Parse the store's closing time from string to LocalTime
        Log.d("MidAisleMediumActivity", "LISS storeCloseTime: $closeTime")
        val storeCloseTimeLocal = closeTime.let { LocalTime.parse(it) }
        Log.d("MidAisleMediumActivity", "LISS storeCloseTimeLocal: $storeCloseTimeLocal")
        val closingDateTime = LocalDateTime.of(currentTime.toLocalDate(), storeCloseTimeLocal)
        Log.d("MidAisleMediumActivity", "LISS Store closing time (DateTime): $closingDateTime")

        // Parse the next day's opening time from string to LocalTime
        val nextOpeningTimeLocal = LocalTime.parse(nextOpeningTime)
        Log.d("MidAisleMediumActivity", "LISS Next opening time: $nextOpeningTimeLocal")

        // Create a LocalDateTime object using the next opening time and tomorrow's date
        val nextOpeningDateTime = LocalDateTime.of(LocalDate.now().plusDays(1), nextOpeningTimeLocal)

        // Calculate the delay in minutes
        val delayMinutes = if (nextAuctionTime.isAfter(closingDateTime)) {
            Log.d("MidAisleMediumActivity", "LISS Next auction time is after store closing time.")
            val minutesUntilNextOpen = Duration.between(closingDateTime, nextOpeningDateTime).toMinutes()
            // Add the minutes to next auction to the difference between closing time and next opening time
            minutesToNextAuction + minutesUntilNextOpen.toInt()
        } else {
            Log.d("MidAisleMediumActivity", "LISS Next auction time is before store closing time.")
            minutesToNextAuction
        }
        Log.d("MidAisleMediumActivity", "LISS Total delay until next auction: $delayMinutes minutes")

        // Schedule the startNextAuction() function to be called after the calculated delay
        handler.postDelayed({ startNextAuction() }, delayMinutes * 60_000L)
    }

    private fun resumeAdvertisement(adType: String) {
        Log.d("MidAisleMediumActivity", "LISS Resuming ad/ video player based on ad type: $adType")
        when (adType) {
            "VERTICAL_VIDEO" -> {
                exoPlayer.play()
                binding.videoView.visibility = View.VISIBLE
                binding.creativeImageView.visibility = View.GONE
            }
            "IMAGE", "GIF" -> {
                binding.creativeImageView.visibility = View.VISIBLE
                binding.videoView.visibility = View.GONE
            }
            else -> {
                binding.creativeImageView.visibility = View.GONE
                binding.videoView.visibility = View.GONE
            }
        }
        binding.root.setBackgroundColor(Color.WHITE)
    }

    private fun pauseAdvertisement() {
        Log.d("MidAisleMediumActivity", "LISS Pausing ad/ video player")
        // Pause the video
        exoPlayer.pause()

        // Hide the video player and any relevant UI components
        binding.videoView.visibility = View.GONE  // Assuming this is your video player view
        //binding.ctaTextView.visibility = View.GONE       // Assuming ctaTextView is the CTA text view
        binding.creativeImageView.visibility = View.GONE // Just in case this is also on the screen

        // Make screen black
        makeScreenBlack()
    }
    private fun storePlacementId(placementId: String) {
        val sharedPref = getSharedPreferences("MyPrefs", MODE_PRIVATE)
        with(sharedPref.edit()) {
            putString("placementId", placementId)
            apply()
        }
    }

    private fun makeScreenBlack() {
        Log.d("MidAisleMediumActivity", "LISS Making screen black")
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
    @SuppressLint("UnsafeOptInUsageError")
    override fun onDestroy() {
        super.onDestroy()
        exoPlayer.release()
        handler.removeCallbacksAndMessages(null)
        simpleCache.release()
        //timer.cancel()
    }

}