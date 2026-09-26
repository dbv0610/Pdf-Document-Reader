package com.wxiwei.office.pg.model

class PGPlaceholderUtil private constructor() {
    companion object {
        const val TITLE = "title"
        const val CTRTITLE = "ctrTitle"
        const val SUBTITLE = "subTitle"
        const val BODY = "body"
        const val HALF = "half"
        const val DT = "dt"
        const val FTR = "ftr"
        const val SLDNUM = "sldNum"
        const val HEADER = "hdr"
        const val OBJECT = "obj"
        const val CHART = "chart"
        const val TABLE = "tbl"
        const val CLIPART = "clipArt"
        const val DIAGRAM = "dgm"
        const val MEDIA = "media"
        const val SLIDEIMAGE = "sldImg"
        const val PICTURE = "pic"
        private val kit = PGPlaceholderUtil()
        @JvmStatic fun instance(): PGPlaceholderUtil = kit
    }
    fun isTitle(type: String?): Boolean = TITLE == type || CTRTITLE == type
    fun isBody(type: String?): Boolean = !(TITLE == type || CTRTITLE == type || DT == type || FTR == type || SLDNUM == type)
    fun isTitleOrBody(type: String?): Boolean = TITLE == type || CTRTITLE == type || SUBTITLE == type || BODY == type
    fun isHeaderFooter(type: String?): Boolean = DT == type || FTR == type || SLDNUM == type
    fun isDate(type: String?): Boolean = DT == type
    fun isFooter(type: String?): Boolean = FTR == type
    fun isSldNum(type: String?): Boolean = SLDNUM == type
    fun checkTypeName(type: String?): String? = if (CTRTITLE == type) TITLE else type
    fun processType(name: String?, type: String?): String? = type
}
