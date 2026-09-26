package com.wxiwei.office.pg.control

import android.app.Activity
import android.app.Dialog
import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.text.ClipboardManager
import android.view.View
import com.wxiwei.office.common.ICustomDialog
import com.wxiwei.office.common.IOfficeToPicture
import com.wxiwei.office.common.ISlideShow
import com.wxiwei.office.common.hyperlink.Hyperlink
import com.wxiwei.office.constant.DialogConstant
import com.wxiwei.office.constant.EventConstant
import com.wxiwei.office.constant.MainConstant
import com.wxiwei.office.java.awt.Dimension
import com.wxiwei.office.java.awt.Rectangle
import com.wxiwei.office.pg.model.PGModel
import com.wxiwei.office.system.AbstractControl
import com.wxiwei.office.system.IControl
import com.wxiwei.office.system.DocumentCoroutines
import com.wxiwei.office.system.OpenTrace
import com.wxiwei.office.system.IFind
import com.wxiwei.office.system.IMainFrame
import com.wxiwei.office.system.SysKit
import java.util.Vector

class PGControl(mainControl: IControl, pgModel: PGModel, filePath: String?) : AbstractControl() {
    private var isDispose = false
    private var pgView: Presentation? = null
    private var mainControl: IControl? = mainControl
    private var isShowingProgressDlg = false
    private var progressDialog: ProgressDialog? = null

    init {
        val start = android.os.SystemClock.uptimeMillis()
        OpenTrace.mark("pgControl.constructor.begin slides=${pgModel.getSlideCount()} loaded=${pgModel.getRealSlideCount()}")
        this.mainControl = mainControl
        pgView = Presentation(getMainFrame()!!.getActivity(), pgModel, this)
        OpenTrace.mark("pgControl.constructor.end", start)
    }

    override fun canBackLayout(): Boolean = false
    override fun setLayoutThreadDied(isDied: Boolean) {}
    override fun setStopDraw(isStopDraw: Boolean) {}
    override fun layoutView(x: Int, y: Int, w: Int, h: Int) {}

    override fun actionEvent(actionID: Int, obj: Any?) {
        val view = pgView ?: return
        when (actionID) {
            EventConstant.SYS_SET_PROGRESS_BAR_ID -> view.post {
                if (!isDispose) mainControl?.getMainFrame()?.showProgressBar(obj as Boolean)
            }
            EventConstant.SYS_VECTORGRAPH_PROGRESS -> {
                @Suppress("UNCHECKED_CAST")
                val images = obj as List<Int>
                val update = { if (!isDispose) mainControl?.getMainFrame()?.updateViewImages(images) }
                if (view.parent != null) view.post(update) else DocumentCoroutines.launch(mainControl) { update() }
            }
            EventConstant.SYS_INIT_ID -> {
                val start = android.os.SystemClock.uptimeMillis()
                OpenTrace.mark("pgControl.SYS_INIT.begin")
                view.init()
                OpenTrace.mark("pgControl.SYS_INIT.end", start)
            }
            EventConstant.TEST_REPAINT_ID, EventConstant.PG_REPAINT_ID -> view.postInvalidate()
            EventConstant.SYS_UPDATE_TOOLSBAR_BUTTON_STATUS -> view.post {
                if (!isDispose) getMainFrame()?.updateToolsbarStatus()
            }
            EventConstant.APP_PAGE_UP_ID -> if (view.isSlideShow()) view.slideShow(ISlideShow.SlideShow_PreviousSlide)
            else if (view.getCurrentIndex() > 0) view.showSlide(view.getCurrentIndex() - 1, false)
            EventConstant.APP_PAGE_DOWN_ID -> if (view.isSlideShow()) view.slideShow(ISlideShow.SlideShow_NextSlide)
            else if (view.getCurrentIndex() < view.getRealSlideCount() - 1) view.showSlide(view.getCurrentIndex() + 1, false)
            EventConstant.PG_SHOW_SLIDE_ID -> if (!view.isSlideShow()) showSlide(obj as Int)
            EventConstant.APP_ZOOM_ID -> if (!view.isSlideShow()) {
                val params = obj as IntArray
                view.setZoom(params[0].toFloat() / MainConstant.STANDARD_RATE, params[1], params[2])
                view.post { if (!isDispose) getMainFrame()?.changeZoom() }
            }
            EventConstant.PG_NOTE_ID -> {
                val text = view.getCurrentSlide()!!.getNotes()?.getNotes() ?: ""
                val vector = Vector<Any>()
                vector.add(text)
                com.wxiwei.office.pg.dialog.NotesDialog(this, getMainFrame()!!.getActivity()!!, null, vector, DialogConstant.SHOW_PG_NOTE_ID).show()
            }
            EventConstant.FILE_COPY_ID -> {
                @Suppress("DEPRECATION")
                val clip = getMainFrame()!!.getActivity()!!.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                @Suppress("DEPRECATION")
                clip.text = view.getSelectedText()
            }
            EventConstant.APP_HYPERLINK -> {
                val address = (obj as Hyperlink).getAddress()
                if (address != null) try {
                    getMainFrame()!!.getActivity()!!.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(address)))
            } catch (e: Exception) {
                OpenTrace.e("PowerPoint hyperlink action failed", e)
            }
            }
            EventConstant.SYS_AUTO_TEST_FINISH_ID -> if (isAutoTest()) getMainFrame()!!.getActivity()!!.onBackPressed()
            EventConstant.APP_GENERATED_PICTURE_ID -> view.post { if (!isDispose) view.createPicture() }
            EventConstant.PG_SLIDESHOW_DURATION -> view.setAnimationDuration(obj as Int)
            EventConstant.PG_SLIDESHOW_GEGIN -> {
                getMainFrame()!!.fullScreen(true)
                view.beginSlideShow(if (obj == null) view.getCurrentIndex() + 1 else obj as Int)
            }
            EventConstant.PG_SLIDESHOW_END -> view.endSlideShow()
            EventConstant.PG_SLIDESHOW_PREVIOUS -> view.slideShow(ISlideShow.SlideShow_PreviousStep)
            EventConstant.PG_SLIDESHOW_NEXT -> view.slideShow(ISlideShow.SlideShow_NextStep)
            EventConstant.APP_COUNT_PAGES_CHANGE_ID -> pagesCountChanged()
            EventConstant.APP_SET_FIT_SIZE_ID -> view.setFitSize(obj as Int)
            EventConstant.APP_INIT_CALLOUTVIEW_ID -> view.initCalloutView()
        }
    }

    private fun pagesCountChanged() {
        val view = pgView ?: return
        if (isShowingProgressDlg && view.showLoadingSlide()) {
            isShowingProgressDlg = false
            view.post {
                val frame = getMainFrame() ?: return@post
                if (frame.isShowProgressBar()) {
                    progressDialog?.dismiss()
                    progressDialog = null
                } else mainControl?.getCustomDialog()?.dismissDialog(ICustomDialog.DIALOGTYPE_LOADING)
            }
        }
    }

    private fun showSlide(slideIndex: Int) {
        val view = pgView ?: return
        if (slideIndex < 0 || slideIndex >= view.getSlideCount()) return
        isShowingProgressDlg = false
        if (slideIndex >= view.getRealSlideCount()) {
            isShowingProgressDlg = true
            if (getMainFrame()!!.isShowProgressBar()) {
                view.postDelayed({
                    if (isShowingProgressDlg) {
                        progressDialog = ProgressDialog.show(getActivity(), getMainFrame()!!.getAppName(), getMainFrame()!!.getLocalString("DIALOG_LOADING"), false, false, null)
                        progressDialog?.show()
                    }
                }, 200)
            } else mainControl?.getCustomDialog()?.showDialog(ICustomDialog.DIALOGTYPE_LOADING)
        }
        view.showSlide(slideIndex, false)
    }

    override fun getActionValue(actionID: Int, obj: Any?): Any? {
        val view = pgView ?: return null
        return when (actionID) {
            EventConstant.APP_ZOOM_ID -> view.getZoom()
            EventConstant.APP_COUNT_PAGES_ID -> view.getSlideCount()
            EventConstant.APP_GET_REAL_PAGE_COUNT_ID -> view.getRealSlideCount()
            EventConstant.APP_CURRENT_PAGE_NUMBER_ID -> view.getCurrentIndex() + 1
            EventConstant.APP_FIT_ZOOM_ID -> view.getFitZoom()
            EventConstant.PG_SLIDE_TO_IMAGE -> view.slideToImage(obj as Int)
            EventConstant.APP_PAGEAREA_TO_IMAGE -> (obj as? IntArray)?.takeIf { it.size == 7 }?.let { view.slideAreaToImage(it[0], it[1], it[2], it[3], it[4], it[5], it[6]) }
            EventConstant.PG_GET_SLIDE_NOTE -> view.getSldieNote(obj as Int)
            EventConstant.PG_GET_SLIDE_SIZE -> if ((obj as Int) > 0 && obj <= view.getSlideCount()) {
                val d: Dimension = view.getPageSize()!!
                Rectangle(0, 0, d.width.toInt(), d.height.toInt())
            } else null
            EventConstant.PG_SLIDESHOW -> view.isSlideShow()
            EventConstant.APP_PAGE_UP_ID -> view.hasPreviousSlide_Slideshow()
            EventConstant.APP_PAGE_DOWN_ID -> view.hasNextSlide_Slideshow()
            EventConstant.PG_SLIDESHOW_HASPREVIOUSACTION -> view.hasPreviousAction_Slideshow()
            EventConstant.PG_SLIDESHOW_HASNEXTACTION -> view.hasNextAction_Slideshow()
            EventConstant.APP_THUMBNAIL_ID -> (obj as? IntArray)?.takeIf { it.size >= 2 && it[1] > 0 }?.let { view.getThumbnail(it[0], it[1].toFloat() / MainConstant.STANDARD_RATE) }
            EventConstant.PG_SLIDESHOW_SLIDEEXIST -> (obj as Int) <= view.getRealSlideCount()
            EventConstant.PG_SLIDESHOW_ANIMATIONSTEPS -> view.getSlideAnimationSteps(obj as Int)
            EventConstant.PG_SLIDESHOW_SLIDESHOWTOIMAGE -> (obj as? IntArray)?.takeIf { it.size >= 2 && it[1] > 0 }?.let { view.getSlideshowToImage(it[0], it[1]) }
            EventConstant.APP_GET_FIT_SIZE_STATE_ID -> view.getFitSizeState()
            EventConstant.APP_GET_SNAPSHOT_ID -> view.getSnapshot(obj as Bitmap)
            else -> null
        }
    }

    override fun getCurrentViewIndex(): Int = pgView!!.getCurrentIndex() + 1
    fun getTotalSlide(): Int = pgView!!.getRealSlideCount()
    fun getTotalSlideCount(): Int = pgView!!.getSlideCount()
    override fun getView(): View = pgView!!
    override fun getDialog(activity: Activity?, id: Int): Dialog? = null
    override fun getMainFrame(): IMainFrame = mainControl!!.getMainFrame()
    override fun getActivity(): Activity = mainControl!!.getMainFrame().getActivity()
    override fun getFind(): IFind = pgView!!.getFind()!!
    override fun isAutoTest(): Boolean = mainControl?.isAutoTest() ?: false
    override fun getOfficeToPicture(): IOfficeToPicture? = mainControl?.getOfficeToPicture()
    override fun getCustomDialog(): ICustomDialog? = mainControl?.getCustomDialog()
    override fun isSlideShow(): Boolean = pgView?.isSlideShow() ?: false
    override fun getSlideShow(): ISlideShow? = mainControl?.getSlideShow()
    override fun getApplicationType(): Byte = MainConstant.APPLICATION_TYPE_PPT
    override fun getSysKit(): SysKit = mainControl!!.getSysKit()
    override fun isEndFile(): Boolean = pgView!!.getCurrentIndex() == pgView!!.getSlideCount() - 1

    override fun dispose() {
        isDispose = true
        pgView?.dispose()
        pgView = null
        mainControl = null
    }
}
