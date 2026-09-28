/*
 * Modifications Copyright (c) 2026 dongb2002. All rights reserved.
 *
 * This file is based on third-party open-source code and has been modified by dongb2002.
 * The modifications are proprietary to dongb2002. The original copyright and license notice
 * of this file, where present below, remains in effect for the original portions.
 */
package com.wxiwei.office.common.picture

import com.wxiwei.office.system.DocumentCoroutines.launch
import kotlinx.coroutines.Job

class PictureConverterThread(
    private val converterMgr: PictureConverterMgr,
    private val sourPath: String?,
    private val destPath: String?,
    private val type: String?
) : PictureConversionTask {
    private var job: Job? = null

    override fun start(): Job? {
        if (job != null && job!!.isActive) return job
        job = launch(converterMgr.control, object : Runnable {
            override fun run() {
                converterMgr.convertPNG(sourPath, destPath, type, false)
            }
        })
        return job
    }

    override fun cancel() {
        if (job != null) job!!.cancel(null)
    }
}
