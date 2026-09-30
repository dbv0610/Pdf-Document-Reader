/*
 * Copyright (c) 2026 dongb2002. All rights reserved.
 *
 * Proprietary and confidential. Unauthorized copying, modification or distribution of this
 * file, via any medium, is strictly prohibited without the written permission of dongb2002.
 */
package com.wxiwei.office.editor

/**
 * An undo stack that forgets its oldest steps past [limit], so a long editing session does not keep
 * every model snapshot and removed text alive. Undo only ever takes the newest step, and a dropped
 * step's change stays applied (and saved), it just can no longer be undone.
 */
internal class UndoStack<T>(private val limit: Int = MAX_UNDO) : ArrayList<T>() {
    override fun add(element: T): Boolean {
        super.add(element)
        if (size > limit) removeAt(0)
        return true
    }

    companion object {
        const val MAX_UNDO = 200
    }
}
