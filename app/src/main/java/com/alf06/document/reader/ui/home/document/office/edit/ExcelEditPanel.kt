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
            button("Nền vàng") { format(CellFormat(fillColor = "FFF2CC")) },
            button("Nền xanh") { format(CellFormat(fillColor = "DDEBF7")) },
            button("Bỏ nền") { format(CellFormat(fillColor = "none")) },
            button("Chữ đỏ") { format(CellFormat(fontColor = "C00000")) },
            button("⇤") { format(CellFormat(horizontal = "left")) },
            button("↔") { format(CellFormat(horizontal = "center")) },
            button("⇥") { format(CellFormat(horizontal = "right")) },
            button("Xuống dòng") { toggleWrap() },
            button("0.00") { format(CellFormat(numberFormat = "0.00")) },
            button("%") { format(CellFormat(numberFormat = "0%")) },
            button("+ Dòng trên") { structural { session.insertRows(sheet, row, 1) } },
            button("− Dòng") { structural { session.deleteRows(sheet, row, 1) } },
            button("+ Cột trái") { structural { session.insertColumns(sheet, col, 1) } },
            button("− Cột") { structural { session.deleteColumns(sheet, col, 1) } },
            button("↶") { if (!session.undo()) toast("Không còn gì để hoàn tác") else refresh(true) },
            button("↷") { if (!session.redo()) toast("Không còn gì để làm lại") else refresh(true) },
            button("Lưu", bold = true, color = 0xFFD96D00.toInt()) { save() },
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

    private fun refresh(force: Boolean) {
        val ss = excel.getSpreadsheet() ?: return
        val current = ss.getSheetView()?.getCurrentSheet() ?: return
        val s = session.sheetIndexOf(current)
        val r = current.getActiveCellRow()
        val c = current.getActiveCellColumn()
        if (!force && s == sheet && r == row && c == col) return
        sheet = s; row = r; col = c
        cellName.text = A1FormulaShifter.address(r, c)
        if (!value.hasFocus() || force) value.setText(session.getInput(s, r, c))
    }

    private fun applyValue() {
        if (sheet < 0) return
        if (!session.setCellInput(sheet, row, col, value.text.toString())) {
            toast(session.lastError?.message ?: "Không sửa được ô")
            return
        }
        session.warnings.firstOrNull()?.let { toast(it) }
        refresh(true)
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

    private fun save() {
        if (!session.hasChanges()) {
            toast("Chưa có thay đổi")
            return
        }
        val result = saveOver(file) { target -> session.save(target) }
        report(result, "Đã lưu " + file.name)
        // the file now holds every change: start a new session on it
        if (result is com.wxiwei.office.editor.EditResult.Ok) session = SheetEditSession(reader.control!!, file)
    }
}
