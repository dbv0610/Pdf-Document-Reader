/*
 * 文件名称:          FontNameManage.java
 *
 * 编译器:            android2.2
 * 时间:              下午3:17:42
 */
package com.wxiwei.office.simpletext.font

import android.content.res.AssetManager
import android.graphics.Typeface

/**
 * font name manage
 *
 * Read版本:        Read V1.0
 *
 * 作者:            ljj8494
 *
 * 日期:            2012-10-16
 *
 * 负责人:          ljj8494
 */
class FontTypefaceManage {
    //
    //private LinkedHashMap<String, Integer> sysFont;
    //
    private var sysFontName: MutableList<String?>? = null

    //
    // read on the reader, UI and thumbnail threads
    private val tfs = java.util.concurrent.ConcurrentHashMap<String, Typeface>()

    @Volatile
    private var assets: AssetManager? = null

    // fonts embedded in the open document, keyed by typeface name; read on the reader thread
    private val embeddedFonts = java.util.concurrent.ConcurrentHashMap<String, Typeface>()

    /**
     *
     */
    init {
        /*sysFont = new LinkedHashMap<String, Integer>();
        sysFontName = new ArrayList<String>();
        File[] files = new File("/system/fonts").listFiles();
        if (files != null)
        {
            String name;
            int index;
            for (int i = 0; i < files.length; i++)
            {
                name = files[i].getName().toLowerCase();
                index = name.lastIndexOf(".");
                if (index > 0)
                {
                    name = name.substring(0, index);
                }
                sysFont.put(name, i);
                sysFontName.add(name);
            }
        }*/
    }

    /**
     *
     */
    fun addFontName(fontName: String?): Int {
        /*Integer a = sysFont.get(fontName.toLowerCase());
        return a == null ? -1 : a;*/
        if (sysFontName == null) {
            sysFontName = ArrayList()
        }
        var a = sysFontName!!.indexOf(fontName)
        if (a < 0) {
            a = sysFontName!!.size
            sysFontName!!.add(fontName)
        }
        return a
    }

    /**
     * Regular typeface of [index]: a font embedded in the document, else a bundled font with the
     * same metrics as the Office font (Arial -> Arimo...), else the system font of that name.
     */
    fun getFontTypeface(index: Int): Typeface {
        val fontName = (if (index < 0) null else sysFontName?.getOrNull(index)) ?: "sans-serif"
        embeddedFonts[fontName]?.let { return it }
        return tfs.getOrPut(fontName) {
            bundledTypeface(fontName, bold = false, italic = false)
                ?: Typeface.create(fontName, Typeface.NORMAL) ?: Typeface.DEFAULT
        }
    }

    /** Lets the manager load the bundled fonts in assets/fonts; call once with any context. */
    fun setAssets(assets: AssetManager) {
        if (this.assets == null) this.assets = assets
    }

    /** A bundled face for an Office font name, or null when none is bundled or it cannot load. */
    private fun bundledTypeface(fontName: String, bold: Boolean, italic: Boolean): Typeface? {
        val assets = assets ?: return null
        val family = BUNDLED[fontName.trim().lowercase()] ?: return null
        val file = family.file(bold, italic) ?: return null
        return try {
            if (family.variable) {
                Typeface.Builder(assets, "fonts/$file")
                    .setFontVariationSettings("'wght' ${if (bold) 700 else 400}")
                    .build()
            } else {
                Typeface.createFromAsset(assets, "fonts/$file")
            }
        } catch (e: RuntimeException) {
            null
        }
    }

    fun addEmbeddedFont(fontName: String, typeface: Typeface) {
        embeddedFonts[fontName] = typeface
        styled.clear()
    }

    /** Marks an embedded family whose regular face is already heavy (OS/2 weight >= 600). */
    fun markBoldFace(fontName: String) {
        boldFaces.add(fontName)
    }

    // fonts named for their bold face ("Montserrat Bold": Canva and others embed each weight as a family)
    private val boldFaces = java.util.Collections.newSetFromMap(java.util.concurrent.ConcurrentHashMap<String, Boolean>())
    private val styled = java.util.concurrent.ConcurrentHashMap<Long, Typeface>()

    private fun isBoldFace(name: String?): Boolean =
        name != null && (boldFaces.contains(name) || BOLD_NAME.containsMatchIn(name))

    /**
     * Typeface of [index] in the requested style. Android picks the family's real bold/italic face
     * and only synthesizes one when the family has none, so callers must not fake bold/italic.
     * A family that already is bold is not emboldened again.
     */
    fun getFontTypeface(index: Int, bold: Boolean, italic: Boolean): Typeface {
        val base = getFontTypeface(index)
        val name = if (index < 0) null else sysFontName?.getOrNull(index)
        val style = (if (bold && !isBoldFace(name)) Typeface.BOLD else 0) or (if (italic) Typeface.ITALIC else 0)
        if (style == Typeface.NORMAL) return base
        val key = (index.toLong() shl 8) or style.toLong()
        styled[key]?.let { return it }
        val wantBold = style and Typeface.BOLD != 0
        val wantItalic = style and Typeface.ITALIC != 0
        val bundled = if (name == null || embeddedFonts.containsKey(name)) null else BUNDLED[name.trim().lowercase()]
        val typeface = if (bundled != null) {
            // the exact bundled face; a missing one (Cousine italic) is synthesized from the nearest
            val exact = if (bundled.file(wantBold, wantItalic) != null) bundledTypeface(name!!, wantBold, wantItalic) else null
            exact ?: Typeface.create(bundledTypeface(name!!, wantBold, false) ?: base, if (wantItalic) Typeface.ITALIC else 0)
        } else {
            Typeface.create(base, style)
        }
        return typeface.also { styled[key] = it }
    }

    fun hasEmbeddedFont(fontName: String): Boolean = embeddedFonts.containsKey(fontName)

    /**
     *
     */
    fun dispose() {
    }

    /** Font files in assets/fonts; [variable] fonts carry every weight (wght axis) in one file. */
    private class Bundled(
        val regular: String, val bold: String?, val italic: String?, val boldItalic: String?,
        val variable: Boolean = false,
    ) {
        fun file(bold: Boolean, italic: Boolean): String? = when {
            variable -> if (italic) this.italic else regular
            bold && italic -> boldItalic
            bold -> this.bold
            italic -> this.italic
            else -> regular
        }
    }

    companion object {
        // Metric-compatible open fonts (same advance widths as the Office font, so lines break
        // where Word/Excel break them); Consolas and Cambria have no such twin, a close one is used.
        private val ARIMO = Bundled("Arimo-VF.ttf", "Arimo-VF.ttf", "Arimo-Italic-VF.ttf", "Arimo-Italic-VF.ttf", variable = true)
        private val TINOS = Bundled("Tinos-Regular.ttf", "Tinos-Bold.ttf", "Tinos-Italic.ttf", "Tinos-BoldItalic.ttf")
        private val COUSINE = Bundled("Cousine-Regular.ttf", "Cousine-Bold.ttf", null, null)
        private val CARLITO = Bundled("Carlito-Regular.ttf", "Carlito-Bold.ttf", "Carlito-Italic.ttf", "Carlito-BoldItalic.ttf")
        private val BUNDLED: Map<String, Bundled> = mapOf(
            "arial" to ARIMO, "arial unicode ms" to ARIMO, "helvetica" to ARIMO, "helvetica neue" to ARIMO,
            "liberation sans" to ARIMO, "arimo" to ARIMO,
            "times new roman" to TINOS, "times" to TINOS, "liberation serif" to TINOS, "tinos" to TINOS, "cambria" to TINOS,
            "courier new" to COUSINE, "courier" to COUSINE, "liberation mono" to COUSINE, "cousine" to COUSINE, "consolas" to COUSINE,
            "calibri" to CARLITO, "calibri light" to CARLITO, "carlito" to CARLITO,
        )

        private val BOLD_NAME = Regex("(?i)(^|[ -])(semi ?bold|demi ?bold|extra ?bold|ultra ?bold|bold|black|heavy)($|[ -])")

        //
        private var kit: FontTypefaceManage? = null

        /**
         *
         */
        @JvmStatic
        fun instance(): FontTypefaceManage {
            if (kit == null) {
                kit = FontTypefaceManage()
            }
            return kit!!
        }
    }
}
