package com.example.ratensaveandroidapp

import android.animation.Animator
import android.animation.ObjectAnimator
import android.view.animation.AccelerateDecelerateInterpolator
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
import androidx.media3.common.util.UnstableApi
import com.example.ratensaveandroidapp.databinding.AdDisplayLayoutBinding
import com.example.ratensaveandroidapp.datamodel.AdResponse
import com.example.ratensaveandroidapp.fragments.BaseAdFragment
import java.time.Duration
import java.time.LocalDateTime
import java.time.LocalDate
import java.time.LocalTime
import com.example.ratensaveandroidapp.utils.TemplateFactory

@OptIn(androidx.media3.common.util.UnstableApi::class)
class AdDisplayActivity : AppCompatActivity() {

    private lateinit var binding: AdDisplayLayoutBinding
    private lateinit var viewModel: AuctionViewModel
    private var placementId: String? = null
    //private lateinit var timer: Timer
    private lateinit var handler: Handler
    //private lateinit var checkStoreStatusRunnable: Runnable
    private lateinit var periodicTimeCheckRunnable: Runnable

    @OptIn(UnstableApi::class) @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        binding = AdDisplayLayoutBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Set the background color to black
        //binding.root.setBackgroundColor(Color.BLACK)

        // Make the activity full screen
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )
        // Set the apple background
        //binding.root.setBackgroundResource(R.drawable.applebackground)

        // Initialize the timer and handler
        handler = Handler(Looper.getMainLooper())


        // Keep screen on
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        viewModel = ViewModelProvider(this).get(AuctionViewModel::class.java)
        retrievePlacementId()

        Log.d("AdDisplayActivity", "LISS starting observer")
        observeAdResponse() // Observe the LiveData here

        val adResponse = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getSerializableExtra("adResponse", AdResponse::class.java)
        } else {
            @Suppress("DEPRECATION")
            intent.getSerializableExtra("adResponse") as? AdResponse
        }
        Log.d("AdDisplayActivity", "LISS Received adResponse: $adResponse")
        Log.d("AdDisplayActivity", "LISS ${adResponse?.storeCloseTime} ${adResponse?.storeOpenTime} ${adResponse?.nextOpeningTime}")
        if (adResponse != null) {
            Log.d("AdDisplayActivity", "LISS Loading ad fragment with adResponse")

            loadAdFragment(adResponse)

            // Check if storeOpenTime and storeCloseTime are null
            if (adResponse.storeOpenTime == null || adResponse.storeCloseTime == null) {
                throw IllegalArgumentException("storeOpenTime or storeCloseTime is null. Cannot display ad.")
            }
            Log.d("AdDisplayActivity", "LISS starting periodic time check")

            adResponse.storeOpenTime?.let { openTime ->
                Log.d("AdDisplayActivity", "openTime: $openTime retrieved from adResponse")

                adResponse.storeCloseTime?.let { closeTime ->
                    Log.d("AdDisplayActivity", "closeTime: $closeTime retrieved from adResponse")

                    adResponse.nextOpeningTime?.let { nextOpeningTime ->
                        Log.d("AdDisplayActivity", "nextOpeningTime: $nextOpeningTime retrieved from adResponse")

                        val isOpen = isStoreOpen(openTime, closeTime)
                        Log.d("AdDisplayActivity", "isStoreOpen check: store is ${if (isOpen) "open" else "closed"} (openTime: $openTime, closeTime: $closeTime)")

                        if (isOpen) {
                            Log.d("AdDisplayActivity", "Store is open, scheduling next auction with minutesToNextAuction: ${adResponse.minutesToNextAuction}")
                            scheduleNextAuction(adResponse.minutesToNextAuction, openTime, closeTime, nextOpeningTime)
                        } else {
                            Log.d("AdDisplayActivity", "Store is closed, scheduling next auction after reopening")
                            scheduleNextAuction(adResponse.minutesToNextAuction, openTime, closeTime, nextOpeningTime)
                            Log.d("AdDisplayActivity", "Store is closed, pausing advertisement")
                            pauseAdvertisement()
                        }
                    } ?: Log.e("AdDisplayActivity", "LISS nextOpeningTime is null, cannot proceed to schedule auction")
                } ?: Log.e("AdDisplayActivity", "LISS closeTime is null, cannot proceed to schedule auction")
            } ?: Log.e("AdDisplayActivity", "LISS openTime is null, cannot proceed to schedule auction")

        } else {
            Log.d("AdDisplayActivity", "LISS adResponse is null, starting next auction")
            startNextAuction()
        }

        val backButton = findViewById<Button>(R.id.backButton)
        backButton.setOnClickListener {
            Log.d("AdDisplayActivity", "Back button clicked!")
            navigateToHomeActivity()
        }

        hideSystemUI()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun loadAdFragment(adResponse: AdResponse) {
        // Extract placementTypeId and templateId from adResponse
        val placementTypeId = adResponse.placementTypeId
        val templateId = adResponse.templateId

        // Retrieve the appropriate fragment using TemplateFactory
        val fragment = TemplateFactory.getFragmentForTemplate(placementTypeId, templateId).apply {
            arguments = Bundle().apply {
                putSerializable("adResponse", adResponse) // Pass the adResponse to the fragment
            }
        }

        // Check if the fragment is not null
        if (fragment != null) {
            // Replace the current fragment with the new ad fragment
            supportFragmentManager.beginTransaction()
                .replace(R.id.adContainer, fragment)  // R.id.adContainer should be the container where the fragment is displayed
                .commitAllowingStateLoss()
        } else {
            // Handle the case where no valid fragment was found
            Log.e("AdDisplayActivity", "LISS No valid fragment found for templateId: $templateId")
        }
        adResponse.storeOpenTime.let { openTime ->
            adResponse.storeCloseTime.let { closeTime ->
                startPeriodicTimeCheck(adResponse.adType ?: "IMAGE", openTime, closeTime)
            }
        }
    }


    @RequiresApi(Build.VERSION_CODES.O)
    private fun startPeriodicTimeCheck(adType: String, openTime: String, closeTime: String) {
        Log.e("AdDisplayActivity", "Starting periodic time check every 60 seconds")

        periodicTimeCheckRunnable = Runnable {
            if (isStoreOpen(openTime, closeTime)) {
                Log.d("AdDisplayActivity", "Store is open, resuming ad")
                resumeAdvertisement(adType)
            } else {
                Log.d("AdDisplayActivity", "Store is closed, pausing ad")
                pauseAdvertisement()
            }

            // Schedule the next periodic time check after 60 seconds
            handler.postDelayed(periodicTimeCheckRunnable, 60000)
        }

        // Start the periodic check immediately
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
            Log.e("AdDisplayActivity", "No placement ID found")
            // Handle the case where no placement ID is found, maybe navigate back or show a message
        } else {
            Log.d("AdDisplayActivity", "Retrieved Placement ID: $placementId")
            // You can now use placementId for further operations, like starting an auction
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun observeAdResponse() {
        // This ensures you only add the observer once during the Activity lifecycle
        viewModel.adResponse.observe(this) { adResponse ->
            Log.d("AdDisplayActivity", "LISS Observer triggered with response: $adResponse")
            if (adResponse != null) {
                Log.d("AdDisplayActivity", "LISS Updating UI with new ad response: $adResponse")
                Log.d("AdDisplayActivity", "LISS Auction results received: $adResponse")
                Log.d("AdDisplayActivity", "LISS Updating UI with new ad response:")
                Log.d("AdDisplayActivity", "LISS adType: ${adResponse.adType}")
                Log.d("AdDisplayActivity", "LISS creativeUrl: ${adResponse.creativeUrl}")
                // Load the new fragment based on the ad response using loadAdFragment
                loadAdFragment(adResponse)  // Your method to update UI
            } else {
                Log.e("AdDisplayActivity", "LISS Failed to receive auction results")
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

    /* private fun updateUIWithAdResponse(adResponse: AdResponse) {
        Log.d("AdDisplayActivity", "LISS Updating UI with new ad response: $adResponse")
        Log.d("AdDisplayActivity", "LISS adType: ${adResponse.adType}")
        Log.d("AdDisplayActivity", "LISS creativeUrl: ${adResponse.creativeUrl}")
        Log.d("AdDisplayActivity", "LISS minutesToNextAuction: ${adResponse.minutesToNextAuction}")
        Log.d("AdDisplayActivity", "LISS storeOpenTime: ${adResponse.storeOpenTime}")
        Log.d("AdDisplayActivity", "LISS storeCloseTime: ${adResponse.storeCloseTime}")
        Log.d("AdDisplayActivity", "LISS nextOpeningTime: ${adResponse.nextOpeningTime}")
        Log.d("AdDisplayActivity", "LISS fallback logo: ${adResponse.fallbackLogo}")

        if (adResponse.fallbackLogo == true) {
            // Display the fallback logo
            Log.d("AdDisplayActivity", "LISS fallback logo url: ${adResponse.creativeUrl}")
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
    } */

    /*private fun displayImage(url: String, isFallbackLogo: Boolean) {
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
        Log.d("AdDisplayActivity", "Displaying video: $url")

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
    */


    private fun startNextAuction() {
        val sharedPref = getSharedPreferences("AppSettings", MODE_PRIVATE)
        val placementId = sharedPref.getString("placementId", null) // Use a default value or handle null appropriately
        val storeTimezone = intent.getStringExtra("storeTimezone") // Retrieve the store timezone from Intent

        if (placementId != null && placementId.isNotBlank() && storeTimezone != null) { // Ensure ID exists and timezone is not null
            Log.d("AdDisplayActivity", "Starting next auction with placement ID: $placementId and timezone: $storeTimezone")
            viewModel.startAuction(placementId, storeTimezone)
        } else {
            Log.e("AdDisplayActivity", "No placement ID or store timezone found, navigating back to HomeActivity")
            navigateToHomeActivity()
        }
    }

    /*@RequiresApi(Build.VERSION_CODES.O)
    private fun scheduleNextAuction(minutesToNextAuction: Int, openTime: String, closeTime: String, nextOpeningTime: String) {
        Log.d("AdDisplayActivity", "Scheduling next auction. Current time: ${LocalDateTime.now()}")

        // Get the current date and time
        val currentTime = LocalDateTime.now()

        // Parse the store's opening and closing times from string to LocalTime
        val openTimeLocal = LocalTime.parse(openTime)
        val closeTimeLocal = LocalTime.parse(closeTime)

        // Calculate the expected next auction time
        val nextAuctionTime = currentTime.plusMinutes(minutesToNextAuction.toLong())
        Log.d("AdDisplayActivity", "Expected next auction time: $nextAuctionTime")

        // Check if the store is currently open
        if (isStoreOpen(openTime, closeTime)) {
            if (nextAuctionTime.toLocalTime().isBefore(closeTimeLocal)) {
                // Store is open and the auction can happen before it closes
                val delayMillis = Duration.between(currentTime, nextAuctionTime).toMillis()
                Log.d("AdDisplayActivity", "Store is open. Scheduling auction in $minutesToNextAuction minutes.")
                handler.postDelayed({
                    Log.d("AdDisplayActivity", "Starting next auction now.")
                    startNextAuction()
                }, delayMillis)
            } else {
                // Store is open, but it will close before the next auction time
                val closingDateTime = LocalDateTime.of(currentTime.toLocalDate(), closeTimeLocal)
                val timeUntilClose = Duration.between(currentTime, closingDateTime).toMinutes()
                val remainingMinutesAfterClose = minutesToNextAuction - timeUntilClose.toInt()

                Log.d("AdDisplayActivity", "Store will close before the auction. Remaining time after close: $remainingMinutesAfterClose minutes.")

                // Schedule to resume after reopening
                scheduleAuctionAfterReopening(nextOpeningTime, remainingMinutesAfterClose)
            }
        } else {
            // Store is currently closed, schedule auction after reopening
            scheduleAuctionAfterReopening(nextOpeningTime, minutesToNextAuction)
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun scheduleAuctionAfterReopening(nextOpeningTime: String, remainingMinutes: Int) {
        val currentTime = LocalDateTime.now()
        val nextOpeningTimeLocal = LocalTime.parse(nextOpeningTime)
        val nextOpeningDateTime = if (currentTime.toLocalTime().isAfter(nextOpeningTimeLocal)) {
            // If the next opening time is tomorrow
            LocalDateTime.of(currentTime.toLocalDate().plusDays(1), nextOpeningTimeLocal)
        } else {
            // If the next opening time is later today
            LocalDateTime.of(currentTime.toLocalDate(), nextOpeningTimeLocal)
        }

        // Calculate the delay in milliseconds until the next opening time
        val delayUntilOpeningMillis = Duration.between(currentTime, nextOpeningDateTime).toMillis()
        Log.d("AdDisplayActivity", "Store will reopen at $nextOpeningDateTime. Scheduling auction to start $remainingMinutes minutes after reopening.")

        // First, schedule the handler to wait until the store reopens
        handler.postDelayed({
            Log.d("AdDisplayActivity", "Store has reopened. Scheduling auction to start in $remainingMinutes minutes.")
            val delayAfterOpeningMillis = remainingMinutes * 60 * 1000L
            handler.postDelayed({
                Log.d("AdDisplayActivity", "Starting next auction now.")
                startNextAuction()
            }, delayAfterOpeningMillis)
        }, delayUntilOpeningMillis)
    }*/


     @RequiresApi(Build.VERSION_CODES.O)
    private fun scheduleNextAuction(minutesToNextAuction: Int, openTime: String, closeTime: String, nextOpeningTime: String) {
        Log.d("AdDisplayActivity", "LISS Scheduling next auction. Current time: ${LocalDateTime.now()}")

        // Get the current date and time
        val currentTime = LocalDateTime.now()

        // Calculate the next auction time by adding the minutes to the current time
        val nextAuctionTime = currentTime.plusMinutes(minutesToNextAuction.toLong())
        Log.d("AdDisplayActivity", "LISS Calculated next auction time: $nextAuctionTime")

        // Parse the store's closing time from string to LocalTime
        Log.d("AdDisplayActivity", "LISS storeCloseTime: $closeTime")
        val storeCloseTimeLocal = closeTime.let { LocalTime.parse(it) }
        Log.d("AdDisplayActivity", "LISS storeCloseTimeLocal: $storeCloseTimeLocal")
        val closingDateTime = LocalDateTime.of(currentTime.toLocalDate(), storeCloseTimeLocal)
        Log.d("AdDisplayActivity", "LISS Store closing time (DateTime): $closingDateTime")

        // Parse the next day's opening time from string to LocalTime
        val nextOpeningTimeLocal = LocalTime.parse(nextOpeningTime)
        Log.d("AdDisplayActivity", "LISS Next opening time: $nextOpeningTimeLocal")

        // Create a LocalDateTime object using the next opening time and tomorrow's date
        val nextOpeningDateTime = LocalDateTime.of(LocalDate.now().plusDays(1), nextOpeningTimeLocal)

        // Calculate the delay in minutes
        val delayMinutes = if (nextAuctionTime.isAfter(closingDateTime)) {
            Log.d("AdDisplayActivity", "LISS Next auction time is after store closing time.")
            val minutesUntilNextOpen = Duration.between(closingDateTime, nextOpeningDateTime).toMinutes()
            // Add the minutes to next auction to the difference between closing time and next opening time
            minutesToNextAuction + minutesUntilNextOpen.toInt()
        } else {
            Log.d("AdDisplayActivity", "LISS Next auction time is before store closing time.")
            minutesToNextAuction
        }
        Log.d("AdDisplayActivity", "LISS Total delay until next auction: $delayMinutes minutes")

        // Schedule the startNextAuction() function to be called after the calculated delay
        handler.postDelayed({ startNextAuction() }, delayMinutes * 60_000L)
    }


    private fun resumeAdvertisement(adType: String) {
        Log.d("AdDisplayActivity", "LISS Resuming ad/ video player based on ad type: $adType")

        val currentFragment = supportFragmentManager.findFragmentById(R.id.adContainer) as? BaseAdFragment
        currentFragment?.let {
            it.resumeAd()
            binding.root.setBackgroundColor(Color.WHITE)
        } ?: Log.d("AdDisplayActivity", "No active fragment to resume")
    }

    private fun pauseAdvertisement() {
        Log.d("AdDisplayActivity", "LISS Pausing ad/ video player")

        val currentFragment = supportFragmentManager.findFragmentById(R.id.adContainer) as? BaseAdFragment
        currentFragment?.let {
            it.pauseAd()
        } ?: Log.d("AdDisplayActivity", "No active fragment to pause")

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
        Log.d("AdDisplayActivity", "LISS Making screen black")
        // Set the background color of the root view to black
        binding.root.setBackgroundColor(Color.BLACK)

        // Get the current fragment and call its methods
        val currentFragment = supportFragmentManager.findFragmentById(R.id.adContainer) as? BaseAdFragment
        currentFragment?.let {
            it.hideQRCode()
            it.hideCTAText()
            it.hideCreativeImageView()
            it.hideVideoView()
        }
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
        Log.d("AdDisplayActivity", "LISS Local time: $currentTime")
        val openTimeLocal = LocalTime.parse(openTime)
        val closeTimeLocal = LocalTime.parse(closeTime)
        Log.d("AdDisplayActivity", "LISS Open time local: $openTimeLocal")
        Log.d("AdDisplayActivity", "LISS Closing time local: $closeTimeLocal")

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
        handler.removeCallbacksAndMessages(null)
        //timer.cancel()
    }

}