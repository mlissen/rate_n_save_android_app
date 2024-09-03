package com.example.ratensaveandroidapp.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.example.ratensaveandroidapp.R
import com.example.ratensaveandroidapp.databinding.FragmentHeaderAndBodyNoOfferSixInchBinding
import androidx.media3.datasource.cache.LeastRecentlyUsedCacheEvictor
import com.example.ratensaveandroidapp.datamodel.AdResponse
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.source.ProgressiveMediaSource
import androidx.media3.datasource.DefaultDataSourceFactory
import androidx.media3.datasource.cache.CacheDataSource
import com.example.ratensaveandroidapp.HomeActivity
import com.example.ratensaveandroidapp.utils.CacheManager
import java.io.File

class HeaderAndBodyNoOffer6InchFragment : Fragment() {

    private var _binding: FragmentHeaderAndBodyNoOfferSixInchBinding? = null
    private val binding get() = _binding!!

    private lateinit var exoPlayer: ExoPlayer
    private val simpleCache by lazy { CacheManager.getSimpleCache(requireContext()) } // Use the singleton cache instance


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentHeaderAndBodyNoOfferSixInchBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Make the button visible for testing
        binding.backButton.visibility = View.VISIBLE

        // Set up the back button click listener
        binding.backButton.setOnClickListener {
            navigateToHome()
        }

        // Initialize video caching
        val cacheDirectory = File(requireContext().cacheDir, "video-cache")
        val cacheSize = 100 * 1024 * 1024 // 100 MB
        val lruCacheEvictor = LeastRecentlyUsedCacheEvictor(cacheSize.toLong())


        // Placeholder for receiving and processing AdResponse
        val adResponse = arguments?.getSerializable("adResponse") as? AdResponse
        adResponse?.let {
            updateUI(it)
        }
    }

    private fun navigateToHome() {
        val intent = Intent(requireContext(), HomeActivity::class.java)
        startActivity(intent)
    }

    private fun updateUI(adResponse: AdResponse) {
        binding.headerTextView.text = adResponse.content?.header
        binding.bodyTextView.text = adResponse.content?.body

        when (adResponse.adType) {
            "VERTICAL_VIDEO" -> displayVideo(adResponse.creativeUrl)
            "IMAGE", "GIF" -> displayImage(adResponse.creativeUrl)
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
        val cacheKey = "video_cache_key_$url"
        val mediaItem = MediaItem.Builder()
            .setUri(url)
            .setCustomCacheKey(cacheKey)
            .build()

        val defaultDataSourceFactory = DefaultDataSourceFactory(requireContext(), "exoplayer-codelab")
        val cacheDataSourceFactory = CacheDataSource.Factory()
            .setCache(simpleCache)
            .setUpstreamDataSourceFactory(defaultDataSourceFactory)
            .setFlags(CacheDataSource.FLAG_IGNORE_CACHE_ON_ERROR)

        val mediaSource = ProgressiveMediaSource.Factory(cacheDataSourceFactory)
            .createMediaSource(mediaItem)

        exoPlayer = ExoPlayer.Builder(requireContext()).build().apply {
            setMediaSource(mediaSource)
            prepare()
            play()
            repeatMode = ExoPlayer.REPEAT_MODE_ONE
        }

        binding.videoView.player = exoPlayer
        binding.creativeImageView.visibility = View.GONE
        binding.videoView.visibility = View.VISIBLE
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        exoPlayer.release()
        CacheManager.releaseCache() // Properly release the cache instance
    }
}
