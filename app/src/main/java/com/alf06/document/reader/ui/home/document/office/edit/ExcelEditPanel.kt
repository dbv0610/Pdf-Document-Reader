package com.alf06.document.reader.ui.home.document.office.edit

import android.view.View
import android.view.inputmethod.EditorInfo
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.wxiwei.office.editor.xlsx.A1FormulaShifter
import com.wxiwei.office.editor.xlsx.CellFormat
import com.wxiwei.office.editor.xlsx.SheetEditSession
import com.wxiwei.office.reader.OfficeDocumentView
import com.wxiwei.office.ss.control.ExcelView
import com.wxiwei.office.ss.model.style.CellStyle
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.io.File

/**
 * Excel: the selected cell (tap a cell in the sheet) can be typed into (values or =formulas,
 * dependent formulas recalculate at once) and formatted; Save writes the .xlsx in place.
 */
internal class ExcelEditPanel(activity: AppCompatActivity, reader: OfficeDocumentView, file: File) :
    OfficeEditPanel(activity, reader, file) {

    private val excel = reader.control!!.getView() as ExcelView
    private var session = SheetEditSession(reader.control!!, file)
    private var sheet = -1
    private var row = -1
    private var col = -1

    private val cellName = label("A1").apply {
        contentDescription = "Ô / vùng chọn"
        setOnClickListener { askRange() }
    }
    private val value = input("Giá trị hoặc =công thức").apply {
        imeOptions = EditorInfo.IME_ACTION_DONE
        setOnEditorActionListener { _, _, _ -> applyValue(); true }
    }
    private val watcher: Job

    override val view: View = column().apply {
        addView(line(cellName, value, button("✓", bold = true) { applyValue() }, weights = floatArrayOf(0f, 1f, 0f)))
        addView(toolRow(
            button("B", bold = true) { toggleFont { CellFormat(bold = !it.isBold()) } },
            button("I") { toggleFont { CellFormat(italic = !it.isItalic()) } },
            button("U") { toggleFont { CellFormat(underline = it.getUnderline() == 0) } },
            button("S̶") { toggleFont { CellFormat(strike = !it.isStrikeline()) } },
            button("Màu chữ") { pickColor("Màu chữ") { c -> c?.let { format(CellFormat(fontColor = it)) } } },
            button("Màu nền") { pickColor("Màu nền", none = "Bỏ nền") { c -> format(CellFormat(fillColor = c ?: "none")) } },
            button("Cỡ chữ") { pickSize { format(CellFormat(fontSize = it.toDouble())) } },
            button("⇤") { format(CellFormat(horizontal = "left")) },
            button("↔") { format(CellFormat(horizontal = "center")) },
            button("⇥") { format(CellFormat(horizontal = "right")) },
            button("Căn lề…") { pickAlignment() },
            button("Xuống dòng") { toggleWrap() },
            button("Xoay chữ") { pickRotation() },
            button("Viền") { pickBorder() },
            button("Định dạng số") { pickNumberFormat() },
            button("Gộp ô") { mergeOrSplit() },
            button("+ Ảnh") { pickImage() },
            button("Xóa ô") { deleteSelection() },
        ))
        addView(toolRow(
            button("Rộng cột") { askSize("Độ rộng cột " + A1FormulaShifter.address(0, col).dropLast(1) + " (ký tự)", session.columnWidth(sheet, col)) { structural { session.setColumnWidth(sheet, col, it) } } },
            button("Cột −") { structural { session.setColumnWidth(sheet, col, maxOf(0.5, session.columnWidth(sheet, col) * 0.8)) } },
            button("Cột +") { structural { session.setColumnWidth(sheet, col, minOf(255.0, session.columnWidth(sheet, col) * 1.25)) } },
            button("Cao hàng") { askSize("Chiều cao hàng ${row + 1} (pt)", session.rowHeight(sheet, row)) { structural { session.setRowHeight(sheet, row, it) } } },
            button("Hàng −") { structural { session.setRowHeight(sheet, row, maxOf(1.0, session.rowHeight(sheet, row) * 0.8)) } },
            button("Hàng +") { structural { session.setRowHeight(sheet, row, minOf(409.0, session.rowHeight(sheet, row) * 1.25)) } },
            button("+ Dòng trên") { structural { session.insertRows(sheet, row, 1) } },
            button("+ Dòng dưới") { structural { session.insertRows(sheet, row + 1, 1) } },
            button("− Dòng") { structural { session.deleteRows(sheet, row, 1) } },
            button("+ Cột trái") { structural { session.insertColumns(sheet, col, 1) } },
            button("+ Cột phải") { structural { session.insertColumns(sheet, col + 1, 1) } },
            button("− Cột") { structural { session.deleteColumns(sheet, col, 1) } },
            button("↶") { if (!session.undo()) toast("Không còn gì để hoàn tác") else { syncSheets(); dropStalePicture(); refresh(true) } },
            button("↷") { if (!session.redo()) toast("Không còn gì để làm lại") else { syncSheets(); dropStalePicture(); refresh(true) } },
            button("+ Sheet") { askNewSheet() },
            button("Lưu", bold = true, color = 0xFFD96D00.toInt()) { commitTyped(); save() },
            button("Lưu bản sao") { commitTyped(); saveCopy() },
        ))
    }

    init {
        keepAboveKeyboard(true)
        // a picture added here: touch it to select and drag it, drag a corner to resize it;
        // a range: long press a cell and drag, or drag the round handle of the selection
        reader.onDocumentGesture = gesture@{ type, event ->
            val ss = excel.getSpreadsheet() ?: return@gesture false
            val sv = ss.getSheetView() ?: return@gesture false
            when (type) {
                com.wxiwei.office.system.IMainFrame.ON_DOWN -> {
                    if (grabPicture(event)) return@gesture true
                    if (sv.getCurrentSheet()?.getSelectionRange() == null) return@gesture false
                    val h = sv.selectionHandle()
                    val p = sheetPoint(event.rawX, event.rawY)
                    if (Math.hypot((p.x - h.x).toDouble(), (p.y - h.y).toDouble()) > dp(24)) return@gesture false
                    reader.touchCapture = { e -> dragSelection(e) }
                    true
                }
                com.wxiwei.office.system.IMainFrame.ON_LONG_PRESS -> {
                    val p = sheetPoint(event.rawX, event.rawY)
                    val at = sv.cellAt(p.x, p.y) ?: return@gesture false
                    commitTyped()
                    sv.getCurrentSheet()!!.setActiveCellRowCol(at[0], at[1])
                    sv.getCurrentSheet()!!.setSelectionEnd(at[0], at[1])
                    ss.postInvalidate()
                    reader.touchCapture = { e -> dragSelection(e) }
                    true
                }
                else -> false
            }
        }
        // the sheet selects a cell on tap; follow it
        watcher = activity.lifecycleScope.launch {
            while (isActive) {
                refresh(false)
                delay(250)
            }
        }
    }

    private companion object {
        const val MAX_PICTURE_PX = 320
    }

    override fun close() {
        super.close()
        reader.onDocumentGesture = null
        reader.touchCapture = null
        excel.getSpreadsheet()?.getSheetView()?.selectedShape = null
        excel.getSpreadsheet()?.getSheetView()?.getCurrentSheet()?.let { it.setActiveCellRowCol(it.getActiveCellRow(), it.getActiveCellColumn()) }
        watcher.cancel()
    }

    // the cell last selected in the sheet, and the input of the edited cell as loaded
    private var seen = Triple(-1, -1, -1)
    private var loaded = ""
    // a reference put in the formula by a tap: the next tap replaces it
    private var tappedRef: IntRange? = null

    private fun refresh(force: Boolean) {
        val ss = excel.getSpreadsheet() ?: return
        val current = ss.getSheetView()?.getCurrentSheet() ?: return
        val s = session.sheetIndexOf(current)
        val r = current.getActiveCellRow()
        val c = current.getActiveCellColumn()
        val tapped = Triple(s, r, c)
        val range = current.getSelectionRange()
        cellName.text = range?.let { A1FormulaShifter.address(it.getFirstRow(), it.getFirstColumn()) + ":" + A1FormulaShifter.address(it.getLastRow(), it.getLastColumn()) }
            ?: A1FormulaShifter.address(r, c)
        if (!force && tapped == seen) return
        seen = tapped
        val typed = value.text.toString()
        if (!force && sheet >= 0 && typed != loaded) {
            // a formula being typed: the tapped cell goes in as a reference, like in Excel
            if (typed.startsWith("=")) return insertReference(current, s, r, c)
            applyValue()
        }
        sheet = s; row = r; col = c
        loaded = session.getInput(s, r, c)
        tappedRef = null
        value.setText(loaded)
    }

    private fun insertReference(current: com.wxiwei.office.ss.model.baseModel.Sheet, s: Int, r: Int, c: Int) {
        val name = current.getSheetName() ?: ""
        val ref = (if (s != sheet) "'" + name.replace("'", "''") + "'!" else "") + A1FormulaShifter.address(r, c)
        val text = value.text
        val previous = tappedRef?.takeIf { it.last < text.length && value.selectionStart == it.last + 1 }
        val start = previous?.first ?: value.selectionStart.coerceAtLeast(0)
        val end = previous?.let { it.last + 1 } ?: value.selectionEnd.coerceAtLeast(0)
        text.replace(start, end, ref)
        tappedRef = start until start + ref.length
        value.requestFocus()
        value.setSelection(start + ref.length)
    }

    /** Writes what was typed in the edited cell (Save, or a tap on another cell). */
    private fun commitTyped() {
        if (sheet >= 0 && value.text.toString() != loaded) applyValue()
    }

    private fun applyValue() {
        if (sheet < 0) return
        if (!session.setCellInput(sheet, row, col, value.text.toString())) {
            toast(session.lastError?.message ?: "Không sửa được ô")
            return
        }
        loaded = value.text.toString()
        tappedRef = null
        session.warnings.firstOrNull()?.let { toast(it) }
        refresh(true)
    }

    private fun pickNumberFormat() {
        val formats = listOf("Chung" to "General", "Số nguyên 0" to "0", "Số 0.00" to "0.00", "Hàng nghìn #,##0" to "#,##0",
            "Hàng nghìn #,##0.00" to "#,##0.00", "Phần trăm 0%" to "0%", "Phần trăm 0.00%" to "0.00%",
            "Tiền ₫" to "#,##0 \"₫\"", "Tiền $" to "\$#,##0.00", "Ngày d/m/yyyy" to "d/m/yyyy",
            "Ngày giờ d/m/yyyy h:mm" to "d/m/yyyy h:mm", "Giờ h:mm:ss" to "h:mm:ss", "Chữ (@)" to "@")
        dialogs.pick("Định dạng số", formats.map { it.first }) { i -> format(CellFormat(numberFormat = formats[i].second)) }
    }

    /** Borders of the cell: all around (thin or thick), at the bottom, or none; then their color. */
    private fun pickBorder() {
        if (sheet < 0) return
        val choices = listOf("Viền quanh" to "all", "Viền quanh đậm" to "thick", "Viền dưới" to "bottom", "Không viền" to "none")
        dialogs.pick("Viền ô", choices.map { it.first }) { i ->
            val kind = choices[i].second
            if (kind == "none") format(CellFormat(border = "none"))
            else pickColor("Màu viền") { c -> format(CellFormat(border = kind, borderColor = c ?: "000000")) }
        }
    }

    /**
     * Excel's alignment tab: horizontal (general, left, center, right, fill, justify, center across
     * selection, distributed), vertical (top, center, bottom, justify, distributed), indent and wrap,
     * starting from the cell's current values; applied as one undoable step.
     */
    private fun pickAlignment() {
        if (sheet < 0) return
        val style = currentStyle()
        val horizontals = listOf("Chung" to "general", "Trái" to "left", "Giữa" to "center", "Phải" to "right",
            "Lặp đầy ô" to "fill", "Đều hai bên" to "justify", "Giữa vùng chọn" to "centerContinuous", "Phân tán" to "distributed")
        val verticals = listOf("Trên" to "top", "Giữa" to "center", "Dưới" to "bottom", "Đều hai bên" to "justify", "Phân tán" to "distributed")
        val nowH = when (style?.getHorizontalAlign()) {
            CellStyle.ALIGN_LEFT -> "left"; CellStyle.ALIGN_CENTER -> "center"; CellStyle.ALIGN_RIGHT -> "right"
            CellStyle.ALIGN_FILL -> "fill"; CellStyle.ALIGN_JUSTIFY -> "justify"; CellStyle.ALIGN_CENTER_SELECTION -> "centerContinuous"
            else -> "general"
        }
        val nowV = when (style?.getVerticalAlign()) {
            CellStyle.VERTICAL_TOP -> "top"; CellStyle.VERTICAL_CENTER -> "center"; CellStyle.VERTICAL_JUSTIFY -> "justify"
            else -> "bottom"
        }
        val nowIndent = style?.getIndent()?.toInt() ?: 0
        val nowWrap = style?.isWrapText() == true
        dialogs.show("Căn lề") {
            caption("Căn ngang")
            val hGroup = choices(horizontals.map { it.first }, horizontals.indexOfFirst { it.second == nowH })
            caption("Căn dọc")
            val vGroup = choices(verticals.map { it.first }, verticals.indexOfFirst { it.second == nowV })
            caption("Thụt lề (Trái / Phải / Phân tán)")
            val indent = stepper(nowIndent, 0..15, "Giảm thụt lề", "Tăng thụt lề")
            val wrap = check("Xuống dòng khi chữ dài hơn ô", nowWrap)
            positive("Áp dụng") {
                val h = horizontals.getOrNull(hGroup.picked)?.second
                val v = verticals.getOrNull(vGroup.picked)?.second
                // only what changed, so the file keeps the rest of the cell's alignment as it was
                val f = CellFormat(
                    horizontal = h?.takeIf { it != nowH },
                    vertical = v?.takeIf { it != nowV },
                    indent = indent().takeIf { it != nowIndent },
                    wrap = wrap.isChecked.takeIf { it != nowWrap },
                )
                if (f.changesAlignment) format(f)
            }
            negative()
        }
    }

    /** Turns the text of the cell: flat, 45° / 90° up or down, or letters stacked. */
    private fun pickRotation() {
        if (sheet < 0) return
        val choices = listOf("Nằm ngang" to 0, "Nghiêng lên 45°" to 45, "Dọc lên (90°)" to 90, "Nghiêng xuống 45°" to 135, "Dọc xuống (90°)" to 180, "Chữ xếp dọc" to 255)
        dialogs.pick("Xoay chữ", choices.map { it.first }) { i ->
            val rotation = choices[i].second
            format(CellFormat(rotation = rotation))
            fitRowToTurnedText(rotation)
        }
    }

    /** Like Excel: a row too low for the turned text of the cell grows to hold it. */
    private fun fitRowToTurnedText(rotation: Int) {
        if (rotation == 0) return
        val text = session.getInput(sheet, row, col).takeIf { it.isNotEmpty() } ?: return
        val book = excel.getSpreadsheet()?.getWorkbook() ?: return
        val sizePt = (currentStyle()?.let { book.getFont(it.getFontIndex().toInt()) }?.getFontSize() ?: 11.0).toFloat()
        // points: a paint whose text size is the font size measures in points
        val paint = android.graphics.Paint().apply { textSize = sizePt }
        val width = paint.measureText(text)
        val line = paint.fontMetrics.let { it.descent - it.ascent }
        val needed = if (rotation == 255) line * text.length else {
            val rad = Math.toRadians((if (rotation <= 90) rotation else rotation - 90).toDouble())
            (Math.abs(Math.sin(rad)) * width + Math.abs(Math.cos(rad)) * line).toFloat()
        } + 4f
        if (needed > session.rowHeight(sheet, row)) structural { session.setRowHeight(sheet, row, minOf(409.0, needed.toDouble())) }
    }

    /** Asks the new sheet's name, adds it after the last one and shows it. */
    private fun askNewSheet() {
        dialogs.show("Thêm sheet") {
            val field = input("Tên sheet", session.nextSheetName()).apply { selectAll() }
            positive("Thêm") {
                val index = session.addSheet(field.text.toString().trim())
                if (index < 0) return@positive toast(session.lastError?.message ?: "Không thêm được sheet")
                excel.refreshSheetBar(index)
                excel.showSheet(index)
                refresh(true)
            }
            negative()
        }
    }

    /** The sheet tabs after an undo/redo added or took out a sheet; a sheet gone shows the last one. */
    private fun syncSheets() {
        val ss = excel.getSpreadsheet() ?: return
        val book = ss.getWorkbook() ?: return
        val shown = ss.getSheetView()?.getCurrentSheet()?.let { book.getSheetIndex(it) } ?: -1
        val focus = if (shown in 0 until book.getSheetCount()) shown else book.getSheetCount() - 1
        excel.refreshSheetBar(focus)
        if (focus != shown) excel.showSheet(focus)
    }

    private fun askSize(title: String, current: Double, onSet: (Double) -> Unit) {
        if (sheet < 0) return
        dialogs.show(title) {
            val field = input("", "%.1f".format(java.util.Locale.ROOT, current), numeric = true).apply { selectAll() }
            positive("OK") { field.text.toString().replace(',', '.').toDoubleOrNull()?.let(onSet) ?: toast("Số không hợp lệ") }
            negative()
        }
    }

    private fun structural(action: () -> Boolean) {
        if (sheet < 0) return
        if (!action()) toast(session.lastError?.message ?: "Không thực hiện được")
        else {
            session.warnings.firstOrNull()?.let { toast(it) }
            refresh(true)
        }
    }

    /** The selected range (or null for one cell) of the shown sheet. */
    private fun range(): com.wxiwei.office.ss.model.CellRangeAddress? =
        excel.getSpreadsheet()?.getSheetView()?.getCurrentSheet()?.getSelectionRange()

    /** Formats the selected range, or the cell. */
    private fun format(f: CellFormat) {
        if (sheet < 0) return
        val r = range()
        val ok = if (r != null) session.setRangeFormat(sheet, r.getFirstRow(), r.getFirstColumn(), r.getLastRow(), r.getLastColumn(), f)
            else session.setCellFormat(sheet, row, col, f)
        if (!ok) toast(session.lastError?.message ?: "Không định dạng được")
    }

    /** Where a screen point falls in the sheet view. */
    private fun sheetPoint(rawX: Float, rawY: Float): android.graphics.PointF {
        val loc = IntArray(2)
        excel.getSpreadsheet()!!.getLocationOnScreen(loc)
        return android.graphics.PointF(rawX - loc[0], rawY - loc[1])
    }

    /** The finger moves the far corner of the range; near an edge the sheet scrolls along. */
    private fun dragSelection(e: android.view.MotionEvent) {
        val ss = excel.getSpreadsheet() ?: return
        val sv = ss.getSheetView() ?: return
        val p = sheetPoint(e.rawX, e.rawY)
        val edge = dp(32).toFloat()
        val dx = when { p.x > ss.width - edge -> dp(12).toFloat(); p.x < sv.getRowHeaderWidth() + edge / 2 -> -dp(12).toFloat(); else -> 0f }
        val dy = when { p.y > ss.height - edge -> dp(12).toFloat(); p.y < sv.getColumnHeaderHeight() + edge / 2 -> -dp(12).toFloat(); else -> 0f }
        if (dx != 0f || dy != 0f) sv.scrollBy(dx / sv.getZoom(), dy / sv.getZoom(), true)
        val at = sv.cellAt(p.x.coerceAtLeast(sv.getRowHeaderWidth() + 1f), p.y.coerceAtLeast(sv.getColumnHeaderHeight() + 1f)) ?: return
        sv.getCurrentSheet()!!.setSelectionEnd(at[0], at[1])
        ss.abortDrawing()
        ss.postInvalidate()
    }

    /** The name box: type a cell ("B3") or a range ("A1:C10") to select it. */
    private fun askRange() {
        dialogs.show("Chọn ô / vùng") {
            val field = input("A1 hoặc A1:C10", cellName.text.toString()).apply { selectAll() }
            positive("Chọn") { selectRange(field.text.toString()) }
            negative()
        }
    }

    private fun selectRange(text: String) {
        val m = Regex("(?i)^\\s*\\$?([A-Z]{1,3})\\$?(\\d{1,7})(?:\\s*:\\s*\\$?([A-Z]{1,3})\\$?(\\d{1,7}))?\\s*$").find(text)
            ?: return toast("Vùng không hợp lệ")
        fun col(s: String) = s.uppercase().fold(0) { n, ch -> n * 26 + (ch - 'A' + 1) } - 1
        val (c1, r1) = col(m.groupValues[1]) to m.groupValues[2].toInt() - 1
        val (c2, r2) = if (m.groupValues[3].isEmpty()) c1 to r1 else col(m.groupValues[3]) to m.groupValues[4].toInt() - 1
        if (minOf(r1, r2) < 0 || maxOf(r1, r2) >= 1048576 || maxOf(c1, c2) >= 16384) return toast("Vùng không hợp lệ")
        val ss = excel.getSpreadsheet() ?: return
        val current = ss.getSheetView()?.getCurrentSheet() ?: return
        commitTyped()
        current.setActiveCellRowCol(r1, c1)
        if (r2 != r1 || c2 != c1) current.setSelectionEnd(r2, c2)
        ss.getSheetView()?.goToCell(minOf(r1, r2), minOf(c1, c2))
        ss.postInvalidate()
        refresh(true)
    }

    /** Picks a picture from the device and puts it at the selected cell. */
    private fun pickImage() {
        if (sheet < 0) return toast("Chọn một ô trước")
        var launcher: androidx.activity.result.ActivityResultLauncher<String>? = null
        launcher = activity.activityResultRegistry.register("sheet-image-" + System.nanoTime(),
            androidx.activity.result.contract.ActivityResultContracts.GetContent()) { uri ->
            launcher?.unregister()
            if (uri != null) addImage(uri)
        }
        launcher.launch("image/*")
    }

    private fun addImage(uri: android.net.Uri) {
        val type = context.contentResolver.getType(uri) ?: "image/jpeg"
        val ext = when { type.contains("png") -> "png"; type.contains("gif") -> "gif"; type.contains("bmp") -> "bmp"; else -> "jpeg" }
        val image = File(context.cacheDir, "sheet-image-" + System.nanoTime() + "." + ext)
        try {
            context.contentResolver.openInputStream(uri)!!.use { input -> image.outputStream().use { input.copyTo(it) } }
        } catch (e: Exception) {
            return toast("Không đọc được ảnh")
        }
        addImageFile(image)
    }

    /** The picture at the selected cell: its own size, at most [MAX_PICTURE_PX] wide or high at 100%. */
    internal fun addImageFile(image: File): Boolean {
        val bounds = android.graphics.BitmapFactory.Options().apply { inJustDecodeBounds = true }
        android.graphics.BitmapFactory.decodeFile(image.path, bounds)
        if (bounds.outWidth <= 0 || bounds.outHeight <= 0) { toast("Không đọc được ảnh"); return false }
        val scale = minOf(1f, MAX_PICTURE_PX.toFloat() / maxOf(bounds.outWidth, bounds.outHeight))
        val w = maxOf(1, Math.round(bounds.outWidth * scale)); val h = maxOf(1, Math.round(bounds.outHeight * scale))
        commitTyped()
        if (!session.addPicture(sheet, row, col, image, w, h)) { toast(session.lastError?.message ?: "Không thêm được ảnh"); return false }
        // the new picture is selected: it can be dragged at once
        excel.getSpreadsheet()?.getSheetView()?.let { sv ->
            sv.selectedShape = sv.getCurrentSheet()?.getShapes()?.lastOrNull()
            excel.getSpreadsheet()?.postInvalidate()
        }
        return true
    }

    /**
     * A touch on a corner of the selected picture resizes it (keeping its proportions), on a
     * picture added in this session selects and moves it; elsewhere the picture is let go.
     * The picture follows the finger and is placed, as one undoable step, when it is lifted.
     */
    private fun grabPicture(event: android.view.MotionEvent): Boolean {
        val ss = excel.getSpreadsheet() ?: return false
        val sv = ss.getSheetView() ?: return false
        val p = sheetPoint(event.rawX, event.rawY)
        val selected = sv.selectedShape?.takeIf { session.isMovablePicture(it) && sv.getCurrentSheet()?.getShapes()?.contains(it) == true }
        val box = selected?.let { sv.shapeRect(it) }
        val reach = dp(24).toFloat()
        // corners: 0 top-left, 1 top-right, 2 bottom-left, 3 bottom-right
        val corner = box?.let { b ->
            listOf(b.left to b.top, b.right to b.top, b.left to b.bottom, b.right to b.bottom)
                .indexOfFirst { (x, y) -> Math.hypot((p.x - x).toDouble(), (p.y - y).toDouble()) <= reach }
        } ?: -1
        val shape = if (corner >= 0) selected!! else sv.shapeAt(p.x, p.y) { session.isMovablePicture(it) }
        if (shape == null) {
            if (sv.selectedShape != null) { sv.selectedShape = null; ss.postInvalidate() }
            return false
        }
        commitTyped()
        sv.selectedShape = shape
        ss.abortDrawing()
        ss.postInvalidate()
        val start = shape.bounds!!.let { com.wxiwei.office.java.awt.Rectangle(it.x, it.y, it.width, it.height) }
        val downX = event.rawX
        val downY = event.rawY
        reader.touchCapture = { e ->
            val zoom = sv.getZoom()
            val dx = (e.rawX - downX) / zoom
            val dy = (e.rawY - downY) / zoom
            val to = if (corner < 0) com.wxiwei.office.java.awt.Rectangle(
                maxOf(0, Math.round(start.x + dx)), maxOf(0, Math.round(start.y + dy)), start.width, start.height)
            else {
                val sx = if (corner == 1 || corner == 3) 1 else -1
                val sy = if (corner >= 2) 1 else -1
                val min = 8f / maxOf(1, minOf(start.width, start.height))
                val scale = maxOf(min, (start.width + sx * dx) / start.width, (start.height + sy * dy) / start.height)
                val w = maxOf(1, Math.round(start.width * scale)); val h = maxOf(1, Math.round(start.height * scale))
                com.wxiwei.office.java.awt.Rectangle(if (sx > 0) start.x else start.x + start.width - w,
                    if (sy > 0) start.y else start.y + start.height - h, w, h)
            }
            if (e.actionMasked == android.view.MotionEvent.ACTION_UP || e.actionMasked == android.view.MotionEvent.ACTION_CANCEL) {
                shape.bounds = start
                if (e.actionMasked == android.view.MotionEvent.ACTION_UP && !session.setPictureBounds(shape, to))
                    toast(session.lastError?.message ?: "Không di chuyển được ảnh")
                ss.postInvalidate()
            } else {
                shape.bounds = to
                ss.abortDrawing()
                ss.postInvalidate()
            }
        }
        return true
    }

    /** "Xóa ô": the selected picture, or else the cell. */
    private fun deleteSelection() {
        val sv = excel.getSpreadsheet()?.getSheetView()
        val picture = sv?.selectedShape?.takeIf { session.isMovablePicture(it) }
        if (picture != null) {
            if (session.removePicture(picture)) sv.selectedShape = null
            else toast(session.lastError?.message ?: "Không xóa được ảnh")
            return
        }
        if (sheet >= 0 && session.clearCell(sheet, row, col)) refresh(true)
    }

    /** A picture taken off by an undo/redo is no longer selected. */
    private fun dropStalePicture() {
        val sv = excel.getSpreadsheet()?.getSheetView() ?: return
        val shape = sv.selectedShape ?: return
        if (sv.getCurrentSheet()?.getShapes()?.contains(shape) != true) sv.selectedShape = null
    }

    /** "Gộp ô": merges the selected range (asks when values would be dropped); on a merged cell, splits it. */
    private fun mergeOrSplit() {
        if (sheet < 0) return
        val r = range()
        if (r == null) {
            if (session.mergeAt(sheet, row, col) == null) return toast("Nhấn giữ một ô rồi kéo để chọn vùng cần gộp")
            return structural { session.unmergeCells(sheet, row, col) }
        }
        val merge = { structural { session.mergeCells(sheet, r.getFirstRow(), r.getFirstColumn(), r.getLastRow(), r.getLastColumn()) } }
        if (!session.mergeDropsValues(sheet, r.getFirstRow(), r.getFirstColumn(), r.getLastRow(), r.getLastColumn())) return merge()
        dialogs.confirm("Gộp ô", "Gộp ô chỉ giữ giá trị của ô trên cùng bên trái, các giá trị khác sẽ bị xóa.", "Gộp") { merge() }
    }

    private fun currentStyle(): CellStyle? =
        excel.getSpreadsheet()?.getWorkbook()?.getSheet(sheet)?.getRow(row)?.getCell(col)?.getCellStyle()

    private fun toggleFont(change: (com.wxiwei.office.simpletext.font.Font) -> CellFormat) {
        val book = excel.getSpreadsheet()?.getWorkbook() ?: return
        val font = currentStyle()?.let { book.getFont(it.getFontIndex().toInt()) } ?: com.wxiwei.office.simpletext.font.Font()
        format(change(font))
    }

    private fun toggleWrap() = format(CellFormat(wrap = currentStyle()?.isWrapText() != true))

    override fun hasChanges(): Boolean { commitTyped(); return session.hasChanges() }

    override fun writeTo(target: File) = session.save(target)

    // the file now holds every change: start a new session on it
    override fun onSaved() {
        session = SheetEditSession(reader.control!!, file)
    }
}
