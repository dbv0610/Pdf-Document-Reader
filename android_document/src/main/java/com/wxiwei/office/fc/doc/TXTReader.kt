/*
 * 文件名称: TXTReader.java
 */
package com.wxiwei.office.fc.doc

import com.wxiwei.office.constant.MainConstant
import com.wxiwei.office.constant.wp.WPModelConstant
import com.wxiwei.office.simpletext.model.AttrManage
import com.wxiwei.office.simpletext.model.IDocument
import com.wxiwei.office.simpletext.model.LeafElement
import com.wxiwei.office.simpletext.model.ParagraphElement
import com.wxiwei.office.simpletext.model.SectionElement
import com.wxiwei.office.system.AbstractReader
import com.wxiwei.office.system.IControl
import com.wxiwei.office.wp.model.WPDocument
import java.io.BufferedReader
import java.io.File
import java.io.FileInputStream
import java.io.InputStreamReader

class TXTReader(control: IControl?, filePath: String?, encoding: String?) : AbstractReader() {
    private var offset: Long = 0
    private var filePath: String? = filePath
    private var encoding: String? = encoding
    private var wpdoc: IDocument? = null

    init {
        this.control = control
    }

    fun authenticate(password: String?): Boolean {
        if (encoding != null) {
            return true
        }
        encoding = password
        if (encoding != null) {
            try {
                control?.actionEvent(MainConstant.HANDLER_MESSAGE_SUCCESS, getModel())
                return true
            } catch (e: Throwable) {
                control?.getSysKit()?.getErrorKit()?.writerLog(e)
            }
        }
        return false
    }

    override fun getModel(): Any? {
        if (wpdoc != null) {
            return wpdoc
        }
        wpdoc = WPDocument()
        if (encoding != null) {
            readFile()
        }
        return wpdoc
    }

    fun readFile() {
        val section = SectionElement()
        val attr = section.getAttribute()
        AttrManage.instance().setPageWidth(attr, 11906)
        AttrManage.instance().setPageHeight(attr, 16838)
        AttrManage.instance().setPageMarginLeft(attr, 1800)
        AttrManage.instance().setPageMarginRight(attr, 1800)
        AttrManage.instance().setPageMarginTop(attr, 1440)
        AttrManage.instance().setPageMarginBottom(attr, 1440)
        section.setStartOffset(offset)

        val file = File(filePath!!)
        val reader = BufferedReader(InputStreamReader(FileInputStream(file), encoding!!))
        var line: String?
        while (true) {
            line = reader.readLine()
            if (line == null && offset != 0L) {
                break
            }
            if (abortReader) {
                break
            }
            var text = if (line == null) "\n" else "$line\n"
            text = text.replace('\t', ' ')
            val length = text.length
            if (length > 500) {
                var end = 200
                var start = 0
                while (end <= length) {
                    val value = text.substring(start, end) + "\n"
                    appendParagraph(value)
                    if (end == length) {
                        break
                    }
                    start = end
                    end += 100
                    if (end > length) {
                        end = length
                    }
                }
            } else {
                appendParagraph(text)
            }
        }
        reader.close()
        section.setEndOffset(offset)
        (wpdoc as WPDocument).appendSection(section)
    }

    private fun appendParagraph(text: String) {
        val paragraph = ParagraphElement()
        paragraph.setStartOffset(offset)
        val leaf = LeafElement(text)
        leaf.setStartOffset(offset)
        offset += text.length
        leaf.setEndOffset(offset)
        paragraph.appendLeaf(leaf)
        paragraph.setEndOffset(offset)
        (wpdoc as WPDocument).appendParagraph(paragraph, WPModelConstant.MAIN)
    }

    override fun searchContent(file: File?, key: String): Boolean {
        if (file == null) return false
        val reader = BufferedReader(InputStreamReader(FileInputStream(file)))
        var line: String?
        while (reader.readLine().also { line = it } != null) {
            if (line != null && line.contains(key)) {
                return true
            }
        }
        return false
    }

    override fun dispose() {
        if (isReaderFinish()) {
            wpdoc = null
            filePath = null
            control = null
        }
    }
}
