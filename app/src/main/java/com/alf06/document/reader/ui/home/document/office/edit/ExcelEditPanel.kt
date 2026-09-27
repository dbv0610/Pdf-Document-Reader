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

    private val cellName = label("A1")
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
            button("⤒") { format(CellFormat(vertical = "top")) },
            button("⤓") { format(CellFormat(vertical = "bottom")) },
            button("Xuống dòng") { toggleWrap() },
            button("Định dạng số") { pickNumberFormat() },
            button("Xóa ô") { if (sheet >= 0 && session.clearCell(sheet, row, col)) refresh(true) },
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
            button("↶") { if (!session.undo()) toast("Không còn gì để hoàn tác") else refresh(true) },
            button("↷") { if (!session.redo()) toast("Không còn gì để làm lại") else refresh(true) },
            button("Lưu", bold = true, color = 0xFFD96D00.toInt()) { commitTyped(); save() },
            button("Lưu bản sao") { commitTyped(); saveCopy() },
        ))
    }

    init {
        keepAboveKeyboard(true)
        // the sheet selects a cell on tap; follow it
        watcher = activity.lifecycleScope.launch {
            while (isActive) {
                refresh(false)
                delay(250)
            }
        }
    }

    override fun close() {
        super.close()
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
        if (!force && tapped == seen) return
        seen = tapped
        val typed = value.text.toString()
        if (!force && sheet >= 0 && typed != loaded) {
            // a formula being typed: the tapped cell goes in as a reference, like in Excel
            if (typed.startsWith("=")) return insertReference(current, s, r, c)
            applyValue()
        }
        sheet = s; row = r; col = c
        cellName.text = A1FormulaShifter.address(r, c)
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
        androidx.appcompat.app.AlertDialog.Builder(context).setTitle("Định dạng số")
            .setItems(formats.map { it.first }.toTypedArray()) { _, i -> format(CellFormat(numberFormat = formats[i].second)) }
            .setNegativeButton("Hủy", null).show()
    }

    /** Asks for a number, starting from [current]. */
    private fun askSize(title: String, current: Double, onSet: (Double) -> Unit) {
        if (sheet < 0) return
        val field = input("").apply {
            inputType = android.text.InputType.TYPE_CLASS_NUMBER or android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL
            setText("%.1f".format(java.util.Locale.ROOT, current))
            selectAll()
        }
        androidx.appcompat.app.AlertDialog.Builder(context).setTitle(title).setView(field)
            .setPositiveButton("OK") { _, _ -> field.text.toString().replace(',', '.').toDoubleOrNull()?.let(onSet) ?: toast("Số không hợp lệ") }
            .setNegativeButton("Hủy", null).show()
    }

    private fun structural(action: () -> Boolean) {
        if (sheet < 0) return
        if (!action()) toast(session.lastError?.message ?: "Không thực hiện được")
        else {
            session.warnings.firstOrNull()?.let { toast(it) }
            refresh(true)
        }
    }

    private fun format(f: CellFormat) {
        if (sheet < 0) return
        if (!session.setCellFormat(sheet, row, col, f)) toast(session.lastError?.message ?: "Không định dạng được")
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
