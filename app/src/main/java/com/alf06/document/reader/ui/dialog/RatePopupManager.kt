package com.alf06.document.reader.ui.dialog

import androidx.appcompat.app.AppCompatActivity
import com.alf06.document.reader.utils.PreferenceHelper
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

object RatePopupManager: KoinComponent {
    private val preferenceData: PreferenceHelper by inject()
    var isShownInSession = false
        private set

    fun showRateIfNeeded(activity: AppCompatActivity, sessionList: List<Int>?, screenCount: Int) {
        if (preferenceData.isRatedApp.value) return
        if (isShownInSession) return
        if (sessionList.isNullOrEmpty()) return
        if (!sessionList.contains(screenCount)) return
        isShownInSession = true
        RatingDialog(activity, onFinishRate = {
            preferenceData.isRatedApp.value = true
        }).show()
    }

    fun reset() {
        isShownInSession = false
    }
}
