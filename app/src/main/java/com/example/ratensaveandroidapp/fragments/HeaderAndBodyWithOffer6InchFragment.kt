package com.example.ratensaveandroidapp.fragments

import android.graphics.Bitmap
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.ratensaveandroidapp.databinding.FragmentHeaderAndBodySixInchOfferBinding
import com.example.ratensaveandroidapp.datamodel.AdResponse
import com.example.ratensaveandroidapp.datamodel.Content

class HeaderAndBodyWithOffer6InchFragment : BaseAdFragment() {

    private var _binding: FragmentHeaderAndBodySixInchOfferBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHeaderAndBodySixInchOfferBinding.inflate(inflater, container, false)
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
        content?.let {
            binding.headerTextView.text = it.header
            binding.headerTextView.visibility = if (it.header.isBlank()) View.GONE else View.VISIBLE

            binding.bodyTextView.text = it.body
            binding.bodyTextView.visibility = if (it.body.isBlank()) View.GONE else View.VISIBLE
        } ?: run {
            binding.headerTextView.visibility = View.GONE
            binding.bodyTextView.visibility = View.GONE
        }
    }

    override fun getCreativeImageView(): View = binding.creativeImageView
    override fun getVideoView(): View = binding.videoView

    override fun showQRCode(qrCodeBitmap: Bitmap) {
        binding.qrCodeImageView.setImageBitmap(qrCodeBitmap)
        binding.qrCodeImageView.visibility = View.VISIBLE
    }

    override fun hideQRCode() {
        binding.qrCodeImageView.visibility = View.GONE
    }

    override fun hideCTAText() {
        // Implement this method to hide the CTA text
        // If there's a CTA text view in this fragment, hide it here
        // For example:
         binding.ctaTextView.visibility = View.GONE
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