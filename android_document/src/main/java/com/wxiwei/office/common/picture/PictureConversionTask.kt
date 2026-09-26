package com.wxiwei.office.common.picture

import kotlinx.coroutines.Job

/** Deferred coroutine task used by the sequential picture conversion queue.  */
internal interface PictureConversionTask {
    fun start(): Job?
    fun cancel()
}
