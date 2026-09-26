package com.wxiwei.office.system

import android.util.Log

/** Centralized diagnostics for the file-open pipeline. */
object OpenTrace {
    private const val TAG = "OPEN_TRACE"

    @JvmStatic
    fun d(message: String) = Log.d(TAG, message)

    @JvmStatic
    fun e(message: String, error: Throwable? = null) {
        if (error == null) Log.e(TAG, message) else Log.e(TAG, message, error)
    }

    @JvmStatic
    fun mark(stage: String, start: Long? = null) {
        val elapsed = start?.let { " elapsed=${android.os.SystemClock.uptimeMillis() - it}ms" } ?: ""
        d("stage=$stage thread=${Thread.currentThread().name}$elapsed")
    }
}
