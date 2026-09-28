package com.alf06.document.reader.ui.home.document.pdf.tools

import android.content.Context
import android.content.Intent
import android.graphics.Typeface
import android.util.TypedValue
import android.view.ViewGroup
import android.widget.TextView
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.alf06.document.reader.R
import com.alf06.document.reader.databinding.ActivityPdfReflowBinding
import com.reader.pdfviewer.pdfium.PdfPageText
import com.reader.pdfviewer.tools.PdfSource
import com.reader.pdfviewer.tools.PdfTools
import com.ui.baselib.base.BaseActivity
import com.ui.baselib.extensions.click
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import java.io.File
import kotlin.math.abs

/**
 * The text of a PDF as flowing paragraphs, for reading on a small screen: the text size can be
 * changed and the colors follow the reading theme. Scanned pages are read with OCR. Pictures,
 * tables and columns are not kept: this is a text view of the document.
 */
class PdfReflowActivity : BaseActivity<ActivityPdfReflowBinding>(ActivityPdfReflowBinding::inflate) {

    private val tools: PdfTools by inject()
    private val prefs by lazy { PdfReadingPrefs(this) }

    /** A page title (null [text] for the page heading) or a paragraph. */
    private class Block(val page: Int, val text: String?)

    private val blocks = ArrayList<Block>()
    private var sizeSp = 17f
    private var startPage = 0
    private var scrolledToStart = false

    private val adapter = object : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
        override fun getItemCount() = blocks.size
        override fun getItemViewType(position: Int) = if (blocks[position].text == null) 1 else 0
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = object : RecyclerView.ViewHolder(TextView(parent.context).apply {
            layoutParams = RecyclerView.LayoutParams(-1, -2)
            setTextIsSelectable(viewType == 0)
            setLineSpacing(0f, 1.35f)
        }) {}
        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            val b = blocks[position]
            val view = holder.itemView as TextView
            val (fg, secondary) = colors()
            if (b.text == null) {
                view.text = getString(R.string.pdf_reflow_page, b.page + 1)
                view.setTextColor(secondary)
                view.setTextSize(TypedValue.COMPLEX_UNIT_SP, sizeSp * 0.7f)
                view.typeface = Typeface.DEFAULT_BOLD
                view.setPadding(0, dp(18), 0, dp(6))
            } else {
                view.text = b.text
                view.setTextColor(fg)
                view.setTextSize(TypedValue.COMPLEX_UNIT_SP, sizeSp)
                view.typeface = Typeface.SERIF
                view.setPadding(0, 0, 0, dp(12))
            }
        }
    }

    private fun dp(v: Int) = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, v.toFloat(), resources.displayMetrics).toInt()

    override fun initialize() {
        val file = File(intent.getStringExtra(ARG_PATH) ?: return finish())
        startPage = intent.getIntExtra(ARG_PAGE, 0)
        sizeSp = getSharedPreferences("pdf_reading", MODE_PRIVATE).getFloat("reflow_size", 17f)
        binding.txtTitle.text = file.name
        binding.rcvText.layoutManager = LinearLayoutManager(this)
        binding.rcvText.adapter = adapter
        applyTheme()
        val source = PdfSource(file, intent.getStringExtra(ARG_PASSWORD))
        binding.txtStatus.text = getString(R.string.pdf_tool_working)
        lifecycleScope.launch {
            try {
                var pages = 0
                tools.readText(source, allowOcr = true) { text ->
                    // called on the tools thread
                    val paragraphs = paragraphsOf(text)
                    kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) { show(text, paragraphs, ++pages) }
                }
                binding.txtStatus.text = if (blocks.isEmpty()) getString(R.string.pdf_reflow_no_text) else ""
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                binding.txtStatus.text = getString(R.string.pdf_tool_failed, e.message ?: "")
            }
        }
    }

    /** One page read: its paragraphs go at the end; the page the reader was on is scrolled to. */
    private fun show(text: PdfPageText, paragraphs: List<String>, pages: Int) {
        binding.txtStatus.text = getString(R.string.pdf_reflow_reading, pages)
        if (paragraphs.isEmpty()) return
        val first = blocks.size
        blocks += Block(text.pageIndex, null)
        paragraphs.forEach { blocks += Block(text.pageIndex, it) }
        adapter.notifyItemRangeInserted(first, blocks.size - first)
        if (!scrolledToStart && text.pageIndex >= startPage) {
            scrolledToStart = true
            (binding.rcvText.layoutManager as LinearLayoutManager).scrollToPositionWithOffset(first, 0)
        }
    }

    override fun ActivityPdfReflowBinding.setData() {}

    override fun backPressed() { finish() }

    override fun ActivityPdfReflowBinding.onClick() {
        icBack.click { finish() }
        btnSmaller.click { resize(-1.5f) }
        btnLarger.click { resize(1.5f) }
        btnTheme.click {
            prefs.theme = (prefs.theme + 1) % 3
            applyTheme()
        }
    }

    private fun resize(by: Float) {
        sizeSp = (sizeSp + by).coerceIn(11f, 34f)
        getSharedPreferences("pdf_reading", MODE_PRIVATE).edit().putFloat("reflow_size", sizeSp).apply()
        adapter.notifyDataSetChanged()
    }

    /** Text and secondary text colors for the theme. */
    private fun colors(): Pair<Int, Int> = when (prefs.theme) {
        PdfReadingPrefs.THEME_NIGHT -> 0xFFDDDDDD.toInt() to 0xFF9E9E9E.toInt()
        PdfReadingPrefs.THEME_SEPIA -> 0xFF4B3B2A.toInt() to 0xFF8A7560.toInt()
        else -> 0xFF1F1F1F.toInt() to 0xFF7C8A97.toInt()
    }

    private fun applyTheme() {
        val bg = when (prefs.theme) {
            PdfReadingPrefs.THEME_NIGHT -> 0xFF121212.toInt()
            PdfReadingPrefs.THEME_SEPIA -> 0xFFF4ECD8.toInt()
            else -> 0xFFFFFFFF.toInt()
        }
        binding.rcvText.setBackgroundColor(bg)
        adapter.notifyDataSetChanged()
    }

    companion object {
        private const val ARG_PATH = "path"
        private const val ARG_PASSWORD = "password"
        private const val ARG_PAGE = "page"

        fun intent(context: Context, file: File, password: String?, page: Int) = Intent(context, PdfReflowActivity::class.java)
            .putExtra(ARG_PATH, file.path).putExtra(ARG_PASSWORD, password).putExtra(ARG_PAGE, page)

        /**
         * The lines of a page joined into paragraphs: a new one after a wider gap than between
         * lines, or after a short line that ends a sentence. Words cut with a hyphen are joined.
         */
        internal fun paragraphsOf(text: PdfPageText): List<String> {
            val lines = text.lines.filter { it.text.isNotBlank() }
            if (lines.isEmpty()) return emptyList()
            val widest = lines.maxOf { it.bounds.width() }
            val gaps = lines.zipWithNext { a, b -> b.bounds.top - a.bounds.bottom }.filter { it >= 0 }.sorted()
            val usualGap = gaps.getOrNull(gaps.size / 2) ?: 0f
            val result = ArrayList<String>()
            val current = StringBuilder()
            lines.forEachIndexed { i, line ->
                val prev = lines.getOrNull(i - 1)
                if (prev != null && current.isNotEmpty()) {
                    val gap = line.bounds.top - prev.bounds.bottom
                    val height = abs(prev.bounds.height()).coerceAtLeast(1f)
                    val shortEnd = prev.bounds.width() < widest * 0.7f && prev.text.trimEnd().lastOrNull()?.let { it in ".!?:…\"”)" } == true
                    if (gap > usualGap + height * 0.6f || gap < -height * 2 || shortEnd) {
                        result += current.toString().trim(); current.clear()
                    }
                }
                val t = line.text.trim()
                if (current.endsWith("-") && t.firstOrNull()?.isLowerCase() == true) current.setLength(current.length - 1)
                else if (current.isNotEmpty()) current.append(' ')
                current.append(t)
            }
            if (current.isNotBlank()) result += current.toString().trim()
            return result
        }
    }
}
