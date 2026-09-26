package com.alf06.document.reader.ui.home.document.office

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.graphics.Color
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.ViewGroup
import android.widget.ImageView
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.alf06.document.reader.databinding.ActivitySlideShowBinding
import com.ui.baselib.base.BaseActivity
import com.ui.baselib.extensions.gone
import com.ui.baselib.extensions.visible
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class SlideShowActivity :
    BaseActivity<ActivitySlideShowBinding>(ActivitySlideShowBinding::inflate) {
    override val fullStatus: Boolean = true

    private val adapter by lazy { SlideAdapter(lifecycleScope, source) }
    private var hideUiJob: Job? = null

    override fun backPressed() {
        finish()
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun initialize() {
        if (source == null) {
            finish()
            return
        }
        binding.viewPager2.adapter = adapter
        binding.viewPager2.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                updatePageIndicator(position)
            }
        })
        updatePageIndicator(0)

        val gestureDetector = GestureDetector(this, object : GestureDetector.SimpleOnGestureListener() {
            override fun onDoubleTap(e: MotionEvent): Boolean {
                showUiControls()
                return true
            }
        })
        binding.viewPager2.getChildAt(0).setOnTouchListener { _, event ->
            gestureDetector.onTouchEvent(event)
            false
        }
    }

    override fun ActivitySlideShowBinding.setData() {
        showUiControls()
        source?.let { source ->
            lifecycleScope.launch {
                source.invalidated.collect { page ->
                    if (page - 1 < adapter.itemCount) adapter.notifyItemChanged(page - 1)
                }
            }
        }
    }

    override fun ActivitySlideShowBinding.onClick() {
        icBack.setOnClickListener { backPressed() }
        icNexPage.setOnClickListener {
            val next = viewPager2.currentItem + 1
            if (next < adapter.itemCount) viewPager2.currentItem = next
        }
        icPrevPage.setOnClickListener {
            val prev = viewPager2.currentItem - 1
            if (prev >= 0) viewPager2.currentItem = prev
        }
    }

    private fun showUiControls() {
        binding.lnUiState.visible()
        hideUiJob?.cancel()
        hideUiJob = lifecycleScope.launch {
            delay(UI_VISIBLE_DURATION)
            binding.lnUiState.gone()
        }
    }

    @SuppressLint("SetTextI18n")
    private fun updatePageIndicator(position: Int) {
        binding.tvPageCurrent.text = "${position + 1}/${adapter.itemCount}"
    }

    override fun onDestroy() {
        hideUiJob?.cancel()
        if (isFinishing) source = null
        super.onDestroy()
    }

    private class SlideAdapter(
        private val scope: CoroutineScope,
        private val source: Source?,
    ) : RecyclerView.Adapter<SlideAdapter.SlideViewHolder>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SlideViewHolder {
            val imageView = ImageView(parent.context).apply {
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
                scaleType = ImageView.ScaleType.FIT_CENTER
                setBackgroundColor(Color.WHITE)
            }
            return SlideViewHolder(imageView)
        }

        override fun onBindViewHolder(holder: SlideViewHolder, position: Int) {
            holder.job?.cancel()
            holder.imageView.setImageBitmap(null)
            val source = source ?: return
            holder.job = scope.launch {
                source.preview(position + 1)?.let(holder.imageView::setImageBitmap)
                source.load(position + 1)?.let(holder.imageView::setImageBitmap)
            }
        }

        override fun onViewRecycled(holder: SlideViewHolder) {
            holder.job?.cancel()
            holder.imageView.setImageBitmap(null)
        }

        override fun getItemCount(): Int = source?.count ?: 0

        class SlideViewHolder(val imageView: ImageView) : RecyclerView.ViewHolder(imageView) {
            var job: Job? = null
        }
    }

    companion object {
        private const val UI_VISIBLE_DURATION = 2_000L

        var source: Source? = null
    }

    class Source(
        val count: Int,
        val preview: suspend (Int) -> Bitmap?,
        val load: suspend (Int) -> Bitmap?,
        val invalidated: Flow<Int>,
    )
}
