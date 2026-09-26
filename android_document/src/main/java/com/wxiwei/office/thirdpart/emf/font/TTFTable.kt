// Copyright 2001, FreeHEP.
package com.wxiwei.office.thirdpart.emf.font

import java.io.IOException

/**
 * Concrete instances derived from this class hold data stored in true type
 * tables. Right now the data is accessible as public attributes. In some cases
 * methods may return more convenient objects (such as Shapes instead of point
 * arrays).
 * 
 * @author Simon Fischer
 * @version $Id: TTFTable.java 8584 2006-08-10 23:06:37Z duns $
 */
abstract class TTFTable {
    private var ttfFont: TTFFont? = null

    lateinit var ttf: TTFInput

    var isRead: Boolean = false
        private set

    @Throws(IOException::class)
    fun init(font: TTFFont, ttf: TTFInput) {
        this.ttfFont = font
        this.ttf = ttf
    }

    @Throws(IOException::class)
    fun read() {
        ttf.pushPos()
        print("[" + getTag())
        ttf.seek(0)
        readTable()
        isRead = true
        print("]")
        ttf.popPos()
    }

    @Throws(IOException::class)
    abstract fun readTable()

    abstract fun getTag(): String?

    @Throws(IOException::class)
    fun getTable(tag: String?): TTFTable? {
        return ttfFont!!.getTable(tag)
    }

    // --------------------------------------------------------------------------------
    override fun toString(): String {
        return (if (::ttf.isInitialized) ttf.toString() else "null") + ": [" + getTag() + "/" + javaClass.getName() + "]"
    }

    companion object {
        val TT_TAGS: Array<String?> = arrayOf<String?>(
            "cmap", "glyf",
            "head", "hhea", "hmtx", "loca", "maxp", "name", "OS/2", "post"
        )

        val TABLE_CLASSES: Array<Class<*>?> = arrayOf<Class<*>?>(
            TTFCMapTable::class.java, TTFGlyfTable::class.java, TTFHeadTable::class.java,
            TTFHHeaTable::class.java, TTFHMtxTable::class.java, TTFLocaTable::class.java,
            TTFMaxPTable::class.java, TTFNameTable::class.java, TTFOS_2Table::class.java,
            TTFPostTable::class.java
        )
    }
}
