package com.example.ratensaveandroidapp.utils

import android.graphics.Bitmap
import android.graphics.Color
import com.google.zxing.BarcodeFormat
import com.google.zxing.MultiFormatWriter
import com.google.zxing.common.BitMatrix

object QRCodeGenerator {

    fun generateQRCode(text: String, width: Int = 512, height: Int = 512): Bitmap {
        val bitMatrix: BitMatrix = try {
            MultiFormatWriter().encode(text, BarcodeFormat.QR_CODE, width, height)
        } catch (e: Exception) {
            // For simplicity, logging the exception and returning a blank bitmap.
            // Consider handling this scenario more gracefully in a real app.
            e.printStackTrace()
            return Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565)
        }
        val pixels = IntArray(width * height)
        for (y in 0 until height) {
            for (x in 0 until width) {
                pixels[y * width + x] = if (bitMatrix[x, y]) Color.BLACK else Color.WHITE
            }
        }

        return Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565).also {
            it.setPixels(pixels, 0, width, 0, 0, width, height)
        }
    }
}
