package com.wxiwei.office.officereader.filelist

class FileSortType {
    private var sdcardType = 0
    private var recentType = -1
    private var starType = 0
    private var sdcardAscending = 0
    private var recentAscending = 0
    private var starAscending = 0

    fun getSdcardType(): Int = sdcardType
    fun getRecentType(): Int = recentType
    fun getStarType(): Int = starType
    fun getSdcardAscending(): Int = sdcardAscending
    fun getRecentAscending(): Int = recentAscending
    fun getStarAscending(): Int = starAscending

    fun setSdcardType(type: Int, ascending: Int) {
        sdcardType = type
        sdcardAscending = ascending
    }

    fun setRecentType(type: Int, ascending: Int) {
        recentType = type
        recentAscending = ascending
    }

    fun setStarType(type: Int, ascending: Int) {
        starType = type
        starAscending = ascending
    }

    fun dispos() {
    }
}
