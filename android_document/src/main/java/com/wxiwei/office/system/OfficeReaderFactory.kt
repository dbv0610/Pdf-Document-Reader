package com.wxiwei.office.system

import com.wxiwei.office.constant.MainConstant
import com.wxiwei.office.fc.doc.DOCReader
import com.wxiwei.office.fc.doc.DOCXReader
import com.wxiwei.office.fc.doc.TXTReader
import com.wxiwei.office.fc.ppt.PPTXReader
import com.wxiwei.office.fc.ppt.PPTReader
import com.wxiwei.office.fc.xls.XLSReader
import com.wxiwei.office.fc.xls.XLSXReader
import java.io.File

/** The extension selects the application family; the signature selects its reader. */
object OfficeReaderFactory {
    private enum class Family { WORD, EXCEL, POWERPOINT, TEXT }

    private val OOXML_EXTENSIONS = setOf(
        "docx", "dotx", "dotm", "xlsx", "xltx", "xltm", "xlsm", "pptx", "pptm", "potx", "potm"
    )

    fun createReader(control: IControl, path: String, encoding: String?): IReader {
        OpenFileErrors.requireReadable(path)
        val name = path.lowercase()
        val family = when {
            listOf(MainConstant.FILE_TYPE_DOC, MainConstant.FILE_TYPE_DOT,
                MainConstant.FILE_TYPE_DOCX, MainConstant.FILE_TYPE_DOTX,
                MainConstant.FILE_TYPE_DOTM).any { name.endsWith(it) } -> Family.WORD
            listOf(MainConstant.FILE_TYPE_XLS, MainConstant.FILE_TYPE_XLT,
                MainConstant.FILE_TYPE_XLSX, MainConstant.FILE_TYPE_XLTX,
                MainConstant.FILE_TYPE_XLTM, MainConstant.FILE_TYPE_XLSM).any { name.endsWith(it) } -> Family.EXCEL
            listOf(MainConstant.FILE_TYPE_PPT, MainConstant.FILE_TYPE_POT,
                MainConstant.FILE_TYPE_PPTX, MainConstant.FILE_TYPE_PPTM,
                MainConstant.FILE_TYPE_POTX, MainConstant.FILE_TYPE_POTM).any { name.endsWith(it) } -> Family.POWERPOINT
            else -> Family.TEXT
        }
        val file = File(path)
        val ext = file.extension.lowercase()
        if (family == Family.TEXT) {
            val reader = TXTReader(control, path, encoding)
            OpenTrace.d("format sniff ext=$ext detected=not-sniffed reader=${reader.javaClass.simpleName}")
            return reader
        }
        val detected = OfficeFormatSniffer.sniff(file)
        if (family == Family.WORD && detected == OfficeFormatSniffer.Format.RTF) {
            OpenTrace.d("format sniff ext=$ext detected=$detected reader=none (RTF)")
            throw Exception("The document is really a RTF file")
        }
        if (detected == OfficeFormatSniffer.Format.ZIP && !OfficeFormatSniffer.hasZipEndRecord(file)) {
            OpenTrace.d("format sniff ext=$ext detected=$detected reader=none (missing ZIP end record)")
            throw BadFileFormatException("ZIP end record missing (detected=$detected, first bytes=${OfficeFormatSniffer.firstBytes(file)})")
        }
        if (detected != OfficeFormatSniffer.Format.ZIP && detected != OfficeFormatSniffer.Format.OLE2) {
            OpenTrace.d("format sniff ext=$ext detected=$detected reader=none")
            throw BadFileFormatException("not an Office document (detected=$detected, first bytes=${OfficeFormatSniffer.firstBytes(file)})")
        }
        if (detected == OfficeFormatSniffer.Format.OLE2 && ext in OOXML_EXTENSIONS && EncryptedOoxml.isEncrypted(file)) {
            val plainPath = EncryptedOoxml.decrypt(control, file, ext)
            val reader = when (family) {
                Family.WORD -> DOCXReader(control, plainPath)
                Family.EXCEL -> XLSXReader(control, plainPath)
                Family.POWERPOINT -> PPTXReader(control, plainPath)
                else -> error("Unexpected Office family: $family")
            }
            OpenTrace.d("format sniff ext=$ext detected=encrypted-OOXML reader=${reader.javaClass.simpleName}")
            return reader
        }
        val zip = detected == OfficeFormatSniffer.Format.ZIP
        val reader = when (family) {
            Family.WORD -> if (zip) DOCXReader(control, path) else DOCReader(control, path)
            Family.EXCEL -> if (zip) XLSXReader(control, path) else XLSReader(control, path)
            Family.POWERPOINT -> if (zip) PPTXReader(control, path) else PPTReader(control, path)
            else -> error("Unexpected Office family: $family")
        }
        OpenTrace.d("format sniff ext=$ext detected=$detected reader=${reader.javaClass.simpleName}")
        return reader
    }
}
