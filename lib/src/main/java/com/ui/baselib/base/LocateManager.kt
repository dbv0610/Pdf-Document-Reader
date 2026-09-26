package com.ui.baselib.base

import android.content.Context
import android.content.res.Configuration
import android.content.res.Resources
import androidx.core.content.edit
import java.util.Locale

object LocateManager {
    private const val PREF_NAME = "data"
    private const val KEY_LANGUAGE = "KEY_LANGUAGE"

    /**
     * Where the user's chosen language lives, when the app keeps it outside this module
     * (set once in Application.onCreate). Null falls back to what [saveLocale] stored.
     */
    var languageProvider: ((Context) -> String?)? = null

    private var cacheLocateInit = ""

    // In-memory copy so attachBaseContext doesn't hit SharedPreferences for every activity.
    @Volatile
    private var savedLanguage: String? = null

    fun saveLocale(context: Context, lang: String) {
        savedLanguage = lang
        prefs(context).edit { putString(KEY_LANGUAGE, lang) }
    }

    fun initDeviceLocate() {
        cacheLocateInit = Resources.getSystem().configuration.locales[0].language
    }

    fun getDeviceLanguageCode(): String =
        cacheLocateInit.ifEmpty { Resources.getSystem().configuration.locales[0].language }

    fun getPreLanguage(mContext: Context): String {
        val chosen = languageProvider?.invoke(mContext)
            ?: savedLanguage
            ?: prefs(mContext).getString(KEY_LANGUAGE, null).orEmpty().also { savedLanguage = it }
        return chosen.ifBlank { getDeviceLanguageCode() }
    }

    fun createLocale(context: Context, languageCode: String): Context {
        if (languageCode.isBlank()) return context
        val parts = languageCode.split("_")
        val locale = Locale.Builder()
            .setLanguage(parts[0])
            .apply { if (parts.size > 1) setRegion(parts[1].removePrefix("r")) }
            .build()
        Locale.setDefault(locale)
        val config = Configuration(context.resources.configuration)
        config.setLocale(locale)
        config.setLayoutDirection(locale)
        return context.createConfigurationContext(config)
    }

    private fun prefs(context: Context) =
        context.getSharedPreferences(context.packageName + PREF_NAME, Context.MODE_PRIVATE)
}
