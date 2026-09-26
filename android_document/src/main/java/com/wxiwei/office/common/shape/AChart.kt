/*
 * 文件名称:          aChartData.java
 *  
 * 编译器:            android2.2
 * 时间:              下午3:16:53
 */
package com.wxiwei.office.common.shape

import android.graphics.Bitmap
import android.graphics.Bitmap.CompressFormat
import android.graphics.Canvas
import com.wxiwei.office.common.PaintKit
import com.wxiwei.office.common.picture.Picture
import com.wxiwei.office.fc.ppt.reader.ReaderKit.Companion.instance
import com.wxiwei.office.system.IControl
import com.wxiwei.office.thirdpart.achartengine.chart.AbstractChart
import java.io.File
import java.io.FileOutputStream

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
 * 日期:            2012-1-19
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
class AChart : AbstractShape() {
    /**
     * 
     * (non-Javadoc)
     * @see AbstractShape.getType
     */
    override val type: Short
        get() = AbstractShape.Companion.SHAPE_CHART

    private fun saveChartToPicture(control: IControl) {
        var bmp: Bitmap? = null
        try {
            val width = (bounds!!.width * this.aChart!!.getZoomRate()).toInt()
            val height = (bounds!!.height * this.aChart!!.getZoomRate()).toInt()
            bmp = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bmp)


            //canvas.drawBitmap(this.bmp, matrix, paint);
            aChart!!.draw(
                canvas,
                control,
                0,
                0,
                width,
                height,
                PaintKit.Companion.instance().getPaint()
            )

            canvas.save()

            canvas.restore()
            val pic = Picture()

            val name = System.currentTimeMillis().toString() + ".tmp"
            val file = File(
                control.getSysKit().getPictureManage().getPicTempPath() + File.separator + name
            )
            file.createNewFile()

            val out = FileOutputStream(file)
            bmp.compress(CompressFormat.PNG, 100, out)
            bmp.recycle()
            out.close()

            pic.tempFilePath = file.getAbsolutePath()
            picIndex = control.getSysKit().getPictureManage().addPicture(pic)
        } catch (e: Exception) {
            if (bmp != null) {
                bmp.recycle()
            }
            control.getSysKit().getErrorKit().writerLog(e)
        }
    }

    /**
     * 
     * @return
     */
    fun getDrawingPicture(control: IControl): Int {
        if (picIndex == -1) {
            saveChartToPicture(control)
        }

        return picIndex
    }

    override fun dispose() {
        super.dispose()
        this.aChart = null
    }

    //    private Bitmap bmp;
    private var picIndex = -1

    /**
     * 
     * @return
     */
    /**
     * 
     * @param chart
     */
    var aChart: AbstractChart? = null
}
