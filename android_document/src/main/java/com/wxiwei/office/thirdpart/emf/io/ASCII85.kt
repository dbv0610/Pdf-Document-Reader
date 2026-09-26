// Copyright 2001, FreeHEP.
package com.wxiwei.office.thirdpart.emf.io

interface ASCII85 {
    companion object {
        @JvmField
        val MAX_CHARS_PER_LINE: Int = 80

        @JvmField
        val a85p1: Long = 85

        @JvmField
        val a85p2: Long = a85p1 * a85p1

        @JvmField
        val a85p3: Long = a85p2 * a85p1

        @JvmField
        val a85p4: Long = a85p3 * a85p1
    }
}
