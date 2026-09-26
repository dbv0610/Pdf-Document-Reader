package com.wxiwei.office.pg.model

class PGBulletText {
    private var bulletTexts: MutableList<String>? = ArrayList()

    fun addBulletText(text: String): Int {
        val size = bulletTexts!!.size
        bulletTexts!!.add(text)
        return size
    }

    fun getBulletText(index: Int): String? {
        if (index < 0 || index >= bulletTexts!!.size) return null
        return bulletTexts!![index]
    }

    fun dispose() {
        bulletTexts?.clear()
        bulletTexts = null
    }
}
