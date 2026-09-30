package com.alf06.document.reader

import androidx.core.content.ContextCompat
import androidx.multidex.MultiDexApplication
import com.alf06.document.reader.di.appModule
import com.alf06.document.reader.di.viewModelModule
import com.alf06.document.reader.utils.LocateManager
import com.alf06.document.reader.utils.PreferenceHelper
import com.editor.docsdk.DialogStyle
import com.google.firebase.FirebaseApp
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.component.KoinComponent
import org.koin.core.component.get
import org.koin.core.context.startKoin
import com.ui.baselib.base.LocateManager as LibLocateManager

class MyApplication : MultiDexApplication(), KoinComponent {


    override fun onCreate() {
        super.onCreate()

        LocateManager.initDeviceLocate()
        startKoin {
            androidLogger()
            androidContext(this@MyApplication)
            modules(
                listOf(
                    appModule,
                    viewModelModule,
                )
            )
        }
        LibLocateManager.languageProvider = { get<PreferenceHelper>().languageSelected }
        FirebaseApp.initializeApp(this)
        DialogStyle.customizer = DialogStyle.Customizer { context, style ->
            style.copy(accent = ContextCompat.getColor(context, R.color.primary))
        }
    }
}
