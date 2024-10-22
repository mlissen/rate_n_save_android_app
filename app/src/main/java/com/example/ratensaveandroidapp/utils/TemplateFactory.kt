package com.example.ratensaveandroidapp.utils

import androidx.fragment.app.Fragment
import com.example.ratensaveandroidapp.fragments.*

object TemplateFactory {

    fun getFragmentForTemplate(placementTypeId: Int, templateId: Int): Fragment {
        return when (placementTypeId) {
            1 -> when (templateId) {
                1 -> HeaderAndBodyNoOffer6InchFragment()
                2 -> FullScreenAdFragmentFor6InchNoOffer()
                3 -> HeaderAndBodyWithOffer6InchFragment()
                4 -> FullScreenAdFragmentFor6InchWithOffer()
                5 -> SpinWheelFragment()
                else -> DefaultAdFragment()
            }
            else -> DefaultAdFragment()
        }
    }
}
