package com.wxiwei.office.pg.model

class PGNotes(notes: String?) {
    private var notes: String? = notes

    fun setNotes(notes: String?) { this.notes = notes }
    fun getNotes(): String? = notes
    fun dispose() { notes = null }
}
