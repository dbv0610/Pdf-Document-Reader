package com.wxiwei.office.system

import java.io.File
import java.util.concurrent.ConcurrentHashMap

/**
 * Passwords the user typed for encrypted documents, keyed by absolute path. The host sets one
 * before reopening a file; readers look it up here instead of threading it through
 * MainControl/FileReaderThread.
 */
object DocumentPasswords {
    private val passwords = ConcurrentHashMap<String, String>()

    @JvmStatic
    fun set(path: String, password: String) {
        passwords[key(path)] = password
    }

    @JvmStatic
    fun get(path: String?): String? = path?.let { passwords[key(it)] }

    @JvmStatic
    fun clear(path: String?) {
        path?.let { passwords.remove(key(it)) }
    }

    private fun key(path: String) = File(path).absolutePath
}
