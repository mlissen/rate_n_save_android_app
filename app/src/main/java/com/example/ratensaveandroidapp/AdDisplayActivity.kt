package com.example.ratensaveandroidapp

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.ratensaveandroidapp.datamodel.AdResponse
import com.example.ratensaveandroidapp.utils.TemplateFactory
import android.view.WindowInsets
import android.view.WindowInsetsController
import android.view.WindowManager
import android.os.Build
import android.view.View



class AdDisplayActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ad_display)

        // Keep the screen on indefinitely
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        hideSystemUI() // Add this line to hide system UI

        val placementTypeId = intent.getIntExtra("placementTypeId", -1)
        val templateId = intent.getIntExtra("templateId", -1)
        val adResponse = intent.getSerializableExtra("adResponse") as AdResponse

        val fragment = TemplateFactory.getFragmentForTemplate(placementTypeId, templateId).apply {
            arguments = Bundle().apply {
                putSerializable("adResponse", adResponse)
            }
        }

        supportFragmentManager.beginTransaction()
            .replace(R.id.adContainer, fragment)
            .commit()
    }

    private fun hideSystemUI() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
            window.insetsController?.apply {
                systemBarsBehavior = WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
                hide(android.view.WindowInsets.Type.systemBars())
            }
        } else {
            @Suppress("DEPRECATION")
            window.decorView.systemUiVisibility = (
                    View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                            or View.SYSTEM_UI_FLAG_FULLSCREEN
                            or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            or View.SYSTEM_UI_FLAG_LAYOUT_STABLE)
        }
    }
}
