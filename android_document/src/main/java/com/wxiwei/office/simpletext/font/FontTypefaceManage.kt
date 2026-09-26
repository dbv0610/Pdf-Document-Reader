/*
 * 文件名称:          FontNameManage.java
 *
 * 编译器:            android2.2
 * 时间:              下午3:17:42
 */
package com.wxiwei.office.simpletext.font

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
    private var tfs: LinkedHashMap<String, Typeface>? = null

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
     *
     */
    fun getFontTypeface(index: Int): Typeface {
        /*Typeface ty = index == -1 ? Typeface.create(Typeface.MONOSPACE, Typeface.NORMAL) :
                Typeface.create(sysFontName.get(index), Typeface.NORMAL);
        return ty;*/
        if (tfs == null) {
            tfs = LinkedHashMap()
        }
        var fontName = if (index < 0) "sans-serif" else sysFontName!![index]
        if (fontName == null) {
            fontName = "sans-serif"
        }
        //fontName = "Arial";
        embeddedFonts[fontName]?.let { return it }
        var tf = tfs!![fontName]
        if (tf == null) {
            tf = Typeface.create(fontName, Typeface.NORMAL)
            if (tf == null) {
                tf = Typeface.DEFAULT
            }
            tfs!![fontName] = tf!!
        }
        return tf!!
    }

    fun addEmbeddedFont(fontName: String, typeface: Typeface) {
        embeddedFonts[fontName] = typeface
    }

    fun hasEmbeddedFont(fontName: String): Boolean = embeddedFonts.containsKey(fontName)

    /**
     *
     */
    fun dispose() {
    }

    companion object {
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
