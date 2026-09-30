package com.alf06.document.reader.ui.home.document.pdf.tools

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.net.Uri
import android.util.TypedValue
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.alf06.document.reader.R
import com.alf06.document.reader.databinding.ActivityPdfOrganizeBinding
import com.wxiwei.office.editor.ui.DialogKit
import com.alf06.document.reader.ui.home.document.pdf.PdfThumbnailLoader
import com.reader.pdfviewer.pdfium.PdfPasswordException
import com.reader.pdfviewer.tools.PdfSource
import com.reader.pdfviewer.tools.PdfTools
import com.ui.baselib.base.BaseActivity
import com.ui.baselib.extensions.click
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import java.io.File

/**
 * Organizing the pages of a PDF: drag to reorder (long press), turn, copy or delete a page, add
 * empty pages or the pages of another PDF, then save over the file or as a new one. Returns
 * RESULT_OK when the opened file itself changed.
 */
class PdfOrganizeActivity : BaseActivity<ActivityPdfOrganizeBinding>(ActivityPdfOrganizeBinding::inflate) {

    private val tools: PdfTools by inject()

    /** One page of the result: page [page] of source [source] (or an empty page when [source] < 0), turned [turns] quarters. */
    private data class Item(val id: Long, val source: Int, val page: Int, var turns: Int = 0)

    private val sources = ArrayList<PdfSource>()
    private val loaders = ArrayList<PdfThumbnailLoader>()
    private val items = ArrayList<Item>()
    private var nextId = 1L
    private var changed = false
    private lateinit var file: File
    private var password: String? = null

    private val adapter = PageAdapter()

    private val pickPdf = registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri -> uri?.let { addFile(it) } }

    override fun initialize() {
        file = File(intent.getStringExtra(ARG_PATH) ?: return finish())
        password = intent.getStringExtra(ARG_PASSWORD)
        binding.txtTitle.text = getString(R.string.pdf_organize) + " · " + file.name
        binding.rcvPages.layoutManager = GridLayoutManager(this, if (resources.configuration.screenWidthDp >= 600) 5 else 3)
        binding.rcvPages.adapter = adapter
        touchHelper.attachToRecyclerView(binding.rcvPages)
        addSource(PdfSource(file, password)) { count -> (0 until count).forEach { items += Item(nextId++, 0, it) }; adapter.notifyDataSetChanged() }
    }

    override fun ActivityPdfOrganizeBinding.setData() {}

    override fun ActivityPdfOrganizeBinding.onClick() {
        icBack.click { backPressed() }
        btnSave.click { askSave() }
        lnActions.addView(action(getString(R.string.pdf_organize_add_blank)) {
            items += Item(nextId++, -1, 0); changed = true
            adapter.notifyItemInserted(items.lastIndex)
            rcvPages.scrollToPosition(items.lastIndex)
        })
        lnActions.addView(action(getString(R.string.pdf_organize_add_file)) { pickPdf.launch(arrayOf("application/pdf")) })
        lnActions.addView(action(getString(R.string.pdf_organize_rotate_all)) {
            items.forEach { it.turns = (it.turns + 1) % 4 }; changed = true
            adapter.notifyDataSetChanged()
        })
        lnActions.addView(action(getString(R.string.pdf_organize_reverse)) {
            items.reverse(); changed = true
            adapter.notifyDataSetChanged()
        })
    }

    private fun action(label: String, run: () -> Unit) = TextView(this).apply {
        text = label
        setTextColor(ContextCompat.getColor(context, R.color.text_primary))
        setTextSize(TypedValue.COMPLEX_UNIT_SP, 13f)
        val pad = dp(10)
        setPadding(pad, dp(6), pad, dp(6))
        background = GradientDrawable().apply { cornerRadius = dp(16).toFloat(); setColor(0xFFF1F1F4.toInt()) }
        layoutParams = LinearLayout.LayoutParams(-2, -2).apply { marginEnd = dp(6) }
        setOnClickListener { run() }
    }

    private fun dp(v: Int) = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, v.toFloat(), resources.displayMetrics).toInt()

    /** Opens [source] for its thumbnails and page count; [then] gets the count. */
    private fun addSource(source: PdfSource, then: (Int) -> Unit) {
        lifecycleScope.launch {
            val count = try { tools.pageCount(source) } catch (e: Exception) {
                if (generateSequence<Throwable>(e) { it.cause }.any { it is PdfPasswordException }) askPassword(source, then)
                else Toast.makeText(this@PdfOrganizeActivity, R.string.some_errors_occurred_please_try_again, Toast.LENGTH_SHORT).show()
                return@launch
            }
            sources += source
            loaders += PdfThumbnailLoader(this@PdfOrganizeActivity, source.file, source.password, THUMB_WIDTH)
            then(count)
        }
    }

    private fun askPassword(source: PdfSource, then: (Int) -> Unit) {
        DialogKit(this).show(getString(R.string.pdf_password_required)) {
            text(source.file.name)
            val field = input(getString(R.string.pdf_password)).apply {
                inputType = android.text.InputType.TYPE_CLASS_TEXT or android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD
            }
            positive(getString(android.R.string.ok)) { addSource(source.copy(password = field.text.toString()), then) }
            negative(getString(android.R.string.cancel))
        }
    }

    /** Pages of another PDF (copied into the cache, a picked file may not stay readable), added at the end. */
    private fun addFile(uri: Uri) {
        lifecycleScope.launch {
            val copy = File(cacheDir, "organize-${System.nanoTime()}.pdf")
            val ok = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                try { contentResolver.openInputStream(uri)!!.use { input -> copy.outputStream().use { input.copyTo(it) } }; true } catch (e: Exception) { false }
            }
            if (!ok) return@launch Toast.makeText(this@PdfOrganizeActivity, R.string.some_errors_occurred_please_try_again, Toast.LENGTH_SHORT).show()
            addSource(PdfSource(copy)) { count ->
                val index = sources.lastIndex
                val first = items.size
                (0 until count).forEach { items += Item(nextId++, index, it) }
                changed = true
                adapter.notifyItemRangeInserted(first, count)
                binding.rcvPages.scrollToPosition(first)
            }
        }
    }

    private fun askSave() {
        if (items.isEmpty()) return Toast.makeText(this, R.string.pdf_organize_empty, Toast.LENGTH_SHORT).show()
        val choices = listOf(getString(R.string.pdf_organize_save_over), getString(R.string.pdf_organize_save_new))
        DialogKit(this).pick(getString(R.string.save), choices, getString(android.R.string.cancel)) { i ->
            if (i == 0) saveOver() else saveNew()
        }
    }

    private fun refs() = items.map {
        if (it.source < 0) PdfTools.PageRef(-1, 0, it.turns) else PdfTools.PageRef(it.source, it.page, it.turns)
    }

    /** The file itself is replaced once the new one is complete; the reader opens it again. */
    private fun saveOver() {
        val refs = refs()
        val temp = File(file.parentFile, ".${file.name}.organize")
        PdfTaskRunner(this).start(getString(R.string.pdf_organize), temp, onSuccess = {
            setResult(Activity.RESULT_OK)
            if (password != null) Toast.makeText(this, R.string.pdf_organize_unprotected, Toast.LENGTH_LONG).show()
            finish()
        }) { output, _ ->
            tools.organize(sources, refs, output)
            if (!output.renameTo(file)) {
                output.copyTo(file, overwrite = true)
                output.delete()
            }
            null
        }
    }

    private fun saveNew() {
        val refs = refs()
        PdfTaskRunner(this).run(getString(R.string.pdf_organize), file.nameWithoutExtension, "organized") { output, _ ->
            tools.organize(sources, refs, output)
            changed = false
            null
        }
    }

    override fun backPressed() {
        if (!changed) return finish()
        DialogKit(this).confirm(getString(R.string.pdf_organize), getString(R.string.pdf_unsaved_leave), getString(R.string.pdf_discard), getString(android.R.string.cancel)) { finish() }
    }

    override fun onDestroy() {
        loaders.forEach { it.close() }
        sources.drop(1).forEach { if (it.file.path.startsWith(cacheDir.path)) it.file.delete() }
        super.onDestroy()
    }

    // ---- the grid ----

    private val touchHelper = ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(
        ItemTouchHelper.UP or ItemTouchHelper.DOWN or ItemTouchHelper.START or ItemTouchHelper.END, 0) {
        override fun onMove(rv: RecyclerView, from: RecyclerView.ViewHolder, to: RecyclerView.ViewHolder): Boolean {
            val a = from.bindingAdapterPosition; val b = to.bindingAdapterPosition
            if (a < 0 || b < 0) return false
            items.add(b, items.removeAt(a))
            adapter.notifyItemMoved(a, b)
            changed = true
            return true
        }
        override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {}
        override fun clearView(rv: RecyclerView, holder: RecyclerView.ViewHolder) {
            super.clearView(rv, holder)
            // page numbers follow the new order
            adapter.notifyDataSetChanged()
        }
    })

    private inner class Holder(val root: LinearLayout, val image: ImageView, val number: TextView, val buttons: List<TextView>) : RecyclerView.ViewHolder(root) {
        var job: Job? = null
    }

    private inner class PageAdapter : RecyclerView.Adapter<Holder>() {
        override fun getItemCount() = items.size
        override fun getItemId(position: Int) = items[position].id

        @SuppressLint("ClickableViewAccessibility")
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
            val c: Context = parent.context
            val root = LinearLayout(c).apply {
                orientation = LinearLayout.VERTICAL
                layoutParams = RecyclerView.LayoutParams(-1, -2).apply { setMargins(dp(4), dp(4), dp(4), dp(4)) }
                background = GradientDrawable().apply { cornerRadius = dp(6).toFloat(); setColor(Color.WHITE) }
                setPadding(dp(4), dp(4), dp(4), dp(2))
            }
            val frame = FrameLayout(c)
            val image = ImageView(c).apply { scaleType = ImageView.ScaleType.FIT_CENTER; setBackgroundColor(0xFFF6F6F6.toInt()) }
            frame.addView(image, FrameLayout.LayoutParams(-1, -1))
            val number = TextView(c).apply {
                setTextColor(Color.WHITE); setTextSize(TypedValue.COMPLEX_UNIT_SP, 11f)
                background = GradientDrawable().apply { cornerRadius = dp(8).toFloat(); setColor(0x99000000.toInt()) }
                setPadding(dp(6), dp(1), dp(6), dp(1))
            }
            frame.addView(number, FrameLayout.LayoutParams(-2, -2, Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL).apply { bottomMargin = dp(4) })
            root.addView(frame, LinearLayout.LayoutParams(-1, dp(150)))
            val row = LinearLayout(c).apply { orientation = LinearLayout.HORIZONTAL; gravity = Gravity.CENTER }
            val labels = listOf("⟳" to R.string.pdf_organize_rotate, "⧉" to R.string.pdf_organize_duplicate, "✕" to R.string.pdf_organize_delete)
            val buttons = labels.map { (glyph, desc) ->
                TextView(c).apply {
                    text = glyph; gravity = Gravity.CENTER; contentDescription = c.getString(desc)
                    setTextColor(ContextCompat.getColor(c, R.color.text_primary)); setTextSize(TypedValue.COMPLEX_UNIT_SP, 17f)
                    background = android.util.TypedValue().let { tv -> c.theme.resolveAttribute(android.R.attr.selectableItemBackgroundBorderless, tv, true); ContextCompat.getDrawable(c, tv.resourceId) }
                }.also { row.addView(it, LinearLayout.LayoutParams(0, dp(34), 1f)) }
            }
            root.addView(row)
            return Holder(root, image, number, buttons)
        }

        override fun onBindViewHolder(holder: Holder, position: Int) {
            val item = items[position]
            holder.number.text = (position + 1).toString()
            holder.image.rotation = item.turns * 90f
            holder.image.setImageDrawable(null)
            holder.job?.cancel()
            if (item.source >= 0) {
                holder.job = lifecycleScope.launch { loaders.getOrNull(item.source)?.thumbnail(item.page)?.let { holder.image.setImageBitmap(it) } }
            } else {
                holder.image.setImageDrawable(GradientDrawable().apply { setColor(Color.WHITE); setStroke(dp(1), 0xFFDDDDDD.toInt()) })
            }
            fun at(): Int = holder.bindingAdapterPosition
            holder.buttons[0].setOnClickListener { val i = at(); if (i >= 0) { items[i].turns = (items[i].turns + 1) % 4; changed = true; notifyItemChanged(i) } }
            holder.buttons[1].setOnClickListener {
                val i = at(); if (i < 0) return@setOnClickListener
                items.add(i + 1, items[i].copy(id = nextId++)); changed = true
                notifyItemInserted(i + 1); notifyItemRangeChanged(i + 1, items.size - i - 1)
            }
            holder.buttons[2].setOnClickListener {
                val i = at(); if (i < 0) return@setOnClickListener
                items.removeAt(i); changed = true
                notifyItemRemoved(i); notifyItemRangeChanged(i, items.size - i)
            }
        }

        override fun onViewRecycled(holder: Holder) { holder.job?.cancel() }
    }

    companion object {
        private const val ARG_PATH = "path"
        private const val ARG_PASSWORD = "password"
        private const val THUMB_WIDTH = 300

        fun intent(context: Context, file: File, password: String?) = Intent(context, PdfOrganizeActivity::class.java)
            .putExtra(ARG_PATH, file.path).putExtra(ARG_PASSWORD, password)
    }
}
