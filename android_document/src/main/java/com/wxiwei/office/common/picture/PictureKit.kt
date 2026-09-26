/*
 * 文件名称:          PictureKit.java
 *  
 * 编译器:            android2.2
 * 时间:              下午4:12:38
 */
package com.wxiwei.office.common.picture

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.Rect
import com.wxiwei.office.common.pictureefftect.PictureCroppedInfo
import com.wxiwei.office.common.pictureefftect.PictureEffectInfo
import com.wxiwei.office.common.pictureefftect.PictureEffectUtil
import com.wxiwei.office.constant.EventConstant
import com.wxiwei.office.pg.animate.IAnimation
import com.wxiwei.office.pg.animate.ShapeAnimation
import com.wxiwei.office.system.IControl
import java.io.FileInputStream
import java.io.IOException
import java.lang.Boolean
import kotlin.BooleanArray
import kotlin.Byte
import kotlin.Exception
import kotlin.Float
import kotlin.FloatArray
import kotlin.Int
import kotlin.IntArray
import kotlin.String
import kotlin.Suppress
import kotlin.Throws
import kotlin.concurrent.Volatile
import kotlin.math.abs
import kotlin.math.ceil
import kotlin.math.hypot
import kotlin.math.max
import kotlin.math.sqrt

/**
 * 图片处理工具类
 * 
 * 
 * 
 * 
 * Read版本:        Read V1.0
 * 
 * 
 * 作者:            ljj8494
 * 
 * 
 * 日期:            2011-11-16
 * 
 * 
 * 负责人:          ljj8494
 * 
 * 
 * 负责小组:
 * 
 * 
 * 
 * 
 */
class PictureKit
private constructor() {
    /**
     * 
     * 
     * @param canvas         画布
     * @param pic            绘制图片对象
     * @param x              x值
     * @param y              y值
     * @param zoom           是否缩放
     * @param destWidth      如果缩放，指定缩放后的宽度
     * @param destHeight     如果缩放，指定缩放后的高度
     * 
     * @throws OutOfMemoryError
     */
    @Synchronized
    @Throws(OutOfMemoryError::class)
    fun drawPicture(
        canvas: Canvas, control: IControl?, viewIndex: Int, pic: Picture?, x: Float, y: Float,
        zoom: Float, destWidth: Float, destHeight: Float, effectInfor: PictureEffectInfo?
    ) {
        drawPicture(
            canvas,
            control,
            viewIndex,
            pic,
            x,
            y,
            zoom,
            destWidth,
            destHeight,
            effectInfor,
            null
        )
    }

    private fun applyEffect(paint: Paint, effectInfor: PictureEffectInfo?) {
        if (effectInfor != null) {
            val cMatrix = ColorMatrix()

            //black&white
            if (effectInfor.blackWhiteThreshold != null) {
                cMatrix.set(PictureEffectUtil.getBlackWhiteArray(effectInfor.blackWhiteThreshold!!))
            } else if (effectInfor.isGrayScale == true) {
                cMatrix.set(PictureEffectUtil.grayScaleArray)
            }


            //brightness and contrast
            val brightness = effectInfor.brightness
            val contrast = effectInfor.getContrast()
            if (brightness != null && contrast != null) {
                val cm = ColorMatrix()
                cm.set(PictureEffectUtil.getBrightAndContrastArray(brightness, contrast))
                cMatrix.preConcat(cm)
            } else if (brightness != null) {
                val cm = ColorMatrix()
                cm.set(PictureEffectUtil.getBrightnessArray(brightness.toInt()))
                cMatrix.preConcat(cm)
            } else if (contrast != null) {
                val cm = ColorMatrix()
                cm.set(PictureEffectUtil.getContrastArray(contrast))
                cMatrix.preConcat(cm)
            }

            paint.setColorFilter(ColorMatrixColorFilter(cMatrix))
        }
    }

    /**
     * @param canvas         画布
     * @param pic            绘制图片对象
     * @param x              x值
     * @param y              y值
     * @param zoom           是否缩放
     * @param destWidth      如果缩放，指定缩放后的宽度
     * @param destHeight     如果缩放，指定缩放后的高度
     * 
     * @throws OutOfMemoryError
     */
    @Synchronized
    @Throws(OutOfMemoryError::class)
    fun drawPicture(
        canvas: Canvas,
        control: IControl?,
        viewIndex: Int,
        pic: Picture?,
        x: Float,
        y: Float,
        zoom: Float,
        destWidth: Float,
        destHeight: Float,
        effectInfor: PictureEffectInfo?,
        animation: IAnimation?
    ) {
        if (control != null && pic != null && pic.tempFilePath != null) {
            if (animation != null && animation.getCurrentAnimationInfor()!!.getAlpha() == 0) {
                return
            }

            val ret = drawPicture(
                canvas, control, viewIndex, pic.tempFilePath!!, pic.pictureType, null,
                x, y, zoom, destWidth, destHeight, effectInfor, animation
            )
            if (ret != null) {
                if (ret.equals(FAIL, ignoreCase = true)) {
                    //picture is too large, do not decode it any more
                    pic.tempFilePath = null
                } else {
                    //wmf image to jpg
                    pic.pictureType = Picture.Companion.PNG
                    pic.tempFilePath = ret
                }
            }
        }
    }

    private fun drawCropedPicture(
        canvas: Canvas, x: Float, y: Float, destWidth: Float, destHeight: Float,
        sBitmap: Bitmap, croppedInfor: PictureCroppedInfo?
    ): kotlin.Boolean {
        if (croppedInfor != null) {
            var destCrop: Rect? = null
            var srcCrop: Rect? = null
            val srcWidth = sBitmap.getWidth()
            val srcHeight = sBitmap.getHeight()

            var left = (srcWidth * croppedInfor.leftOff).toInt()
            var top = (srcHeight * croppedInfor.topOff).toInt()
            var right = (srcWidth * (1 - croppedInfor.rightOff)).toInt()
            var bottom = (srcHeight * (1 - croppedInfor.bottomOff)).toInt()

            destCrop = Rect(left, top, right, bottom)

            left = if (left >= 0) left else 0
            top = if (top >= 0) top else 0
            right = if (right >= srcWidth) srcWidth else right
            bottom = if (bottom >= srcHeight) srcHeight else bottom
            srcCrop = Rect(left, top, right, bottom)

            canvas.save()

            val matrix = Matrix()
            val zoomX = destWidth / (destCrop.width())
            val zoomY = destHeight / (destCrop.height())
            matrix.postScale(zoomX, zoomY)

            var offX = destCrop.left * zoomX
            var offY = destCrop.top * zoomY
            matrix.postTranslate(x - offX, y - offY)

            offX = if (offX >= 0) 0f else offX
            offY = if (offY >= 0) 0f else offY
            canvas.clipRect(
                x - offX,
                y - offY,
                x - offX + srcCrop.width() * zoomX,
                y - offY + srcCrop.height() * zoomY
            )

            canvas.drawBitmap(sBitmap, matrix, paint)

            canvas.restore()
        }

        return true
    }

    /**
     * 
     * @param canvas         画布
     * @param data           byte数组的图片数据
     * @param offset         数组的开始位置
     * @param len            长度
     * @param x              x值
     * @param y              y值
     * @param zoom           是否缩放
     * @param destWidth      如果缩放，指定缩放后的宽度
     * @param destHeight     如果缩放，指定缩放后的高度
     * @return F  单个文件都会OOM, decode失败;  Null not drawing picture ; other content: wmf2Jpg file path
     */
    private fun drawPicture(
        canvas: Canvas,
        control: IControl,
        viewIndex: Int,
        path: String,
        imageType: Byte,
        options: BitmapFactory.Options?,
        x: Float,
        y: Float,
        zoom: Float,
        destWidth: Float,
        destHeight: Float,
        effectInfor: PictureEffectInfo?,
        animation: IAnimation?
    ): String? {
        var options = options
        var x = x
        var y = y
        var destWidth = destWidth
        var destHeight = destHeight
        try {
            var sBitmap = control.getSysKit().getPictureManage().getBitmap(path)
            // Off-screen renders (thumbnails) decode at the size they draw and keep it out of the
            // shared cache: full-size decodes there evicted the pictures on screen, which then
            // vanished while flinging and were decoded again on the UI thread.
            val offscreen = forceDrawOnThread.get() === Boolean.TRUE
            if (sBitmap == null) {
                if (!this.isDrawPictrue) {
                    return null
                }

                if (control.getSysKit().getPictureManage().isConverting(path)) {
                    control.getSysKit().getPictureManage().appendViewIndex(path, viewIndex)
                    markPendingPicture()
                    return null
                }

                if (imageType == Picture.Companion.WMF || imageType == Picture.Companion.EMF) {
                    if (control.isSlideShow()) {
                        control.getSysKit().getAnimationManager().killAnimationTimer()
                    }

                    var w = (destWidth / zoom).toInt()
                    var h = (destHeight / zoom).toInt()
                    if (w * h < VectorMaxSize) {
                        var z = sqrt((VectorMaxSize / (w * h)).toDouble())
                        if (z > VectorMaxZOOM) {
                            z = VectorMaxZOOM.toDouble()
                        }
                        w = Math.round(w * z).toInt()
                        h = Math.round(h * z).toInt()
                    }

                    val dst = control.getSysKit().getPictureManage().convertVectorgraphToPng(
                        viewIndex, imageType, path,
                        w, h, control.isSlideShow()
                    )
                    if (!control.isSlideShow()) {
                        markPendingPicture()
                    }

                    if (control.isSlideShow()) {
                        control.getSysKit().getAnimationManager().restartAnimationTimer()

                        control.actionEvent(EventConstant.TEST_REPAINT_ID, null)
                    }

                    return dst
                } else {
                    try {
                        sBitmap = if (offscreen)
                            decodeSampled(path, canvas, destWidth, destHeight)
                        else
                            decodeFile(path, options)
                        if (sBitmap == null) {
                            //load fail, so call library to convert it to normal png image
                            if (control.isSlideShow()) {
                                control.getSysKit().getAnimationManager().killAnimationTimer()
                            }

                            var dst: String? = null
                            if (imageType == Picture.Companion.JPEG) {
                                // android only supports RGB color space, but some JPEG picture is
                                // CMYK color space, it returns null when call BitmapFactory.decodeStream,
                                // so call lib to load picture
                                dst = control.getSysKit().getPictureManage().convertToPng(
                                    viewIndex,
                                    path,
                                    Picture.Companion.JPEG_TYPE,
                                    control.isSlideShow()
                                )
                            } else if (imageType == Picture.Companion.PNG) {
                                dst = control.getSysKit().getPictureManage().convertToPng(
                                    viewIndex,
                                    path,
                                    Picture.Companion.PNG_TYPE,
                                    control.isSlideShow()
                                )
                            }

                            if (control.isSlideShow()) {
                                control.getSysKit().getAnimationManager().restartAnimationTimer()

                                control.actionEvent(EventConstant.TEST_REPAINT_ID, null)
                            } else {
                                markPendingPicture()
                            }

                            return dst
                        }
                    } catch (e: Exception) {
                        return FAIL
                    }
                }
                if (sBitmap == null) {
                    return FAIL
                }

                if (!offscreen) {
                    control.getSysKit().getPictureManage().addBitmap(path, sBitmap)
                }
            }

            if (animation != null) {
                val shapeAnim = animation.getShapeAnimation()
                val paraBegin = shapeAnim!!.getParagraphBegin()
                val paraEnd = shapeAnim.getParagraphEnd()

                if (paraBegin == ShapeAnimation.Para_All && paraEnd == ShapeAnimation.Para_All
                    || (paraBegin == ShapeAnimation.Para_BG && paraEnd == ShapeAnimation.Para_BG)
                ) {
                    val a = animation.getCurrentAnimationInfor()!!.getAlpha()
                    paint.setAlpha(a)

                    val rate = a / 255f * 0.5f

                    val centerX = x + destWidth / 2
                    val centerY = y + destHeight / 2
                    x = centerX - destWidth * rate
                    y = centerY - destHeight * rate
                    destWidth *= rate * 2
                    destHeight *= rate * 2
                }
            }


            //picture effect           
            //transparent
            var isTransparentBMP = false
            if (effectInfor != null && effectInfor.transparentColor != null) {
                val bmp =
                    createTransparentBitmapFromBitmap(sBitmap, effectInfor.transparentColor!!)
                if (bmp != null) {
                    sBitmap = bmp
                    isTransparentBMP = true
                }
            }


            //alpha
            if (effectInfor != null && effectInfor.alpha != null) {
                paint.setAlpha(effectInfor.alpha!!)
            }
            //other effect
            applyEffect(paint, effectInfor)
            paint.setAntiAlias(true)
            paint.setFilterBitmap(true)
            if (effectInfor == null || effectInfor.pictureCroppedInfor == null) {
                val matrix = Matrix()
                matrix.postScale(
                    destWidth / sBitmap.getWidth(),
                    destHeight / sBitmap.getHeight()
                )
                matrix.postTranslate(x, y)

                canvas.drawBitmap(sBitmap, matrix, paint)
            } else {
                drawCropedPicture(
                    canvas,
                    x,
                    y,
                    destWidth,
                    destHeight,
                    sBitmap,
                    effectInfor.pictureCroppedInfor
                )
            }

            paint.reset()

            if (isTransparentBMP) {
                sBitmap.recycle()
            }

            return null
        } catch (e: OutOfMemoryError) {
            if (control.getSysKit().getPictureManage().hasBitmap()) {
                control.getSysKit().getPictureManage().clearBitmap()
                return drawPicture(
                    canvas,
                    control,
                    viewIndex,
                    path,
                    imageType,
                    options,
                    x,
                    y,
                    zoom,
                    destWidth,
                    destHeight,
                    effectInfor,
                    animation
                )
            } else {
                if (options == null) {
                    options = BitmapFactory.Options()
                    options.inSampleSize = 2
                } else {
                    options.inSampleSize *= 2
                }

                return drawPicture(
                    canvas,
                    control,
                    viewIndex,
                    path,
                    imageType,
                    options,
                    x,
                    y,
                    zoom,
                    destWidth,
                    destHeight,
                    effectInfor,
                    animation
                )
            }
        } catch (e: Exception) {
            return FAIL
        }
    }

    /**
     * create a transparent bitmap from an existing bitmap by replacing certain
     * color with transparent
     * 
     * @param bitmap
     * the original bitmap with a color you want to replace
     * @return a replaced color immutable bitmap
     */
    fun createTransparentBitmapFromBitmap(bitmap: Bitmap?, replaceThisColor: Int): Bitmap? {
        if (bitmap != null) {
            val diff = 10
            val picw = bitmap.getWidth()
            val pich = bitmap.getHeight()
            val pix = IntArray(picw * pich)
            bitmap.getPixels(pix, 0, picw, 0, 0, picw, pich)

            for (y in 0..<pich) {
                // from left to right
                for (x in 0..<picw) {
                    val index = y * picw + x
                    val r = (pix[index] shr 16) and 0xff
                    val g = (pix[index] shr 8) and 0xff
                    val b = pix[index] and 0xff

                    val tr = (replaceThisColor shr 16) and 0xff
                    val tg = (replaceThisColor shr 8) and 0xff
                    val tb = replaceThisColor and 0xff

                    if (abs(tr - r) <= diff && abs(tg - g) <= diff && abs(tb - b) <= diff) {
                        pix[index] = 0
                    }
                }
            }

            return Bitmap.createBitmap(pix, picw, pich, Bitmap.Config.ARGB_4444)
        }

        return null
    }

    /**
     * 
     * @param canvas
     * @param bitmap
     * @param x
     * @param y
     * @param zoom
     * @param destWidth
     * @param destHeight
     */
    fun drawPicture(
        canvas: Canvas,
        control: IControl,
        bitmap: Bitmap?,
        x: Float,
        y: Float,
        zoom: kotlin.Boolean,
        destWidth: Float,
        destHeight: Float
    ) {
        try {
            if (bitmap == null) {
                return
            }
            val matrix = Matrix()
            matrix.postScale(
                destWidth / bitmap.getWidth(),
                destHeight / bitmap.getHeight()
            )
            matrix.postTranslate(x, y)
            canvas.drawBitmap(bitmap, matrix, paint)
        } catch (e: OutOfMemoryError) {
            control.getSysKit().getErrorKit().writerLog(e)
        }
    }

    /**
     * 
     * @param pic
     * @return
     */
    fun isVectorPicture(pic: Picture?): kotlin.Boolean {
        if (pic != null) {
            val imageType = pic.pictureType
            if (imageType == Picture.Companion.WMF || imageType == Picture.Companion.EMF) {
                return true
            }
        }

        return false
    }

    /**
     * Starts recording, on the calling thread, pictures left out because they are still being
     * converted (WMF/EMF, CMYK JPEG); the converter fires EventConstant.TEST_REPAINT_ID when one is ready.
     */
    fun startTrackingPendingPictures() {
        pendingOnThread.set(BooleanArray(1))
    }

    /**
     * Stops the recording started by [.startTrackingPendingPictures].
     * 
     * @return true when a picture was left out since then
     */
    fun stopTrackingPendingPictures(): kotlin.Boolean {
        val pending = pendingOnThread.get()
        pendingOnThread.remove()
        return pending != null && pending[0]
    }

    private fun markPendingPicture() {
        val pending = pendingOnThread.get()
        if (pending != null) {
            pending[0] = true
        }
    }

    /**
     * Makes pictures always draw on the calling thread, whatever [.setDrawPictrue] says,
     * without touching that shared flag: the UI turns it off while scrolling, and a background
     * render (thumbnails, PDF export) toggling it would fight the UI thread.
     * 
     * @return the previous value, to restore in a finally block
     */
    fun setForceDrawOnCurrentThread(force: kotlin.Boolean): kotlin.Boolean {
        val previous = forceDrawOnThread.get() === Boolean.TRUE
        if (force) {
            forceDrawOnThread.set(Boolean.TRUE)
        } else {
            forceDrawOnThread.remove()
        }
        return previous
    }

    //
    private val paint = Paint()

    /**
     * @param isDrawPictrue The isDrawPictrue to set.
     */
    //
    @Volatile
    var isDrawPictrue: kotlin.Boolean = true
        /**
         * @return Returns the isDrawPictrue.
         */
        get() = field || forceDrawOnThread.get() === Boolean.TRUE

    //
    private val forceDrawOnThread = ThreadLocal<kotlin.Boolean?>()

    //
    private val pendingOnThread = ThreadLocal<BooleanArray?>() //
    //private String filePath;    
    /**
     * 
     */
    init {
        paint.setAntiAlias(true)
    }

    companion object {
        private const val FAIL = "Fail"

        //
        private val kit = PictureKit()

        private const val VectorMaxZOOM = 3

        private val VectorMaxSize = 1024 * 1024

        /**
         * 
         * @return
         */
        fun instance(): PictureKit {
            return kit
        }

        @Throws(IOException::class)
        private fun decodeFile(path: String?, options: BitmapFactory.Options?): Bitmap? {
            FileInputStream(path).use { `in` ->
                return BitmapFactory.decodeStream(`in`, null, options)
            }
        }

        /**
         * Decodes [path] at the smallest power-of-two reduction still at least as large as it is
         * drawn: [destWidth] x [destHeight] under the canvas' scale.
         */
        @Suppress("deprecation") // getMatrix: fine on the software canvas of an off-screen render
        @Throws(IOException::class)
        private fun decodeSampled(
            path: String?,
            canvas: Canvas,
            destWidth: Float,
            destHeight: Float
        ): Bitmap? {
            val bounds = BitmapFactory.Options()
            bounds.inJustDecodeBounds = true
            decodeFile(path, bounds)
            val m = FloatArray(9)
            canvas.getMatrix().getValues(m)
            val needW = max(
                1,
                ceil(
                    destWidth * hypot(
                        m[Matrix.MSCALE_X].toDouble(),
                        m[Matrix.MSKEW_Y].toDouble()
                    )
                ).toInt()
            )
            val needH = max(
                1,
                ceil(
                    destHeight * hypot(
                        m[Matrix.MSKEW_X].toDouble(),
                        m[Matrix.MSCALE_Y].toDouble()
                    )
                ).toInt()
            )
            var sample = 1
            while (bounds.outWidth / (sample * 2) >= needW && bounds.outHeight / (sample * 2) >= needH) {
                sample *= 2
            }
            val options = BitmapFactory.Options()
            options.inSampleSize = sample
            return decodeFile(path, options)
        }
    }
}
