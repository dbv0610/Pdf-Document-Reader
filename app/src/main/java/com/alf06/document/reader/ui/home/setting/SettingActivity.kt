package com.alf06.document.reader.ui.home.setting

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.core.view.isVisible
import com.alf06.document.reader.R
import com.alf06.document.reader.base.Aso
import com.alf06.document.reader.base.openUrl
import com.alf06.document.reader.base.shareApp
import com.alf06.document.reader.databinding.ActivitySettingBinding
import com.alf06.document.reader.databinding.ItemSettingOptionBinding
import com.alf06.document.reader.ui.dialog.RatingDialog
import com.alf06.document.reader.utils.PreferenceHelper
import com.ui.baselib.base.BaseActivity
import com.ui.baselib.extensions.click
import com.ui.baselib.widget.layout.UiLinearLayout
import org.koin.android.ext.android.inject

class SettingActivity : BaseActivity<ActivitySettingBinding>(ActivitySettingBinding::inflate) {
    override val fullStatus: Boolean
        get() = false

    private val preferenceData: PreferenceHelper by inject()

    override fun backPressed() {
        finish()
    }

    override fun onResume() {
        super.onResume()
        preferenceData.isRatedApp.flow.mainCollect {
            binding.itemRateApp.root.isVisible = !it
        }
    }

    override fun initialize() {
    }

    override fun ActivitySettingBinding.setData() {
    }

    override fun ActivitySettingBinding.onClick() {
        icBack.click { finish() }
        itemLanguage.setUpItemView(R.string.language, R.drawable.ic_setting_language).click {
        }
        itemRateApp.setUpItemView(R.string.rate_app, R.drawable.ic_setting_rate).click {
            RatingDialog(this@SettingActivity, onFinishRate = {
                preferenceData.isRatedApp.value = true
            }).show()
        }
        itemShareApp.setUpItemView(R.string.share_app, R.drawable.ic_setting_share).click {
            shareApp()
        }
        itemPrivacyPolicy.setUpItemView(R.string.privacy_policy, R.drawable.ic_setting_privacy)
            .click {
                openUrl(Aso.POLICY_LINK)
            }
        itemTermOfService.setUpItemView(R.string.term_of_service, R.drawable.ic_setting_terms)
            .click {
                openUrl(Aso.TEAM_SERVICE)
            }
    }

    private fun ItemSettingOptionBinding.setUpItemView(
        @StringRes title: Int,
        @DrawableRes icon: Int
    ): UiLinearLayout {
        tvTitle.setText(title)
        imgIcon.setImageResource(icon)
        return root
    }
}
