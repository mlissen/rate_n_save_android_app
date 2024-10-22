package com.example.ratensaveandroidapp.fragments

import android.graphics.Bitmap
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.ratensaveandroidapp.databinding.FragmentFullScreenAdSixInchBinding
import com.example.ratensaveandroidapp.datamodel.AdResponse
import com.example.ratensaveandroidapp.datamodel.Content

class FullScreenAdFragmentFor6InchNoOffer : BaseAdFragment() {

    private var _binding: FragmentFullScreenAdSixInchBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFullScreenAdSixInchBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.backButton.visibility = View.VISIBLE
        binding.backButton.setOnClickListener {
            navigateToHome()
        }

        val adResponse = arguments?.getSerializable("adResponse") as? AdResponse
        adResponse?.let {
            updateUI(it)
        }
    }

    override fun updateAdContent(content: Content?) {
        // This fragment doesn't display header or body text
        // If you want to add any custom behavior based on the content, you can do it here
    }

    override fun getCreativeImageView(): View = binding.creativeImageView

    override fun getVideoView(): View = binding.videoView

    override fun showQRCode(qrCodeBitmap: Bitmap) {
        // No QR code in this fragment
    }

    override fun hideQRCode() {
        // No QR code in this fragment
    }

    override fun hideCTAText() {
        // Implement this method to hide the CTA text if it exists in this fragment
        // If there's no CTA text in this fragment, you can leave it empty or log a message
    }

    override fun hideCreativeImageView() {
        binding.creativeImageView.visibility = View.GONE
    }

    override fun hideVideoView() {
        binding.videoView.visibility = View.GONE
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}