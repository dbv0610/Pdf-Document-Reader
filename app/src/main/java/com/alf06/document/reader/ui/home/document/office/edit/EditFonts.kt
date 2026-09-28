package com.alf06.document.reader.ui.home.document.office.edit

import android.graphics.Typeface
import com.wxiwei.office.simpletext.font.FontTypefaceManage

/**
 * Fonts offered by the Word editor. The Office ones are drawn with the library's metric twins
 * (Arial -> Arimo...); the others are the device's own families, registered under the name written
 * into the file. Another app opening the file uses its own font of that name.
 */
internal object EditFonts {
    val names = listOf("Arial", "Times New Roman", "Calibri", "Courier New", "Roboto", "Noto Serif")

    private var registered = false

    fun register() {
        if (registered) return
        registered = true
        FontTypefaceManage.instance().registerAppFont("Roboto", Typeface.SANS_SERIF)
        FontTypefaceManage.instance().registerAppFont("Noto Serif", Typeface.SERIF)
    }

    /** How [name] is drawn, for the list. */
    fun typeface(name: String): Typeface {
        register()
        val kit = FontTypefaceManage.instance()
        return kit.getFontTypeface(kit.addFontName(name))
    }
}
