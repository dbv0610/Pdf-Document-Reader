package com.wxiwei.office.fc.xls

import android.util.Xml
import com.wxiwei.office.fc.dom4j.Element
import com.wxiwei.office.fc.dom4j.ElementHandler
import com.wxiwei.office.fc.dom4j.ElementPath
import com.wxiwei.office.fc.dom4j.io.SAXReader
import com.wxiwei.office.fc.openxml4j.opc.PackagePart
import com.wxiwei.office.fc.openxml4j.opc.PackageRelationshipTypes
import com.wxiwei.office.fc.openxml4j.opc.ZipPackage
import com.wxiwei.office.fc.xls.Reader.WorkbookReader
import com.wxiwei.office.fc.xls.Reader.shared.StyleReader
import com.wxiwei.office.fc.xls.Reader.shared.ThemeColorReader
import com.wxiwei.office.ss.model.baseModel.Workbook
import com.wxiwei.office.ss.model.sheetProperty.Palette
import com.wxiwei.office.ss.util.ColorUtil
import com.wxiwei.office.system.AbortReaderError
import com.wxiwei.office.system.IControl
import com.wxiwei.office.system.StopReaderError
import java.io.File
import java.io.InputStream
import org.xmlpull.v1.XmlPullParser

class XLSXReader(control: IControl, filePath: String) : SSReader() {
    private var filePath: String? = filePath
    private var zipPackage: ZipPackage? = null
    private var book: Workbook? = null
    private var packagePart: PackagePart? = null
    private var sharedStringIndex = 0
    private var key: String? = null
    private var searched = false

    init {
        this.control = control
    }

    override fun abortReader() {
        super.abortReader()
        // Activity disposal must also stop the progressive worksheet job.
        // Do not put this in dispose(): FileReaderThread disposes the reader
        // after the first model is delivered, while progressive loading still
        // has to continue for the visible workbook.
        WorkbookReader.instance().cancelReading()
    }

    override fun getModel(): Any? {
        book = Workbook(false)
        zipPackage = ZipPackage(filePath!!)
        initPackagePart()
        processWorkbook()
        return book
    }

    private fun initPackagePart() {
        val relationship = zipPackage!!.getRelationshipsByType(PackageRelationshipTypes.CORE_DOCUMENT).getRelationship(0)
        if (relationship.targetURI.toString() != "/xl/workbook.xml") {
            throw Exception("Format error")
        }
        packagePart = zipPackage!!.getPart(relationship)
    }

    private fun processWorkbook() {
        getWorkBookSharedObjects()
        WorkbookReader.instance().read(zipPackage!!, packagePart!!, book!!, this)
    }

    private fun getWorkBookSharedObjects() {
        getPaletteColor()
        getThemeColor(packagePart!!)
        getStyles(packagePart!!)
        getSharedString(packagePart!!)
    }

    private fun getPaletteColor() {
        var palette: Palette? = Palette()
        var index = Palette.FIRST_COLOR_INDEX.toInt()
        var rgb = palette!!.getColor(index)
        while (rgb != null) {
            book!!.addColor(index++, ColorUtil.rgb(rgb[0], rgb[1], rgb[2]))
            rgb = palette.getColor(index)
        }
        palette.dispose()
        palette = null
    }

    private fun getThemeColor(documentPart: PackagePart) {
        val relationships = documentPart.getRelationshipsByType(PackageRelationshipTypes.THEME_PART)
        if (relationships.size() <= 0) {
            return
        }
        val relationship = relationships.getRelationship(0)
        val themePart = zipPackage!!.getPart(relationship.targetURI)
        ThemeColorReader.instance().getThemeColor(themePart, book!!)
    }

    private fun getSharedString(documentPart: PackagePart) {
        val relationships = documentPart.getRelationshipsByType(PackageRelationshipTypes.SHAREDSTRINGS_PART)
        if (relationships.size() <= 0) {
            return
        }
        val sharedStringsPart = zipPackage!!.getPart(relationships.getRelationship(0).targetURI)
        sharedStringIndex = 0
        val parser = Xml.newPullParser()
        val input = sharedStringsPart.inputStream
        parser.setInput(input, null)
        var inItem = false
        var captureText = false
        val text = StringBuilder()
        try {
            while (true) {
                // Activity.dispose() only flips the reader abort flag.  This
                // loop used to ignore that flag while sharedStrings.xml was
                // being consumed, so a cancelled 50MB workbook kept parsing
                // (and allocating) until it either finished or OOMed.
                if (isAborted()) throw AbortReaderError("abort Reader")
                when (parser.next()) {
                    XmlPullParser.END_DOCUMENT -> break
                    XmlPullParser.START_TAG -> when (parser.name) {
                        "si" -> {
                            inItem = true
                            text.setLength(0)
                        }
                        "t" -> if (inItem) captureText = true
                    }
                    XmlPullParser.TEXT, XmlPullParser.CDSECT -> if (captureText) text.append(parser.text)
                    XmlPullParser.END_TAG -> when (parser.name) {
                        "t" -> captureText = false
                        "si" -> {
                            if (isAborted()) throw AbortReaderError("abort Reader")
                            book!!.addSharedString(sharedStringIndex++, text.toString())
                            inItem = false
                        }
                    }
                }
            }
        } finally {
            input.close()
        }
    }

    private fun getStyles(documentPart: PackagePart) {
        val relationships = documentPart.getRelationshipsByType(PackageRelationshipTypes.STYLE_PART)
        if (relationships.size() <= 0) {
            return
        }
        val stylePart = zipPackage!!.getPart(relationships.getRelationship(0).targetURI)
        StyleReader.instance().getWorkBookStyle(stylePart, book!!, this)
    }

    override fun searchContent(file: File?, key: String): Boolean {
        if (file == null) return false
        val searchKey = key.lowercase()
        zipPackage = ZipPackage(file.absolutePath)
        val relationship = zipPackage!!.getRelationshipsByType(PackageRelationshipTypes.CORE_DOCUMENT).getRelationship(0)
        packagePart = zipPackage!!.getPart(relationship)
        val result = if (searchContentSharedString(packagePart!!, searchKey)) {
            true
        } else {
            WorkbookReader.instance().searchContent(zipPackage!!, this, packagePart!!, searchKey)
        }
        dispose()
        return result
    }

    private fun searchContentSharedString(documentPart: PackagePart, searchKey: String): Boolean {
        val relationships = documentPart.getRelationshipsByType(PackageRelationshipTypes.SHAREDSTRINGS_PART)
        if (relationships.size() <= 0) {
            return false
        }
        val sharedStringsPart = zipPackage!!.getPart(relationships.getRelationship(0).targetURI)
        this.key = searchKey
        searched = false
        val reader = SAXReader()
        try {
            reader.addHandler("/sst/si", SearchSharedStringSaxHandler())
            val input: InputStream = sharedStringsPart.inputStream
            reader.read(input)
            input.close()
        } catch (e: StopReaderError) {
            return true
        } finally {
            reader.resetHandlers()
        }
        return searched
    }

    override fun dispose() {
        super.dispose()
        filePath = null
        book = null
        zipPackage = null
        packagePart = null
        key = null
    }

    private inner class SharedStringSaxHandler : ElementHandler {
        override fun onStart(elementPath: ElementPath) {
        }

        override fun onEnd(elementPath: ElementPath) {
            if (abortReader) {
                throw AbortReaderError("abort Reader")
            }
            val element = elementPath.current
            if (element.name == "si") {
                val textElement = element.element("t")
                if (textElement != null) {
                    book!!.addSharedString(sharedStringIndex, textElement.text)
                } else {
                    // Do not retain the dom4j <si> tree for rich strings.
                    // A 50MB workbook can contain hundreds of thousands of
                    // these trees; keeping them makes the worksheet parse
                    // exceed Android's heap before cells are even read.
                    book!!.addSharedString(sharedStringIndex, element.getStringValue())
                }
                sharedStringIndex++
            }
            element.detach()
        }
    }

    private inner class SearchSharedStringSaxHandler : ElementHandler {
        override fun onStart(elementPath: ElementPath) {
        }

        override fun onEnd(elementPath: ElementPath) {
            if (abortReader) {
                throw AbortReaderError("abort Reader")
            }
            val stringItem = elementPath.current
            if (stringItem.name == "si") {
                val textElement = stringItem.element("t")
                if (textElement != null) {
                    if (textElement.text.lowercase().contains(key!!)) {
                        searched = true
                    }
                } else {
                    val iterator = stringItem.elementIterator("r")
                    var text = ""
                    while (iterator.hasNext()) {
                        val run = iterator.next() as Element
                        text += run.element("t").text
                    }
                    if (text.lowercase().contains(key!!)) {
                        searched = true
                    }
                }
            }
            stringItem.detach()
            if (searched) {
                throw StopReaderError("stop")
            }
        }
    }
}
