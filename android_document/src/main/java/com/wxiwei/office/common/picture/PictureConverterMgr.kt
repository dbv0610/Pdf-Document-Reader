/*
 * 文件名称:          PictureConverterThread.java
 *  
 * 编译器:            android2.2
 * 时间:              下午3:30:35
 */
package com.wxiwei.office.common.picture

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import com.wxiwei.office.constant.EventConstant
import com.wxiwei.office.system.IControl
import com.wxiwei.office.thirdpart.emf.util.EMFUtil
import java.io.FileInputStream
import java.io.InputStream

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
class PictureConverterMgr
    (
    /** The document this converter works for; its coroutine scope runs the conversions.  */
    var control: IControl?
) {
    @Synchronized
    fun addConvertPicture(
        viewIndex: Int,
        type: Byte,
        srcPath: String?,
        dstPath: String?,
        width: Int,
        height: Int,
        singleThread: Boolean
    ) {
        control!!.actionEvent(EventConstant.SYS_SET_PROGRESS_BAR_ID, true)

        if (singleThread) {
            convertWMF_EMF(type, srcPath, dstPath, width, height, true)
            if (this.isIdle) {
                control!!.actionEvent(EventConstant.SYS_SET_PROGRESS_BAR_ID, false)
            }
        } else {
            val thread = VectorgraphConverterThread(this, type, srcPath, dstPath, width, height)

            convertingThread.add(thread)
            convertingPictPathMap!!.put(dstPath, thread)

            val listIndex: MutableList<Int?> = ArrayList<Int?>()
            listIndex.add(viewIndex)
            vectorgraphViews.put(dstPath, listIndex)

            if (viewVectorgraphs.get(viewIndex) == null) {
                val listPath: MutableList<String?> = ArrayList<String?>()
                listPath.add(dstPath)
                viewVectorgraphs.put(viewIndex, listPath)
            } else {
                viewVectorgraphs.get(viewIndex)!!.add(dstPath)
            }

            if (convertingThread.size == 1) {
                //is not converting now, so start the thread
                convertingThread.get(convertingThread.size - 1)!!.start()
            }
        }
    }

    fun convertWMF_EMF(
        type: Byte,
        sourPath: String?,
        destPath: String?,
        picWidth: Int,
        picHeight: Int,
        thumbnail: Boolean
    ) {
        try {
            var sBitmap: Bitmap? = null
            if (type == Picture.Companion.WMF) {
                // WMF needs the native converter that shipped with the removed PDF module; not rendered
            } else if (type == Picture.Companion.EMF) {
                sBitmap = EMFUtil.convert(sourPath, destPath, picWidth, picHeight)
            }

            if (control != null && ((!thumbnail && !isConversionActive(destPath)) || control!!.getView() == null)) {
                //has disposed
                return
            }

            if (sBitmap != null) {
                control!!.getSysKit().getPictureManage().addBitmap(destPath, sBitmap)
                finishConversion(destPath, thumbnail)

                if (!thumbnail) {
                    control!!.actionEvent(EventConstant.TEST_REPAINT_ID, null)
                }
            } else {
                finishConversion(destPath, thumbnail)
            }
        } catch (e: OutOfMemoryError) {
            if (control!!.getSysKit().getPictureManage().hasBitmap()) {
                control!!.getSysKit().getPictureManage().clearBitmap()
                convertWMF_EMF(type, sourPath, destPath, picWidth, picHeight, thumbnail)
            } else {
                control!!.getSysKit().getErrorKit().writerLog(e)
                finishConversion(destPath, thumbnail)
            }
        } catch (e: Exception) {
            if (control != null && ((!thumbnail && !isConversionActive(destPath)) || control!!.getView() == null)) {
                //has disposed
                return
            }

            control!!.getSysKit().getErrorKit().writerLog(e)
            finishConversion(destPath, thumbnail)
        }
    }


    @Synchronized
    fun addConvertPicture(
        viewIndex: Int,
        srcPath: String?,
        dstPath: String?,
        picType: String?,
        singleThread: Boolean
    ) {
        control!!.actionEvent(EventConstant.SYS_SET_PROGRESS_BAR_ID, true)

        if (singleThread) {
            convertPNG(srcPath, dstPath, picType, true)
            if (this.isIdle) {
                control!!.actionEvent(EventConstant.SYS_SET_PROGRESS_BAR_ID, false)
            }
        } else {
            val thread = PictureConverterThread(this, srcPath, dstPath, picType)

            convertingThread.add(thread)
            convertingPictPathMap!!.put(dstPath, thread)

            val listIndex: MutableList<Int?> = ArrayList<Int?>()
            listIndex.add(viewIndex)
            vectorgraphViews.put(dstPath, listIndex)

            if (viewVectorgraphs.get(viewIndex) == null) {
                val listPath: MutableList<String?> = ArrayList<String?>()
                listPath.add(dstPath)
                viewVectorgraphs.put(viewIndex, listPath)
            } else {
                viewVectorgraphs.get(viewIndex)!!.add(dstPath)
            }

            if (convertingThread.size == 1) {
                //is not converting now, so start the thread
                convertingThread.get(convertingThread.size - 1)!!.start()
            }
        }
    }

    fun convertPNG(sourPath: String?, destPath: String?, picType: String?, thumbnail: Boolean) {
        try {
            // the native PNG converter shipped with the removed PDF module
            val ret = false

            if (control != null && ((!thumbnail && !isConversionActive(destPath)) || control!!.getView() == null)) {
                //has disposed
                return
            }

            if (ret) {
                val `in`: InputStream = FileInputStream(destPath)
                val sBitmap = BitmapFactory.decodeStream(`in`, null, null)
                if (sBitmap != null) {
                    control!!.getSysKit().getPictureManage().addBitmap(destPath, sBitmap)
                    finishConversion(destPath, thumbnail)

                    if (!thumbnail) {
                        control!!.actionEvent(EventConstant.TEST_REPAINT_ID, null)
                    }
                } else {
                    finishConversion(destPath, thumbnail)
                }
            } else {
                finishConversion(destPath, thumbnail)
            }
        } catch (e: OutOfMemoryError) {
            if (control!!.getSysKit().getPictureManage().hasBitmap()) {
                control!!.getSysKit().getPictureManage().clearBitmap()
                convertPNG(sourPath, destPath, picType, thumbnail)
            } else {
                control!!.getSysKit().getErrorKit().writerLog(e)
                finishConversion(destPath, thumbnail)
            }
        } catch (e: Exception) {
            if (control != null && ((!thumbnail && !isConversionActive(destPath)) || control!!.getView() == null)) {
                //has disposed
                return
            }

            control!!.getSysKit().getErrorKit().writerLog(e)
            finishConversion(destPath, thumbnail)
        }
    }

    /**
     * 
     * @param path
     */
    fun remove(path: String?) {
        var updateViewList: MutableList<Int?>? = null
        val allConverted: Boolean
        // Read before locking: it walks the view tree, which has locks of its own. The document
        // may be disposed by now (PGControl's view is then gone), which must not kill this thread.
        var currentViewIndex: Int
        try {
            currentViewIndex = control!!.getCurrentViewIndex()
        } catch (e: Exception) {
            currentViewIndex = -1
        }
        // Collected under the lock, sent after it: the events reach the views (exportImage draws),
        // and a drawing thread holds PictureKit's lock while it asks isPictureConverting.
        synchronized(this) {
            if (convertingPictPathMap == null) {
                return
            }
            val thread = convertingPictPathMap.remove(path)
            convertingThread.remove(thread)

            val viewList = vectorgraphViews.remove(path)
            if (viewList != null) {
                for (i in viewList.indices) {
                    val viewIndex: Int = viewList.get(i)!!
                    val vectorgraphs = viewVectorgraphs.get(viewIndex)
                    if (vectorgraphs == null) {
                        continue
                    }
                    vectorgraphs.remove(path)
                    if (vectorgraphs.size == 0) {
                        //all vector graphs contained in this view have been converted
                        //so notify to update this view
                        viewVectorgraphs.remove(viewIndex)

                        if (updateViewList == null) {
                            updateViewList = ArrayList<Int?>()
                        }

                        updateViewList.add(viewIndex)
                    }
                }
            }

            if (convertingThread.size > 0) {
                //check current view vector graphs
                val vectorgraphs = viewVectorgraphs.get(currentViewIndex)
                var next = if (vectorgraphs != null && vectorgraphs.size > 0)
                    convertingPictPathMap.get(vectorgraphs.get(0))
                else
                    null
                if (next == null) {
                    //start the last vector graph converting thread
                    next = convertingThread.get(convertingThread.size - 1)
                }
                next!!.start()
            }
            allConverted = convertingPictPathMap.size == 0
        }

        if (updateViewList != null && updateViewList.size > 0) {
            if (updateViewList.contains(currentViewIndex)) {
                control!!.actionEvent(EventConstant.APP_GENERATED_PICTURE_ID, null)
            }

            control!!.actionEvent(EventConstant.SYS_VECTORGRAPH_PROGRESS, updateViewList)
        }

        if (allConverted) {
            control!!.actionEvent(EventConstant.SYS_SET_PROGRESS_BAR_ID, false)
        }
    }

    /**
     * Ends a conversion. [sync] ones (singleThread, the "thumbnail" flag of convertWMF_EMF and
     * convertPNG) ran inside addConvertPicture and were never queued, so there is nothing to remove.
     */
    private fun finishConversion(destPath: String?, sync: Boolean) {
        if (!sync) {
            remove(destPath)
        }
    }

    @get:Synchronized
    private val isIdle: Boolean
        get() = convertingPictPathMap == null || convertingPictPathMap.isEmpty()

    /** False once dispose() or remove() dropped the conversion to [destPath].  */
    @Synchronized
    private fun isConversionActive(destPath: String?): Boolean {
        return convertingPictPathMap != null && convertingPictPathMap.get(destPath) != null
    }

    @Synchronized
    fun hasConvertingVectorgraph(viewIndex: Int): Boolean {
        return viewVectorgraphs.containsKey(viewIndex)
    }

    /**
     * 
     * @param path
     * @return
     */
    @Synchronized
    fun isPictureConverting(path: String?): Boolean {
        return vectorgraphViews.containsKey(path)
    }

    /**
     * call when vector graph has been converted, but with different view index
     * eg. different pages has same vector graph
     * @param path
     * @param viewIndex
     */
    @Synchronized
    fun appendViewIndex(path: String?, viewIndex: Int) {
        val views = vectorgraphViews.get(path)
        if (views != null) {
            views.add(viewIndex)

            if (viewVectorgraphs.get(viewIndex) == null) {
                val listPath: MutableList<String?> = ArrayList<String?>()
                listPath.add(path)
                viewVectorgraphs.put(viewIndex, listPath)
            } else {
                viewVectorgraphs.get(viewIndex)!!.add(path)
            }
        }
    }

    @Synchronized
    fun dispose() {
        if (convertingPictPathMap != null) {
            val iter = convertingPictPathMap.values.iterator()
            while (iter.hasNext()) {
                try {
                    iter.next()!!.cancel()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
            convertingPictPathMap.clear()

            vectorgraphViews.clear()
            viewVectorgraphs.clear()
        }
    }

    //last-in，first-out
    private val convertingThread: MutableList<PictureConversionTask?>

    //
    private val convertingPictPathMap: MutableMap<String?, PictureConversionTask?>?

    //vector graph path and view indexs which contains this vector graph
    private val vectorgraphViews: MutableMap<String?, MutableList<Int?>?>

    //view index and vector graphs which is contained in this view
    private val viewVectorgraphs: MutableMap<Int?, MutableList<String?>?>

    init {
        convertingThread = ArrayList<PictureConversionTask?>()
        convertingPictPathMap = HashMap<String?, PictureConversionTask?>()
        vectorgraphViews = HashMap<String?, MutableList<Int?>?>()
        viewVectorgraphs = HashMap<Int?, MutableList<String?>?>()
    }
}
