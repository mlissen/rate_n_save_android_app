package com.example.ratensaveandroidapp.fragments

import android.content.Intent
import android.graphics.Bitmap
import android.view.View
import android.widget.ImageView
import androidx.annotation.OptIn
import androidx.fragment.app.Fragment
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.common.MediaItem
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.source.ProgressiveMediaSource
import androidx.media3.datasource.DefaultDataSourceFactory
import androidx.media3.datasource.cache.CacheDataSource
import androidx.media3.ui.PlayerView
import com.bumptech.glide.Glide
import com.example.ratensaveandroidapp.HomeActivity
import com.example.ratensaveandroidapp.datamodel.AdResponse
import com.example.ratensaveandroidapp.datamodel.Content
import com.example.ratensaveandroidapp.utils.CacheManager
import com.example.ratensaveandroidapp.utils.QRCodeGenerator

abstract class BaseAdFragment : Fragment() {

    protected var exoPlayer: ExoPlayer? = null
    protected var adType: String? = null
    protected var isActive: Boolean = false
    protected val simpleCache by lazy { CacheManager.getSimpleCache(requireContext()) }

    abstract fun getCreativeImageView(): View
    abstract fun getVideoView(): View

    protected fun navigateToHome() {
        val intent = Intent(requireContext(), HomeActivity::class.java)
        startActivity(intent)
    }

    /**
     * Main method to update the UI with ad content.
     * This method handles common elements and delegates specific updates to abstract methods.
     */
    protected fun updateUI(adResponse: AdResponse) {
        adType = adResponse.adType
        when (adType) {
            "VERTICAL_VIDEO" -> {
                isActive = true
                displayVideo(adResponse.creativeUrl)
            }

            "IMAGE", "GIF" -> {
                isActive = true
                displayImage(adResponse.creativeUrl)
            }
        }

        updateAdContent(adResponse.content)
        updateOfferCode(adResponse.offerCode)
    }

    private fun displayImage(url: String) {
        Glide.with(this)
            .load(url)
            .into(getCreativeImageView() as ImageView)
        getCreativeImageView().visibility = View.VISIBLE
        getVideoView().visibility = View.GONE
    }

    @OptIn(UnstableApi::class)
    private fun displayVideo(url: String) {
        getCreativeImageView().visibility = View.GONE
        getVideoView().visibility = View.VISIBLE

        val mediaItem = MediaItem.Builder()
            .setUri(url)
            .setCustomCacheKey("video_cache_key_$url")
            .build()

        val dataSourceFactory = DefaultDataSourceFactory(requireContext(), "exoplayer-codelab")
        val cacheDataSourceFactory = CacheDataSource.Factory()
            .setCache(simpleCache)
            .setUpstreamDataSourceFactory(dataSourceFactory)
            .setFlags(CacheDataSource.FLAG_IGNORE_CACHE_ON_ERROR)

        val mediaSource = ProgressiveMediaSource.Factory(cacheDataSourceFactory)
            .createMediaSource(mediaItem)

        exoPlayer = ExoPlayer.Builder(requireContext()).build().apply {
            setMediaSource(mediaSource)
            prepare()
            play()
            repeatMode = ExoPlayer.REPEAT_MODE_ONE
        }

        (getVideoView() as PlayerView).player = exoPlayer
    }

    private fun updateOfferCode(offerCode: String?) {
        if (!offerCode.isNullOrEmpty()) {
            val qrCodeBitmap = QRCodeGenerator.generateQRCode(offerCode)
            showQRCode(qrCodeBitmap)
        } else {
            hideQRCode()
        }
    }

    /**
     * Update ad-specific content like header and body.
     * Implement this method in child classes to handle specific ad content.
     */
    protected abstract fun updateAdContent(content: Content?)

    abstract fun showQRCode(qrCodeBitmap: Bitmap)
    abstract fun hideQRCode()

    override fun onDestroyView() {
        super.onDestroyView()
        releaseExoPlayer()
        CacheManager.releaseCache()
        isActive = false
    }

    private fun releaseExoPlayer() {
        exoPlayer?.let { player ->
            player.stop()
            player.release()
            exoPlayer = null
        }
    }

    override fun onStop() {
        super.onStop()
        if (adType == "VERTICAL_VIDEO") {
            exoPlayer?.pause()
        }
    }

    override fun onResume() {
        super.onResume()
        if (adType == "VERTICAL_VIDEO") {
            exoPlayer?.play()
        }
    }

    open fun pauseAd() {
        when (adType) {
            "VERTICAL_VIDEO" -> {
                exoPlayer?.pause()
            }

            "IMAGE", "GIF" -> {
                getCreativeImageView().visibility = View.GONE
            }
        }
        isActive = false
    }

    open fun resumeAd() {
        when (adType) {
            "VERTICAL_VIDEO" -> {
                exoPlayer?.play()
            }

            "IMAGE", "GIF" -> {
                getCreativeImageView().visibility = View.VISIBLE
            }
        }
        isActive = true
    }
    abstract fun hideCTAText()
    abstract fun hideCreativeImageView()
    abstract fun hideVideoView()


    fun isAdActive(): Boolean = isActive

}

