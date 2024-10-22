package com.example.ratensaveandroidapp.fragments

import android.animation.Animator
import android.animation.ObjectAnimator
import android.graphics.Bitmap
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.ImageView
import androidx.fragment.app.Fragment
import com.example.ratensaveandroidapp.R
import com.google.zxing.BarcodeFormat
import com.google.zxing.WriterException
import com.google.zxing.qrcode.QRCodeWriter
import kotlin.math.abs
import kotlin.random.Random
import android.widget.TextView


class SpinWheelFragment : Fragment() {

    private lateinit var wheel: ImageView
    private lateinit var apple: ImageView
    private lateinit var arrow: ImageView
    private lateinit var qrCodeImage: ImageView
    private lateinit var qrCodeText: TextView


    // Define the number of degrees per section (6 sections, each 60 degrees)
    private val degreesPerSection = 60
    private val offsetRange = 30 // Allow up to 30 degrees of randomness around each section

    // Define segment values in the clockwise order starting from 12 o'clock position
    private val segmentValues = arrayOf("NO_QR", "20OFF", "10OFF", "NO_QR", "10OFF", "10OFF")

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_wheel_spin_mini_game, container, false)

        wheel = view.findViewById(R.id.wheel)
        apple = view.findViewById(R.id.apple)
        arrow = view.findViewById(R.id.arrow)
        qrCodeImage = view.findViewById(R.id.qrCodeImage)
        qrCodeText = view.findViewById(R.id.qrCodeText) // Initialize the TextView

        // Set up the click listener for the apple
        apple.setOnClickListener {
            spinWheel()
        }

        return view
    }

    private fun spinWheel() {
        // Hide the QR code at the start of every spin
        qrCodeImage.visibility = View.GONE
        qrCodeText.visibility = View.GONE


        // Generate a random section angle
        val randomSection = Random.nextInt(0, 360)
        // Adjust the randomOffset to be only positive
        val randomOffset = Random.nextInt(0, offsetRange + 1) // No negative offsets
        val finalAngle = randomSection + randomOffset

        // Add logging for randomSection and randomOffset
        Log.d("SpinWheel", "Random Section: $randomSection")
        Log.d("SpinWheel", "Random Offset: $randomOffset")
        Log.d("SpinWheel", "Final Angle (before spins): $finalAngle")

        // Add more spins to make it visually engaging
        val totalRotation = finalAngle + (360 * 5) // 5 full spins + final random section

        // Add logging for total rotation
        Log.d("SpinWheel", "Total Rotation: $totalRotation")

        // Spin the wheel with animation
        val animator = ObjectAnimator.ofFloat(wheel, "rotation", 0f, totalRotation.toFloat())
        animator.duration = 3000 // 3 seconds
        animator.interpolator = AccelerateDecelerateInterpolator()

        animator.addListener(object : Animator.AnimatorListener {
            override fun onAnimationStart(animation: Animator) {
                // Optional: Add some logic when the spin starts
            }

            override fun onAnimationEnd(animation: Animator) {
                // Once the animation ends, determine the winning section based on the finalAngle
                val winningSection = determineWinningSection(finalAngle)

                // Log the winning section
                Log.d("SpinWheel", "Winning Section: $winningSection")

                displayResult(winningSection)
            }

            override fun onAnimationCancel(animation: Animator) {}
            override fun onAnimationRepeat(animation: Animator) {}
        })

        animator.start()
    }

    private fun determineWinningSection(finalAngle: Int): String {
        // Directly calculate the segment index, as finalAngle is now always positive
        val segmentIndex = finalAngle / degreesPerSection

        // Ensure segmentIndex is within array bounds
        val clampedIndex = segmentIndex.coerceIn(0, segmentValues.size - 1)

        // Add logging for angle and segment index
        Log.d("SpinWheel", "Final Angle: $finalAngle")
        Log.d("SpinWheel", "Segment Index: $segmentIndex")
        Log.d("SpinWheel", "Clamped Segment Index: $clampedIndex")

        // Get the value associated with the segment
        val result = segmentValues[clampedIndex]

        // Log the selected segment
        Log.d("SpinWheel", "Selected Segment: $result")

        return result
    }

    private fun displayResult(result: String) {
        when (result) {
            "10OFF" -> {
                Log.d("SpinWheel", "Displaying QR for: $result")
                generateQRCode(result)
            }
            "20OFF" -> {
                Log.d("SpinWheel", "Displaying QR for: $result")
                generateQRCode(result)
            }
            "NO_QR" -> {
                Log.d("SpinWheel", "No QR for this segment")
                // No QR code for this segment, ensure QR code is hidden
                qrCodeImage.visibility = View.GONE
                qrCodeText.visibility = View.GONE // Added line to hide the text
            }
        }
    }


    private fun generateQRCode(value: String) {
        val writer = QRCodeWriter()
        try {
            val bitMatrix = writer.encode(value, BarcodeFormat.QR_CODE, 200, 200)
            val width = bitMatrix.width
            val height = bitMatrix.height
            val bmp = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565)
            for (x in 0 until width) {
                for (y in 0 until height) {
                    bmp.setPixel(x, y, if (bitMatrix[x, y]) Color.BLACK else Color.WHITE)
                }
            }
            qrCodeImage.setImageBitmap(bmp)
            qrCodeImage.visibility = View.VISIBLE
            qrCodeText.visibility = View.VISIBLE

        } catch (e: WriterException) {
            e.printStackTrace()
        }
    }
}
