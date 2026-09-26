package com.wxiwei.office.pg.control

import android.graphics.Bitmap
import android.graphics.Canvas
import android.widget.ProgressBar
import com.wxiwei.office.pg.model.PGModel
import com.wxiwei.office.pg.view.SlideDrawKit
import com.wxiwei.office.system.IControl
import com.wxiwei.office.system.beans.pagelist.APageListItem
import com.wxiwei.office.system.beans.pagelist.APageListView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class PGPageListItem(
    listView: APageListView,
    control: IControl,
    private var editor: PGEditor?,
    pageWidth: Int,
    pageHeight: Int
) : APageListItem(listView, pageWidth, pageHeight) {
    companion object { const val BUSY_SIZE = 60 }
    private val backgroundColor = 0xFFFFFFFF.toInt()
    private var mBusyIndicator: ProgressBar? = null
    private var pgModel: PGModel? = listView.getModel() as PGModel?
    private val loadScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private var waitForSlideJob: Job? = null

    init {
        this.control = requireNotNull(control) { "PGPageListItem requires a control" }
        // APageListItem is a ViewGroup; explicitly allow its custom onDraw().
        setWillNotDraw(false)
        setBackgroundColor(backgroundColor)
    }

    override fun onDraw(canvas: Canvas) {
        val model = pgModel ?: return
        val slide = model.getSlide(pageIndex) ?: return
        SlideDrawKit.instance().drawSlide(canvas, model, editor, slide, listView.getZoom())
    }

    override fun setPageItemRawData(pIndex: Int, pageWidth: Int, pageHeight: Int) {
        super.setPageItemRawData(pIndex, pageWidth, pageHeight)
        val model = pgModel ?: return
        if (pageIndex >= model.getRealSlideCount()) {
            waitForSlideJob?.cancel()
            if (mBusyIndicator == null) {
                mBusyIndicator = ProgressBar(context).apply {
                    isIndeterminate = true
                    setBackgroundResource(android.R.drawable.progress_horizontal)
                }
                addView(mBusyIndicator)
            }
            mBusyIndicator?.visibility = VISIBLE
            waitForSlideJob = loadScope.launch {
                while (currentCoroutineContext().isActive && pgModel != null &&
                    pageIndex >= (pgModel?.getRealSlideCount() ?: 0)
                ) {
                    delay(100)
                }
                if (!currentCoroutineContext().isActive) return@launch
                withContext(Dispatchers.Main.immediate) {
                    mBusyIndicator?.visibility = INVISIBLE
                    postInvalidate()
                    if (pageIndex == listView.getCurrentPageNumber() - 1) {
                        listView.exportImage(listView.getCurrentPageView(), null)
                    }
                    isInit = false
                }
            }
        } else {
            if ((listView.getZoom() * 100).toInt() == 100 || (isInit && pIndex == 0)) listView.exportImage(this, null)
            isInit = false
            mBusyIndicator?.visibility = INVISIBLE
        }
    }

    override fun releaseResources() {
        super.releaseResources()
        val model = pgModel ?: return
        SlideDrawKit.instance().disposeOldSlideView(model, model.getSlide(pageIndex))
    }

    override fun blank(pIndex: Int) { super.blank(pIndex) }
    public override fun addRepaintImageView(bmp: Bitmap?) { postInvalidate(); listView.exportImage(this, bmp) }
    override fun removeRepaintImageView() {}

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
        val indicator = mBusyIndicator ?: return
        val w = right - left; val h = bottom - top
        val x = if (w > listView.getWidth()) (listView.getWidth() - BUSY_SIZE) / 2 - left else (w - BUSY_SIZE) / 2
        val y = if (h > listView.getHeight()) (listView.getHeight() - BUSY_SIZE) / 2 - top else (h - BUSY_SIZE) / 2
        indicator.layout(x, y, x + BUSY_SIZE, y + BUSY_SIZE)
    }

    override fun dispose() {
        waitForSlideJob?.cancel()
        loadScope.coroutineContext[Job]?.cancel()
        super.dispose(); pgModel = null; editor = null
    }
}
