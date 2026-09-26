package com.alf06.document.reader.ui.splash

import android.annotation.SuppressLint
import android.content.Intent
import androidx.lifecycle.lifecycleScope
import com.alf06.document.reader.databinding.ActivitySplashBinding
import com.alf06.document.reader.ui.home.MainActivity
import com.ui.baselib.base.BaseActivity
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@SuppressLint("CustomSplashScreen")
class SplashActivity : BaseActivity<ActivitySplashBinding>(ActivitySplashBinding::inflate) {
    override val fullStatus: Boolean
        get() = true

    override fun backPressed() {

    }

    override fun initialize() {

    }

    override fun ActivitySplashBinding.setData() {
        lifecycleScope.launch {

            delay(3000L)
            startActivity(
                Intent(this@SplashActivity, MainActivity::class.java)
            )
            finishAffinity()
        }
    }

    override fun ActivitySplashBinding.onClick() = Unit
}
