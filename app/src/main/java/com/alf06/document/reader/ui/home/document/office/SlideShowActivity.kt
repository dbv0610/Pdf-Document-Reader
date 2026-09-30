package com.alf06.document.reader.ui.home.document.office

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.view.Gravity
import android.view.GestureDetector
import android.view.KeyEvent
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import com.wxiwei.office.editor.ui.DialogKit
import androidx.lifecycle.lifecycleScope
import com.alf06.document.reader.databinding.ActivitySlideShowBinding
import com.alf06.document.reader.ui.home.document.office.show.InkView
import com.alf06.document.reader.ui.home.document.office.show.ShowStage
import com.alf06.document.reader.ui.home.document.office.show.SlideFrame
import com.ui.baselib.base.BaseActivity
import com.ui.baselib.extensions.gone
import com.ui.baselib.extensions.visible
import com.wxiwei.office.editor.pptx.SlideEffect
import com.wxiwei.office.editor.pptx.SlideScript
import com.wxiwei.office.pg.view.SlideDrawKit
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

/**
 * The slideshow: slides fill the screen with their transitions, shape animations play tap by tap
 * (see [ShowStage]); links can be tapped; a pen / laser pointer, a slide list and timed advance are
 * in the bar (long press to show it); volume keys and presentation remotes move on and back.
 */
class SlideShowActivity :
    BaseActivity<ActivitySlideShowBinding>(ActivitySlideShowBinding::inflate) {
    override val fullStatus: Boolean = true

    private lateinit var stage: ShowStage
    private lateinit var ink: InkView
    private lateinit var endScreen: TextView
    private lateinit var palette: LinearLayout
    private var hideUiJob: Job? = null
    private var loadJob: Job? = null
    private var autoJob: Job? = null

    /** Effects of one tap; [auto]: they start with the slide. Each with its start in the group (ms). */
    private class Group(val auto: Boolean, val effects: MutableList<Pair<SlideEffect, Long>> = ArrayList())

    private var slide = -1
    private var step = 0
    private var groups: List<Group> = emptyList()
    private var ended = false
    private var startGroupPending = false
    private val cache = LinkedHashMap<Int, SlideDrawKit.Layers>()
    private var autoMs: Long? = null
    private var loop = false

    private val src get() = source!!
    private fun scriptOf(i: Int): SlideScript? = src.script.getOrNull(i)

    override fun backPressed() { finish() }

    @SuppressLint("ClickableViewAccessibility")
    override fun initialize() {
        if (source == null) { finish(); return }
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        stage = ShowStage(this)
        ink = InkView(this)
        endScreen = TextView(this).apply {
            text = "Kết thúc trình chiếu. Chạm để thoát."
            setTextColor(Color.WHITE); textSize = 18f; gravity = Gravity.CENTER; setBackgroundColor(Color.BLACK)
            visibility = View.GONE
            setOnClickListener { finish() }
        }
        binding.stageHost.addView(stage, FrameLayout.LayoutParams(-1, -1))
        binding.stageHost.addView(ink, FrameLayout.LayoutParams(-1, -1))
        binding.stageHost.addView(endScreen, FrameLayout.LayoutParams(-1, -1))
        palette = buildPalette()
        binding.stageHost.addView(palette, FrameLayout.LayoutParams(-2, -2, Gravity.TOP or Gravity.CENTER_HORIZONTAL).apply { topMargin = dp(8) })
        stage.onSettled = {
            if (startGroupPending) {
                startGroupPending = false
                groups.firstOrNull()?.takeIf { it.auto }?.let { step = 1; stage.play(it.effects) }
            } else scheduleAuto()
        }
        stage.addOnLayoutChangeListener { _, l, t, r, b, ol, ot, or, ob -> if (r - l != or - ol || b - t != ob - ot) { placeInk(); if (slide >= 0) reload() } }
        val gestures = GestureDetector(this, object : GestureDetector.SimpleOnGestureListener() {
            override fun onDown(e: MotionEvent) = true
            override fun onSingleTapUp(e: MotionEvent): Boolean { tap(e.x, e.y); return true }
            override fun onLongPress(e: MotionEvent) { toggleUi() }
            override fun onFling(e1: MotionEvent?, e2: MotionEvent, vx: Float, vy: Float): Boolean {
                if (Math.abs(vx) > Math.abs(vy) && Math.abs(vx) > 800) { if (vx < 0) next() else previous(); return true }
                return false
            }
        })
        stage.setOnTouchListener { _, event -> gestures.onTouchEvent(event) }
        addBarButtons()
    }

    override fun ActivitySlideShowBinding.setData() {
        showUi()
        lifecycleScope.launch {
            src.invalidated.collect { page -> cache.remove(page - 1); if (page - 1 == slide) reload() }
        }
        stage.post { go(src.startAt.coerceIn(0, maxOf(0, src.count - 1))) }
    }

    override fun ActivitySlideShowBinding.onClick() {
        icBack.setOnClickListener { backPressed() }
        icNexPage.setOnClickListener { next() }
        icPrevPage.setOnClickListener { previous() }
    }

    // ---- slides and steps ------------------------------------------------------------------

    private fun groupsOf(effects: List<SlideEffect>): List<Group> {
        val out = ArrayList<Group>()
        var cur: Group? = null
        var groupStart = 0L; var groupEnd = 0L
        effects.forEachIndexed { i, e ->
            if (cur == null || e.start == SlideEffect.Start.CLICK) {
                cur = Group(i == 0 && e.start != SlideEffect.Start.CLICK).also { out.add(it) }
                groupStart = 0; groupEnd = 0
            }
            if (e.start == SlideEffect.Start.AFTER) groupStart = groupEnd
            val at = groupStart + e.delayMs
            cur!!.effects.add(e to at)
            groupEnd = maxOf(groupEnd, at + e.durationMs)
        }
        return out
    }

    private fun effects(i: Int) = scriptOf(i)?.effects.orEmpty()

    /** Every animated shape at rest as it is after the first [done] groups. */
    private fun applyState(frame: SlideFrame, done: Int) {
        val all = effects(frame.index)
        val visible = HashMap<Int, Boolean>()
        for (e in all) if (e.shapeId !in visible) visible[e.shapeId] = e.kind != SlideEffect.Kind.ENTRANCE
        for (g in groups.take(done)) for ((e, _) in g.effects) visible[e.shapeId] = e.kind != SlideEffect.Kind.EXIT
        for ((id, v) in visible) frame.rest(id, v)
    }

    private fun fitWidth(): Int {
        val w = stage.width; val h = stage.height
        if (w <= 0 || h <= 0) return 0
        return minOf(w.toFloat(), h * src.aspect).toInt()
    }

    private suspend fun layersOf(i: Int): SlideDrawKit.Layers? {
        cache[i]?.let { return it }
        val width = fitWidth().takeIf { it > 0 } ?: return null
        val animated = effects(i).map { it.shapeId }.toSet()
        var tries = 0
        while (true) {
            val (layers, complete) = src.layers(i + 1, width, animated)
            if (layers != null && (complete || tries >= 6)) {
                cache[i] = layers
                while (cache.size > 3) cache.remove(cache.keys.first { it != i })
                return layers
            }
            if (layers == null && tries >= 20) return null // the slide is still being read
            tries++
            delay(if (layers == null) 300 else 500)
        }
    }

    /** Shows slide [i]: with its transition when moving on, at its last step when coming back. */
    private fun go(i: Int, forward: Boolean = true, atEnd: Boolean = false) {
        if (i !in 0 until src.count) return
        cancelAuto()
        ended = false; endScreen.visibility = View.GONE
        loadJob?.cancel()
        loadJob = lifecycleScope.launch {
            val layers = layersOf(i) ?: run { Toast.makeText(this@SlideShowActivity, "Chưa mở xong slide ${i + 1}", Toast.LENGTH_SHORT).show(); return@launch }
            val frame = SlideFrame(this@SlideShowActivity, i, layers)
            slide = i
            groups = groupsOf(effects(i))
            step = if (atEnd) groups.size else 0
            applyState(frame, step)
            startGroupPending = !atEnd && groups.firstOrNull()?.auto == true
            stage.show(frame, scriptOf(i)?.transition, forward && !atEnd)
            ink.slide = i
            placeInk()
            updateCounter()
            // the next slide ready before it is asked for
            nextVisible(i)?.let { n -> if (n !in cache) launch { layersOf(n) } }
        }
    }

    /** The same slide drawn again (new size, or pictures loaded), at the same step. */
    private fun reload() {
        val i = slide.takeIf { it >= 0 } ?: return
        cache.remove(i)
        val done = step
        loadJob?.cancel()
        loadJob = lifecycleScope.launch {
            val layers = layersOf(i) ?: return@launch
            val frame = SlideFrame(this@SlideShowActivity, i, layers)
            applyState(frame, done)
            stage.show(frame, null, false)
            placeInk()
        }
    }

    private fun nextVisible(from: Int): Int? = (from + 1 until src.count).firstOrNull { scriptOf(it)?.hidden != true }
    private fun previousVisible(from: Int): Int? = (from - 1 downTo 0).firstOrNull { scriptOf(it)?.hidden != true }

    private fun next() {
        if (ended) { finish(); return }
        if (stage.isAnimating) { stage.finishAll(); return }
        if (slide < 0) return
        if (step < groups.size) { stage.play(groups[step].effects); step++; return }
        val n = nextVisible(slide)
        when {
            n != null -> go(n)
            loop -> (0 until src.count).firstOrNull { scriptOf(it)?.hidden != true }?.let { go(it) }
            else -> showEnd()
        }
    }

    private fun previous() {
        if (ended) { ended = false; endScreen.visibility = View.GONE; return }
        if (stage.isAnimating) stage.finishAll()
        val first = if (groups.firstOrNull()?.auto == true) 1 else 0
        if (step > first) { step--; stage.current?.let { applyState(it, step) }; return }
        previousVisible(slide)?.let { go(it, forward = false, atEnd = true) }
    }

    private fun showEnd() {
        cancelAuto()
        ended = true
        endScreen.visibility = View.VISIBLE
    }

    // ---- taps and links --------------------------------------------------------------------

    private fun tap(x: Float, y: Float) {
        val frame = stage.current
        if (frame != null && !ended) {
            val left = (stage.width - frame.slideWidth) / 2f
            val top = (stage.height - frame.slideHeight) / 2f
            val emuX = ((x - left) / frame.layers.zoom * EMU_PER_PX).toLong()
            val emuY = ((y - top) / frame.layers.zoom * EMU_PER_PX).toLong()
            val link = scriptOf(frame.index)?.links?.lastOrNull { l ->
                emuX in l.rectEmu.x..(l.rectEmu.x + l.rectEmu.width) && emuY in l.rectEmu.y..(l.rectEmu.y + l.rectEmu.height)
            }
            if (link != null) {
                when {
                    link.url != null -> try {
                        startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(link.url)))
                    } catch (e: Exception) {
                        Toast.makeText(this, "Không mở được liên kết", Toast.LENGTH_SHORT).show()
                    }
                    link.slideIndex != null -> go(link.slideIndex!!)
                    link.jump == "next" -> next()
                    link.jump == "previous" -> previousVisible(slide)?.let { go(it) }
                    link.jump == "first" -> go(0)
                    link.jump == "last" -> go(src.count - 1)
                    link.jump == "end" -> showEnd()
                }
                return
            }
        }
        if (x < stage.width * 0.25f) previous() else next()
    }

    /** Keys before the views: arrows and page keys would otherwise move the focus between buttons. */
    override fun dispatchKeyEvent(event: KeyEvent): Boolean {
        val action = when (event.keyCode) {
            KeyEvent.KEYCODE_VOLUME_DOWN, KeyEvent.KEYCODE_PAGE_DOWN, KeyEvent.KEYCODE_DPAD_RIGHT, KeyEvent.KEYCODE_DPAD_DOWN,
            KeyEvent.KEYCODE_SPACE, KeyEvent.KEYCODE_ENTER, KeyEvent.KEYCODE_NUMPAD_ENTER, KeyEvent.KEYCODE_MEDIA_NEXT, KeyEvent.KEYCODE_N -> ::next
            KeyEvent.KEYCODE_VOLUME_UP, KeyEvent.KEYCODE_PAGE_UP, KeyEvent.KEYCODE_DPAD_LEFT, KeyEvent.KEYCODE_DPAD_UP,
            KeyEvent.KEYCODE_DEL, KeyEvent.KEYCODE_MEDIA_PREVIOUS, KeyEvent.KEYCODE_P -> ::previous
            KeyEvent.KEYCODE_ESCAPE -> ::finish
            else -> return super.dispatchKeyEvent(event)
        }
        // on the key's release: a press is sometimes not delivered (focus moving between windows)
        if (event.action == KeyEvent.ACTION_UP && !event.isCanceled) action()
        return true
    }

    // ---- timed advance ---------------------------------------------------------------------

    private fun cancelAuto() { autoJob?.cancel(); autoJob = null }

    /** When nothing moves: the next step after the chosen time, or the slide's own timing. */
    private fun scheduleAuto() {
        cancelAuto()
        if (ended || slide < 0) return
        val ms = autoMs ?: scriptOf(slide)?.transition?.advanceAfterMs?.toLong() ?: return
        autoJob = lifecycleScope.launch { delay(ms); next() }
    }

    private fun pickAuto() {
        val choices = listOf("Tắt" to (null to false), "3 giây" to (3000L to false), "5 giây" to (5000L to false), "10 giây" to (10000L to false),
            "20 giây" to (20000L to false), "5 giây, lặp lại" to (5000L to true), "10 giây, lặp lại" to (10000L to true))
        DialogKit(this).pick("Tự chuyển", choices.map { it.first }) { i ->
            autoMs = choices[i].second.first; loop = choices[i].second.second
            autoButton.text = if (autoMs == null) "⏱ Tự chuyển" else "⏱ " + choices[i].first
            scheduleAuto()
        }
    }

    // ---- slide list ------------------------------------------------------------------------

    private fun pickSlide() {
        val labels = (0 until src.count).map { i ->
            val s = scriptOf(i)
            "${i + 1}. " + (s?.title?.takeIf { it.isNotBlank() } ?: "Slide ${i + 1}") + if (s?.hidden == true) " (ẩn)" else ""
        }
        DialogKit(this).pick("Danh sách slide", labels, "Đóng") { i -> go(i) }
    }

    // ---- pen and laser ---------------------------------------------------------------------

    private fun placeInk() {
        val frame = stage.current ?: return
        val left = (stage.width - frame.slideWidth) / 2f
        val top = (stage.height - frame.slideHeight) / 2f
        ink.area.set(left, top, left + frame.slideWidth, top + frame.slideHeight)
        ink.invalidate()
    }

    private fun setInk(mode: InkView.Mode) {
        ink.mode = mode
        palette.visibility = if (mode == InkView.Mode.NONE) View.GONE else View.VISIBLE
        for (i in 0 until palette.childCount) {
            val v = palette.getChildAt(i)
            // colors and the eraser only mean something for the pen
            if (v.tag == "pen") v.visibility = if (mode == InkView.Mode.LASER) View.GONE else View.VISIBLE
        }
        if (mode != InkView.Mode.NONE) { hideUiJob?.cancel(); binding.lnUiState.gone() }
    }

    private fun buildPalette(): LinearLayout = LinearLayout(this).apply {
        visibility = View.GONE
        setBackgroundColor(0xB0000000.toInt())
        setPadding(dp(6), dp(4), dp(6), dp(4))
        gravity = Gravity.CENTER_VERTICAL
        for (c in listOf(Color.RED, 0xFFFFC000.toInt(), 0xFF00B050.toInt(), 0xFF0070C0.toInt(), Color.WHITE, Color.BLACK)) {
            addView(View(context).apply {
                tag = "pen"
                contentDescription = "Bút màu #%06X".format(c and 0xFFFFFF)
                background = android.graphics.drawable.GradientDrawable().apply { shape = android.graphics.drawable.GradientDrawable.OVAL; setColor(c); setStroke(dp(2), Color.WHITE) }
                setOnClickListener { ink.color = c; ink.mode = InkView.Mode.PEN }
            }, LinearLayout.LayoutParams(dp(26), dp(26)).apply { setMargins(dp(4), 0, dp(4), 0) })
        }
        fun text(label: String, tag: String?, run: () -> Unit) = addView(TextView(context).apply {
            text = label; this.tag = tag; setTextColor(Color.WHITE); textSize = 14f; setPadding(dp(10), dp(6), dp(10), dp(6)); setOnClickListener { run() }
        })
        text("Tẩy", "pen") { ink.mode = InkView.Mode.ERASER }
        text("Xóa hết", "pen") { ink.clear() }
        text("Xong", null) { setInk(InkView.Mode.NONE) }
    }

    // ---- bar -------------------------------------------------------------------------------

    private lateinit var autoButton: TextView

    private fun addBarButtons() {
        val bar = binding.bottomBar
        fun button(label: String, run: () -> Unit) = TextView(this).apply {
            text = label; setTextColor(Color.WHITE); textSize = 13f
            setPadding(dp(10), dp(10), dp(10), dp(10))
            setOnClickListener { run(); showUi() }
        }
        val items = listOf(
            button("☰ Danh sách") { pickSlide() },
            button("✎ Bút") { setInk(InkView.Mode.PEN) },
            button("● Laser") { setInk(InkView.Mode.LASER) },
            button("⏱ Tự chuyển") { pickAuto() }.also { autoButton = it },
        )
        items.forEachIndexed { i, v -> bar.addView(v, i) }
    }

    private fun toggleUi() { if (binding.lnUiState.visibility == View.VISIBLE) binding.lnUiState.gone() else showUi() }

    private fun showUi() {
        binding.lnUiState.visible()
        hideUiJob?.cancel()
        hideUiJob = lifecycleScope.launch {
            delay(UI_VISIBLE_DURATION)
            binding.lnUiState.gone()
        }
    }

    @SuppressLint("SetTextI18n")
    private fun updateCounter() { binding.tvPageCurrent.text = "${slide + 1}/${src.count}" }

    private fun dp(v: Int) = Math.round(v * resources.displayMetrics.density)

    // for tests: what is shown
    internal val shownSlide get() = slide
    internal val shownStep get() = step
    internal val isEnded get() = ended
    internal val stageView get() = stage
    internal val inkView get() = ink

    override fun onDestroy() {
        hideUiJob?.cancel(); loadJob?.cancel(); cancelAuto()
        if (isFinishing) source = null
        super.onDestroy()
    }

    companion object {
        private const val UI_VISIBLE_DURATION = 3_000L
        private const val EMU_PER_PX = 9525f

        var source: Source? = null
    }

    /**
     * What to show: [count] slides of width/height [aspect], the show [script] of each (empty for
     * .ppt), [layers] draws a slide (1-based) for the stage; [startAt] 0-based.
     */
    class Source(
        val count: Int,
        val aspect: Float,
        val startAt: Int,
        val script: List<SlideScript>,
        val layers: suspend (page: Int, width: Int, animated: Set<Int>) -> Pair<SlideDrawKit.Layers?, Boolean>,
        val invalidated: Flow<Int>,
    )
}
