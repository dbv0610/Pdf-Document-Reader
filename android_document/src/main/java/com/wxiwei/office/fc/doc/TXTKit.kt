/*
 * 文件名称: TXTKit.java
 */
package com.wxiwei.office.fc.doc

import android.os.Handler
import com.wxiwei.office.fc.fs.storage.HeaderBlock
import com.wxiwei.office.fc.fs.storage.LittleEndian
import com.wxiwei.office.system.FileReaderThread
import com.wxiwei.office.system.IControl
import com.wxiwei.office.system.OpenTrace
import java.io.FileInputStream

class TXTKit {
    companion object {
        private val kit = TXTKit()

        @JvmStatic
        fun instance(): TXTKit {
            return kit
        }
    }

    fun readText(control: IControl, handler: Handler, filePath: String) {
        try {
            val input = FileInputStream(filePath)
            val bytes = ByteArray(16)
            input.read(bytes)
            var signature = LittleEndian.getLong(bytes, 0)
            if (signature == HeaderBlock._signature || signature == 0x0006001404034b50L) {
                input.close()
                OpenTrace.e("text reader rejected office container path=$filePath")
                handler.obtainMessage(com.wxiwei.office.constant.MainConstant.HANDLER_MESSAGE_ERROR, com.wxiwei.office.system.BadFileFormatException("Office container opened as text")).sendToTarget()
                return
            }
            signature = signature and 0x00FFFFFFFFFFFFFFL
            if (signature == 0x002e312d46445025L) {
                input.close()
                OpenTrace.e("text reader rejected PDF container path=$filePath")
                handler.obtainMessage(com.wxiwei.office.constant.MainConstant.HANDLER_MESSAGE_ERROR, com.wxiwei.office.system.BadFileFormatException("PDF opened as text")).sendToTarget()
                return
            }
            input.close()

            // TODO(coroutine): Starts the file reader worker; use an IO dispatcher in a unified refactor.
            FileReaderThread(control, handler, filePath, "UTF-8").start()
        } catch (e: Throwable) {
            OpenTrace.e("text reader preflight failed path=$filePath", e)
            handler.obtainMessage(com.wxiwei.office.constant.MainConstant.HANDLER_MESSAGE_ERROR, e).sendToTarget()
        }
    }

    fun reopenFile(control: IControl, handler: Handler, filePath: String, encode: String) {
        FileReaderThread(control, handler, filePath, encode).start()
    }
}
