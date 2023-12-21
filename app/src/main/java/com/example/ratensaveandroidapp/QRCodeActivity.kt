package com.example.ratensaveandroidapp

// Import necessary libraries
import android.graphics.Bitmap
import android.os.Bundle
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import android.graphics.Color
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import com.google.zxing.BarcodeFormat
import com.google.zxing.MultiFormatWriter
import com.google.zxing.common.BitMatrix
import com.example.ratensaveandroidapp.viewmodel.CouponViewModel
import java.util.*
import android.widget.Button
import android.app.Activity
import com.example.ratensaveandroidapp.R

// Define QRCodeActivity class which extends AppCompatActivity
class QRCodeActivity : AppCompatActivity() {
    private lateinit var qrCodeImageView: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_qr_code)

        // Correct method is findViewById, not findViewByID
        qrCodeImageView = findViewById(R.id.qrCodeImageView)
        val doneButton: Button = findViewById(R.id.doneButton) // Correct this line
        doneButton.setOnClickListener {
            finish() // Finish this activity and return to the previous one
        }

        val couponID = intent.getStringExtra("COUPON_ID")
        couponID?.let {
            val bitmap = generateQRCode(it)
            qrCodeImageView.setImageBitmap(bitmap) // Set the generated QR code as the image for ImageView
        }
    }


    private fun generateQRCode(text: String): Bitmap {
        val width = 512
        val height = 512
        val bitMatrix: BitMatrix = try {
            MultiFormatWriter().encode(text, BarcodeFormat.QR_CODE, width, height, null)
        } catch (e: IllegalArgumentException) {
            return Bitmap.createBitmap(1, 1, Bitmap.Config.RGB_565)
        }
        val pixels = IntArray(width * height)
        for (y in 0 until height) {
            for (x in 0 until width) {
                pixels[y * width + x] = if (bitMatrix[x, y]) Color.BLACK else Color.WHITE
            }
        }

        return Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565).apply {
            setPixels(pixels, 0, width, 0, 0, width, height)
        }
    }

    private fun showError(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}
