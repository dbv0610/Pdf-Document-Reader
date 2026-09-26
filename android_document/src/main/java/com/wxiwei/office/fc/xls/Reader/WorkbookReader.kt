package com.wxiwei.office.fc.xls.Reader

import android.os.Message
import com.wxiwei.office.constant.MainConstant
import com.wxiwei.office.fc.dom4j.*
import com.wxiwei.office.fc.dom4j.io.SAXReader
import com.wxiwei.office.fc.openxml4j.opc.*
import com.wxiwei.office.fc.xls.SSReader
import com.wxiwei.office.ss.model.baseModel.Sheet
import com.wxiwei.office.ss.model.baseModel.Workbook
import com.wxiwei.office.system.AbortReaderError
import com.wxiwei.office.system.IControl
import com.wxiwei.office.system.IReader
import com.wxiwei.office.system.ReaderHandler
import com.wxiwei.office.system.sysKit
import com.wxiwei.office.system.DocumentCoroutines
import com.wxiwei.office.system.OpenTrace
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.delay
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.withContext
import kotlin.coroutines.coroutineContext

class WorkbookReader private constructor() {
    companion object {
        private const val WINDOWWIDTH = 2
        private val reader = WorkbookReader()
        @JvmStatic fun instance(): WorkbookReader = reader
    }

    private var zipPackage: ZipPackage? = null
    private var book: Workbook? = null
    private var iReader: SSReader? = null
    private var sheetIndexList: MutableMap<Int, String>? = null
    private var sheetNameList: MutableMap<String, String>? = null
    private var tempIndex = 0
    private var worksheetRelCollection: PackageRelationshipCollection? = null
    private var chartsheetRelCollection: PackageRelationshipCollection? = null
    private var sheetJob: Job? = null

    @Throws(Exception::class)
    fun read(zipPackage: ZipPackage, packagePart: PackagePart, book: Workbook, iReader: SSReader) {
        this.zipPackage = zipPackage
        this.book = book
        this.iReader = iReader
        getSheetsProp(packagePart)
        val indexes = sheetIndexList!!
        val names = sheetNameList!!
        for (i in 0 until indexes.size) {
            val sheet = Sheet()
            sheet.setWorkbook(book)
            val id = indexes[i]!!
            sheet.setSheetName(names[id])
            book.addSheet(i, sheet)
        }
        worksheetRelCollection = packagePart.getRelationshipsByType(PackageRelationshipTypes.WORKSHEET_PART)
        chartsheetRelCollection = packagePart.getRelationshipsByType(PackageRelationshipTypes.CHARTSHEET_PART)

        // getModel() returns before the UI is constructed.  Parse the first
        // worksheet synchronously on the reader's IO thread so the initial
        // SheetView receives real rows/cells instead of an empty model.
        if (indexes.isNotEmpty()) {
            readSheet(iReader.getControl(), 0)
        }
        book.debugDump("xlsx-after-first-sheet")

        class WorkbookReaderHandler(private var control: IControl, private var currentReader: WorkbookReader?) : ReaderHandler() {
            override fun handleMessage(msg: Message) = synchronized(this@WorkbookReader) {
                when (msg.what) {
                    MainConstant.HANDLER_MESSAGE_SUCCESS -> {
                        val previous = sheetJob
                        sheetJob = DocumentCoroutines.launchSuspend(control) {
                            val job = coroutineContext[Job]
                            try {
                                // Preserve the join chain even when another sheet replaces this job.
                                withContext(NonCancellable) { previous?.cancelAndJoin() }
                                coroutineContext.ensureActive()
                                OpenTrace.d("excel.job start sheet=${msg.obj}")
                                currentReader?.readSheetInSlideWindow(control, msg.obj as Int)
                            } catch (e: CancellationException) {
                                OpenTrace.d("excel.job cancel sheet=${msg.obj}")
                                throw e
                            } catch (e: AbortReaderError) {
                                OpenTrace.d("excel.job cancel sheet=${msg.obj} aborted=true")
                            } catch (e: OutOfMemoryError) {
                                OpenTrace.e("excel.job fail sheet=${msg.obj}", e)
                                control.sysKit.errorKit.writerLog(e, true)
                                synchronized(this@WorkbookReader) {
                                    if (sheetJob === job) currentReader?.dispose()
                                }
                            } catch (e: Exception) {
                                OpenTrace.e("excel.job fail sheet=${msg.obj}", e)
                                control.sysKit.errorKit.writerLog(e, true)
                                synchronized(this@WorkbookReader) {
                                    if (sheetJob === job) currentReader?.dispose()
                                }
                            }
                        }
                    }
                    MainConstant.HANDLER_MESSAGE_ERROR, MainConstant.HANDLER_MESSAGE_DISPOSE -> {
                        sheetJob?.cancel()
                        sheetJob = null
                        dispose()
                        currentReader = null
                    }
                }
            }
        }
        val handler = WorkbookReaderHandler(iReader.getControl(), this)
        book.setReaderHandler(handler)
        val msg = Message()
        msg.what = MainConstant.HANDLER_MESSAGE_SUCCESS
        msg.obj = 0
        handler.handleMessage(msg)
    }

    @Throws(Exception::class)
    private suspend fun readSheetInSlideWindow(control: IControl, currentSheet: Int) {
        val workbook = book ?: return
        synchronized(workbook) {
            iReader?.abortCurrentReading()
            for (i in currentSheet - WINDOWWIDTH..currentSheet + WINDOWWIDTH) {
                if (i >= 0 && workbook.getSheet(i) != null && !workbook.getSheet(i)!!.isAccomplished()) workbook.getSheet(i)!!.setState(Sheet.State_Reading)
            }
        }
        delay(50)
        if (currentSheet >= 0 && workbook.getSheet(currentSheet) != null) {
            val active = workbook.getSheet(currentSheet)!!
            while (!active.isAccomplished()) {
                coroutineContext.ensureActive()
                readSheet(control, currentSheet)
                if (!active.isAccomplished()) {
                    // Recalculate the scrollable extent as new rows arrive;
                    // this also lets the user scroll while the tail is loading.
                    active.setState(Sheet.State_Reading)
                    active.notifyReadingProgress()
                    delay(16)
                }
            }
        }
        for (i in currentSheet - WINDOWWIDTH..currentSheet + WINDOWWIDTH) {
            if (i >= 0 && i != currentSheet && workbook.getSheet(i) != null && !workbook.getSheet(i)!!.isAccomplished()) {
                while (!workbook.getSheet(i)!!.isAccomplished()) {
                    coroutineContext.ensureActive()
                    readSheet(control, i)
                    if (!workbook.getSheet(i)!!.isAccomplished()) workbook.getSheet(i)!!.setState(Sheet.State_Reading)
                }
            }
        }
    }

    @Throws(Exception::class)
    private fun readSheet(control: IControl, index: Int) {
        val relId = sheetIndexList!![index] ?: return
        var rel = worksheetRelCollection?.getRelationshipByID(relId)
        var type = Sheet.TYPE_WORKSHEET
        if (rel == null) {
            rel = chartsheetRelCollection?.getRelationshipByID(relId)
            type = Sheet.TYPE_CHARTSHEET
        }
        if (rel == null) return
        val part = zipPackage!!.getPart(rel.targetURI) ?: return
        book!!.getSheet(index)!!.setSheetType(type)
        val sheet = book!!.getSheet(index)!!
        if (SheetReader.instance().isStreaming(sheet)) {
            SheetReader.instance().continueSheet(control, zipPackage!!, sheet, part, iReader!!)
        } else {
            SheetReader.instance().getSheet(control, zipPackage!!, sheet, part, iReader!!)
        }
    }

    @Throws(Exception::class)
    private fun getSheetsProp(documentPart: PackagePart) {
        sheetIndexList?.clear() ?: run { sheetIndexList = HashMap(5) }
        sheetNameList?.clear() ?: run { sheetNameList = HashMap(5) }
        tempIndex = 0
        val saxReader = SAXReader()
        try {
            val handler = WorkBookSaxHandler()
            saxReader.addHandler("/workbook/workbookPr", handler)
            saxReader.addHandler("/workbook/sheets/sheet", handler)
            val input = documentPart.inputStream
            saxReader.read(input)
            input.close()
        } finally {
            saxReader.resetHandlers()
        }
    }

    @Throws(Exception::class)
    fun searchContent(zipPackage: ZipPackage, iReader: IReader, packagePart: PackagePart, key: String): Boolean {
        if (searchContentSheetName(packagePart, key)) return true
        this.zipPackage = zipPackage
        worksheetRelCollection = packagePart.getRelationshipsByType(PackageRelationshipTypes.WORKSHEET_PART)
        for (i in 0 until worksheetRelCollection!!.size()) {
            if (searchContentSheet(iReader, worksheetRelCollection!!.getRelationship(i), key)) {
                dispose()
                return true
            }
        }
        return false
    }

    @Throws(Exception::class)
    private fun searchContentSheetName(documentPart: PackagePart, key: String): Boolean {
        val input = documentPart.inputStream
        val root = SAXReader().read(input).rootElement
        input.close()
        val iterator = root.element("sheets").elementIterator()
        while (iterator.hasNext()) {
            val element = iterator.next() as Element
            if (element.attributeValue("name").lowercase().contains(key)) return true
        }
        return false
    }

    @Throws(Exception::class)
    private fun searchContentSheet(iReader: IReader, relation: PackageRelationship, key: String): Boolean {
        val part = zipPackage!!.getPart(relation.targetURI) ?: return false
        return SheetReader.instance().searchContent(zipPackage!!, iReader, part, key)
    }

    @Synchronized
    fun dispose() {
        cancelReading()
        SheetReader.instance().dispose()
        zipPackage = null
        book = null
        iReader = null
        sheetNameList?.clear()
        sheetNameList = null
        sheetIndexList?.clear()
        sheetIndexList = null
        worksheetRelCollection?.clear()
        worksheetRelCollection = null
        chartsheetRelCollection?.clear()
        chartsheetRelCollection = null
    }

    /**
     * Cancels only the asynchronous/progressive sheet reader. The model is
     * intentionally kept alive because this is also called by abortReader()
     * while the Activity is being torn down.
     */
    @Synchronized
    fun cancelReading() {
        sheetJob?.cancel()
        // Keep the job so its replacement can join it before using the stream.
    }

    private inner class WorkBookSaxHandler : ElementHandler {
        override fun onStart(elementPath: ElementPath) {}
        override fun onEnd(elementPath: ElementPath) {
            if (iReader?.isAborted() == true) throw AbortReaderError("abort Reader")
            val element = elementPath.current
            when (element.name) {
                "sheet" -> {
                    val id = element.attributeValue("id")
                    val name = element.attributeValue("name")
                    sheetIndexList!![tempIndex++] = id
                    sheetNameList!![id] = name
                }
                "workbookPr" -> book?.setUsing1904DateWindowing(element.attributeValue("date1904")?.let { it.toInt() != 0 } ?: false)
            }
            element.detach()
        }
    }
}
