package com.reader.pdfviewer.pdfium.util

import kotlin.Any
import kotlin.Boolean
import kotlin.Int
import kotlin.String

class SizeF(val width: Float, val height: Float) {
    override fun equals(obj: Any?): Boolean {
        if (obj == null) {
            return false
        }
        if (this === obj) {
            return true
        }
        if (obj is SizeF) {
            val other = obj
            return width == other.width && height == other.height
        }
        return false
    }

    override fun toString(): String {
        return width.toString() + "x" + height
    }

    override fun hashCode(): Int {
        return width.toBits() xor height.toBits()
    }

    fun toSize(): Size {
        return Size(width.toInt(), height.toInt())
    }
}
