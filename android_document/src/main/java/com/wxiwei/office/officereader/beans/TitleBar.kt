package com.wxiwei.office.officereader.beans

import android.content.Context
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Paint
import android.widget.LinearLayout
import android.widget.ProgressBar
import com.wxiwei.office.constant.MainConstant
import kotlin.math.min

/**
 * TODO: app title bar view
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
 * 日期:            2012-11-14
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
class TitleBar(context: Context?) : LinearLayout(context) {
    fun setTitle(title: String?) {
        this.title = title
        this.postInvalidate()
    }

    /**
     * 
     * 
     */
    override fun onDraw(canvas: Canvas) {
        if (title != null) {
            canvas.drawText(title!!, MainConstant.GAP.toFloat(), yPostion, paint!!)
        }
    }


    fun showProgressBar(visible: Boolean) {
        mBusyIndicator!!.setVisibility(if (visible) VISIBLE else GONE)
    }

    /**
     * 
     * 
     */
    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        if (mBusyIndicator != null) {
            val limit = min(this.getWidth(), getHeight()) / 2
            mBusyIndicator!!.measure(
                MeasureSpec.AT_MOST
                        or limit, MeasureSpec.AT_MOST or limit
            )
        }
    }

    /**
     * 
     * @see android.view.ViewGroup.onLayout
     */
    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)

        val w = right - left
        val h = bottom - top
        if (mBusyIndicator != null) {
            val bw = mBusyIndicator!!.getMeasuredWidth()
            val bh = mBusyIndicator!!.getMeasuredHeight()

            mBusyIndicator!!.layout(
                w - bw - MainConstant.GAP,
                (h - bh) / 2,
                w - MainConstant.GAP,
                (h + bh) / 2
            )
        }
    }

    /**
     * 
     */
    fun dispose() {
        paint = null
        title = null

        if (mBusyIndicator != null) {
            removeView(mBusyIndicator)
            mBusyIndicator = null
        }
    }

    private var title: String? = null
    val titleHeight: Int = 0
    private var paint: Paint? = null
    private val yPostion = 0f

    //
    private var mBusyIndicator: ProgressBar? = null

    init {
        val opts = BitmapFactory.Options()
        opts.inJustDecodeBounds = true
        //        BitmapFactory.decodeResource(getResources(),  R.drawable.sys_title_bg_vertical, opts);
//        height = opts.outHeight;
//
//        setBackgroundResource(R.drawable.sys_title_bg_vertical);
//
//        paint = new Paint();
//        paint.setAntiAlias(true);
//        paint.setColor(Color.WHITE);
//        paint.setTextSize(24);
//        FontMetrics fm = paint.getFontMetrics();
//
//        yPostion = (height - fm.descent + fm.ascent) / 2  -  fm.ascent;
//
//        mBusyIndicator = new ProgressBar(getContext());
//        mBusyIndicator.setIndeterminate(true);
//        //mBusyIndicator.setBackgroundResource(R.drawable.busy);
//        addView(mBusyIndicator);
//        mBusyIndicator.setVisibility(GONE);
    }
}
