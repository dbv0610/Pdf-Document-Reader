/*
 * 文件名称:          PictureConverterThread.java
 *  
 * 编译器:            android2.2
 * 时间:              下午4:33:03
 */
package com.wxiwei.office.common.picture

import com.wxiwei.office.system.DocumentCoroutines.launch
import kotlinx.coroutines.Job

/**
 * TODO: 文件注释
 * 
 * 
 * 
 * 
 * Read版本:        Read V1.0
 * 
 * 
 * 作者:            jqin
 * 
 * 
 * 日期:            2013-4-25
 * 
 * 
 * 负责人:           jqin
 * 
 * 
 * 负责小组:
 * 
 * 
 * 
 * 
 */


class VectorgraphConverterThread(
    private val converterMgr: PictureConverterMgr,
    private val type: Byte,
    private val sourPath: String?,
    private val destPath: String?,
    private val picWidth: Int,
    private val picHeight: Int
) : PictureConversionTask {
    private var job: Job? = null

    override fun start(): Job? {
        if (job != null && job!!.isActive) return job
        job = launch(converterMgr.control, object : Runnable {
            override fun run() {
                converterMgr.convertWMF_EMF(type, sourPath, destPath, picWidth, picHeight, false)
            }
        })
        return job
    }

    override fun cancel() {
        if (job != null) job!!.cancel(null)
    }
}
