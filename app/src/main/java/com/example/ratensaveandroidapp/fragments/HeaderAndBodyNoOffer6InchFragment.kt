package com.example.ratensaveandroidapp.fragments

import android.graphics.Bitmap
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.ratensaveandroidapp.databinding.FragmentHeaderAndBodyNoOfferSixInchBinding
import com.example.ratensaveandroidapp.datamodel.AdResponse
import com.example.ratensaveandroidapp.datamodel.Content

class HeaderAndBodyNoOffer6InchFragment : BaseAdFragment() {

    private var _binding: FragmentHeaderAndBodyNoOfferSixInchBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHeaderAndBodyNoOfferSixInchBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.backButton.setOnClickListener {
            navigateToHome()
        }

        val adResponse = arguments?.getSerializable("adResponse") as? AdResponse
        adResponse?.let {
            updateUI(it)
        }
    }

    override fun updateAdContent(content: Content?) {
        content?.let { adContent ->
            // Update the header
            if (adContent.header.isNullOrEmpty()) {
                binding.headerTextView.visibility = View.GONE
            } else {
                binding.headerTextView.visibility = View.VISIBLE
                binding.headerTextView.text = adContent.header
            }

            // Update the body
            if (adContent.body.isNullOrEmpty()) {
                binding.bodyTextView.visibility = View.GONE
            } else {
                binding.bodyTextView.visibility = View.VISIBLE
                binding.bodyTextView.text = adContent.body
            }
        }
    }

    override fun getCreativeImageView(): View = binding.creativeImageView
    override fun getVideoView(): View = binding.videoView

    override fun showQRCode(qrCodeBitmap: Bitmap) {
        // No implementation needed for this fragment
    }

    override fun hideQRCode() {
        // No implementation needed for this fragment
    }

    override fun hideCTAText() {
        // Implement this method to hide the CTA text
        // If there's a CTA text view in this fragment, hide it here
        // For example:
        // binding.ctaTextView.visibility = View.GONE
        // If there's no CTA text view, you can leave it empty or log a message
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