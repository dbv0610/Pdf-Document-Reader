package com.wxiwei.office.system

import android.app.Activity
import android.app.AlertDialog
import android.content.DialogInterface
import android.util.Log
import com.wxiwei.office.common.ICustomDialog
import com.wxiwei.office.constant.EventConstant
import java.io.File
import java.io.FileWriter
import java.io.PrintWriter
import java.text.SimpleDateFormat
import java.util.Calendar

class ErrorUtil(sysKitValue: SysKit) {
    private var sysKit: SysKit? = sysKitValue
    private var logFile: File? = null
    private var message: AlertDialog? = null

    init {
        if (sysKitValue.getControl().getMainFrame().isWriteLog()) {
            logFile = sysKitValue.getControl().getMainFrame().getTemporaryDirectory()
            val file = logFile
            if (file != null && file.exists() && file.canWrite()) {
                logFile = File(file.absolutePath + File.separatorChar + "ASReader")
            }
            if (logFile != null) {
                if (!logFile!!.exists()) logFile!!.mkdirs()
                logFile = File(logFile!!.absolutePath + File.separatorChar + "errorLog.txt")
            }
        }
    }

    fun writerLog(ex: Throwable) {
        writerLog(ex, false)
    }

    fun writerLog(ex: Throwable, isReaderFile: Boolean) {
        writerLog(ex, isReaderFile, true)
    }

    fun writerLog(exValue: Throwable, isReaderFile: Boolean, isShowErrorDialog: Boolean) {
        val ex = exValue
        if (OpenFileErrors.isCancellation(ex)) return
        try {
            val file = logFile
            if (file != null && sysKit?.getControl()?.getMainFrame()?.isWriteLog() == true && ex !is OutOfMemoryError) {
                PrintWriter(FileWriter(file, true), true).use { printWriter ->
                    printWriter.println()
                    printWriter.println("--------------------------------------------------------------------------")
                    printWriter.println("Exception occurs: ${sdf24.format(Calendar.getInstance().time)}  $VERSION")
                    ex.printStackTrace(printWriter)
                }
            }
        } catch (e: OutOfMemoryError) {
            // Logging must never close the host or replace the original open error.
        } catch (e: Exception) {
            Log.e("ErrorUtil", "Could not write error log", e)
        }
        if (isShowErrorDialog) processThrowable(ex, isReaderFile)
    }

    private fun processThrowable(ex: Throwable, isReaderFile: Boolean) {
        val kit = sysKit ?: return
        val control = kit.getControl()
        val activity: Activity = control.getMainFrame().getActivity()
        if (control.isAutoTest()) {
            System.exit(0)
        } else if (message == null) {
            control.getActivity().window.decorView.post {
                try {
                    val classification = OpenFileErrors.details(ex, isReaderFile, kit.isDebug())
                    val err = classification.resource?.let { control.getMainFrame().getLocalString(it) }
                        ?: classification.message
                    val errorCode = classification.errorCode
                    if (errorCode == PARSE_ERROR) Log.e("DIALOG_PARSE_ERROR", "DIALOG_PARSE_ERROR")
                    if (err.isNotEmpty() && errorCode != SYSTEM_PERMISSION) {
                        control.getMainFrame().error(errorCode)
                        control.actionEvent(EventConstant.APP_ABORTREADING, true)
                        if (control.getMainFrame().isPopUpErrorDlg() && message == null) {
                            val builder = AlertDialog.Builder(activity)
                            builder.setMessage(err).setCancelable(false).setTitle(control.getMainFrame().getAppName())
                            val ok = control.getMainFrame().getLocalString("BUTTON_OK")
                            builder.setPositiveButton(ok) { _, _ -> message = null; activity.finish() }
                            message = builder.create()
                            message!!.show()
                        } else {
                            control.getCustomDialog()?.showDialog(ICustomDialog.DIALOGTYPE_ERROR)
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    fun dispose() {
        sysKit = null
    }

    companion object {
        const val INSUFFICIENT_MEMORY = 0
        const val SYSTEM_CRASH = 1
        const val SYSTEM_PERMISSION = 10
        const val BAD_FILE = 2
        const val OLD_DOCUMENT = 3
        const val PARSE_ERROR = 4
        const val RTF_DOCUMENT = 5
        const val PASSWORD_DOCUMENT = 6
        const val PASSWORD_INCORRECT = 7
        const val SD_CARD_ERROR = 8
        const val SD_CARD_WRITEDENIED = 9
        const val SD_CARD_NOSPACELEFT = 10
        private val sdf24 = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
        private const val VERSION = "2.0.0.4"
    }
}
