package com.wxiwei.office.system

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.graphics.drawable.Drawable
import android.graphics.drawable.ShapeDrawable
import android.graphics.drawable.shapes.RoundRectShape
import android.net.Uri
import android.os.Environment
import android.os.StatFs
import com.wxiwei.office.common.bookmark.BookmarkManage
import com.wxiwei.office.common.borders.BordersManage
import com.wxiwei.office.common.bulletnumber.ListManage
import com.wxiwei.office.common.hyperlink.HyperlinkManage
import com.wxiwei.office.common.picture.PictureManage
import com.wxiwei.office.pg.animate.AnimationManager
import com.wxiwei.office.pg.model.PGBulletText
import com.wxiwei.office.system.beans.CalloutView.CalloutManager
import com.wxiwei.office.wp.control.WPShapeManage
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import java.io.File
import kotlin.jvm.JvmName

class SysKit(control: IControl) {
    private var errorKitInstance: ErrorUtil? = null
    private var pmKit: PictureManage? = null
    private var hmKit: HyperlinkManage? = null
    private var lmKit: ListManage? = null
    private var pgLMKit: PGBulletText? = null
    private var brKit: BordersManage? = null
    private var wpSMKit: WPShapeManage? = null
    private var bmKit: BookmarkManage? = null
    private var control: IControl? = control
    private var animationMgr: AnimationManager? = null
    private var calloutMgr: CalloutManager? = null

    /**
     * Scope of everything this document runs in the background: reading, Word layout, picture
     * conversion, timers, thumbnails. [dispose] cancels it, which stops all of them together.
     * Failures are logged instead of reaching the thread's uncaught-exception handler; the library
     * no longer installs a process-wide one, so anything outside this scope crashes as usual.
     */
    val coroutineScope: CoroutineScope = CoroutineScope(
        SupervisorJob() + Dispatchers.Default + CoroutineName("office-document") +
            CoroutineExceptionHandler { _, error ->
                OpenTrace.e("document coroutine failed", error)
                try {
                    errorKitInstance?.writerLog(error)
                } catch (_: Exception) {
                }
            }
    )

    fun getSDPath(): File? {
        if (File("/mnt/extern_sd").exists() || File("/mnt/usbhost1").exists()) {
            return File("/mnt")
        }
        val state = Environment.getExternalStorageState()
        if (Environment.MEDIA_MOUNTED == state) {
            return Environment.getExternalStorageDirectory()
        }
        return null
    }

    fun getAvailableStore(filePath: String): Long {
        val statFs = StatFs(filePath)
        val blocSize = statFs.blockSizeLong
        val availaBlock = statFs.availableBlocksLong
        return availaBlock * blocSize
    }

    fun isVertical(context: Context): Boolean {
        return context.resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT
    }

    fun charsetEncode(str: String, encode: String): String {
        if (str == "") {
            return ""
        }
        val strBuff = StringBuffer("")
        try {
            val bytes = str.toByteArray(charset(encode))
            for (b in bytes) {
                val value = Integer.toHexString(b.toInt() and 0xFF)
                if (value.length == 1) {
                    strBuff.append("0").append(value)
                } else {
                    strBuff.append(value)
                }
            }
            val chars = strBuff.toString().toCharArray()
            strBuff.delete(0, strBuff.length)
            var i = 0
            while (i < chars.size) {
                strBuff.append("%").append(chars[i]).append(chars[i + 1])
                i += 2
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return strBuff.toString()
    }

    fun internetSearch(strValue: String, activity: Activity) {
        val str = charsetEncode(strValue, "utf-8")
        var url = "http://www.google.com.hk/#hl=en&newwindow=1&safe=strict&site=&q=a-a-a-a&oq=a-a-a-a&aq=f&aqi=&aql=&gs_sm=3" +
            "&gs_upl=1075l1602l0l1935l3l3l0l0l0l0l0l0ll0l0&gs_l=hp.3...1075l1602l0l1935l3l3l0l0l0l0l0l0ll0l0&bav=on.2,or.r_gc.r_pw.,cf.osb" +
            "&fp=207f1fbbc21b7536&biw=1280&bih=876"
        url = url.replace("a-a-a-a", str)
        activity.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
    }

    fun isDebug(): Boolean = false
    fun getControl(): IControl = control!!
    fun setControl(control: IControl?) {
        this.control = control
    }

    fun getErrorKit(): ErrorUtil {
        if (errorKitInstance == null) errorKitInstance = ErrorUtil(this)
        return errorKitInstance!!
    }

    fun getPictureManage(): PictureManage {
        if (pmKit == null) pmKit = PictureManage(control!!)
        return pmKit!!
    }

    fun getHyperlinkManage(): HyperlinkManage {
        if (hmKit == null) hmKit = HyperlinkManage()
        return hmKit!!
    }

    fun getListManage(): ListManage {
        if (lmKit == null) lmKit = ListManage()
        return lmKit!!
    }

    fun getPGBulletText(): PGBulletText {
        if (pgLMKit == null) pgLMKit = PGBulletText()
        return pgLMKit!!
    }

    fun getBordersManage(): BordersManage {
        if (brKit == null) brKit = BordersManage()
        return brKit!!
    }

    fun getWPShapeManage(): WPShapeManage {
        if (wpSMKit == null) wpSMKit = WPShapeManage()
        return wpSMKit!!
    }

    fun getBookmarkManage(): BookmarkManage {
        if (bmKit == null) bmKit = BookmarkManage()
        return bmKit!!
    }

    fun getAnimationManager(): AnimationManager {
        if (animationMgr == null) animationMgr = AnimationManager(control!!)
        return animationMgr!!
    }

    fun getCalloutManager(): CalloutManager {
        if (calloutMgr == null) calloutMgr = CalloutManager(control!!)
        return calloutMgr!!
    }

    @get:JvmName("getErrorKitValue")
    val errorKit: ErrorUtil
        get() = getErrorKit()

    @get:JvmName("getPictureManageValue")
    val pictureManage: PictureManage
        get() = getPictureManage()

    @get:JvmName("getCalloutManagerValue")
    val calloutManager: CalloutManager
        get() = getCalloutManager()

    fun dispose() {
        coroutineScope.cancel()
        control = null
        errorKitInstance?.dispose()
        errorKitInstance = null
        pmKit?.dispose()
        pmKit = null
        hmKit?.dispose()
        hmKit = null
        lmKit?.dispose()
        lmKit = null
        pgLMKit?.dispose()
        pgLMKit = null
        brKit?.dispose()
        brKit = null
        wpSMKit?.dispose()
        wpSMKit = null
        bmKit?.dispose()
        bmKit = null
        animationMgr?.dispose()
        animationMgr = null
        calloutMgr?.dispose()
        calloutMgr = null
    }

    companion object {
        private var pageNumberDrawable: ShapeDrawable? = null

        @JvmStatic
        fun getPageNubmerDrawable(): Drawable {
            if (pageNumberDrawable == null) {
                pageNumberDrawable = ShapeDrawable(
                    RoundRectShape(floatArrayOf(6f, 6f, 6f, 6f, 6f, 6f, 6f, 6f), null, null)
                )
                pageNumberDrawable!!.paint.color = 0x88FF8844.toInt()
            }
            return pageNumberDrawable!!
        }

        @JvmStatic
        fun isValidateRect(pageWidth: Int, pageHeight: Int, x: Int, y: Int, width: Int, height: Int): Boolean {
            return x >= 0 && y >= 0 && x < pageWidth && y < pageHeight && width >= 0 && height >= 0 &&
                x + width <= pageWidth && y + height <= pageHeight
        }
    }
}
