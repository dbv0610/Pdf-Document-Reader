package com.alf06.document.reader.utils

import android.content.Context
import android.content.res.Configuration
import android.content.res.Resources
import android.os.LocaleList
import java.util.Locale

object LocateManager {
    private var cacheLocateInit = ""

    fun initDeviceLocate(code: String = "") {
        cacheLocateInit = code.ifEmpty { Resources.getSystem().configuration.locales[0].language }
    }

    fun getDeviceLanguageCode(): String = cacheLocateInit


    fun createAppContext(context: Context, languageCode: String): Context {
        val locale = createLocale(languageCode ?: getDeviceLanguageCode())
        Locale.setDefault(locale)
        val config = Configuration(context.resources.configuration)
        config.setLocale(locale)
        config.setLayoutDirection(locale)
        return context.createConfigurationContext(config)
    }

    private fun createLocale(languageCode: String): Locale {
        val parts = languageCode.split("_")
        return if (parts.size > 1) {
            val region = parts[1].removePrefix("r")
            Locale.Builder()
                .setLanguage(parts[0])
                .setRegion(region)
                .build()
        } else {
            Locale.Builder()
                .setLanguage(parts[0])
                .build()
        }
    }
}

private fun String.toLocaleOrNull(): Locale? {
    return try {
        val l = Locale.forLanguageTag(this)
        if (l.language.isNullOrBlank()) null else l
    } catch (_: Throwable) {
        null
    }
}

private fun LocaleList.getOrNull(index: Int): Locale? =
    if (index in 0 until this.size()) this.get(index) else null
