/*
 * Copyright 2001-2005 (C) MetaStuff, Ltd. All Rights Reserved.
 *
 * This software is open source.
 * See the bottom of this file for the licence.
 */
package com.wxiwei.office.fc.dom4j.tree

import java.io.Serializable
import java.util.concurrent.ConcurrentHashMap

/**
 * Thread-safe hash map based on [ConcurrentHashMap] for dom4j caching.
 */
open class ConcurrentReaderHashMap @JvmOverloads constructor(
    initialCapacity: Int = 16,
    loadFactor: Float = 0.75f
) : ConcurrentHashMap<Any?, Any?>(initialCapacity, loadFactor), Serializable {
    companion object {
        private const val serialVersionUID = 1L
    }
}
