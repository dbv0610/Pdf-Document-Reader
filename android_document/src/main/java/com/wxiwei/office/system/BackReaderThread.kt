package com.wxiwei.office.system

import com.wxiwei.office.constant.EventConstant

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

/** Coroutine-backed replacement for the legacy polling thread. */
class BackReaderThread(
    private var reader: IReader?,
    private var control: IControl?
) {
    private val scope = DocumentCoroutines.childScope(control)
    private var job: Job? = null

    @Synchronized
    fun start(): Job {
        job?.cancel()
        val currentReader = reader
        val currentControl = control
        return scope.launch {
            currentControl?.actionEvent(EventConstant.SYS_START_BACK_READER_ID, true)
            try {
                while (currentCoroutineContext().isActive && currentReader != null && currentControl != null) {
                    if (!currentReader.isReaderFinish()) {
                        currentReader.backReader()
                        delay(50)
                    } else {
                        currentControl.actionEvent(EventConstant.SYS_READER_FINSH_ID, true)
                        clearReferences()
                        break
                    }
                }
            } catch (_: CancellationException) {
                currentReader?.abortReader()
            } catch (e: OutOfMemoryError) {
                currentControl?.getSysKit()?.getErrorKit()?.writerLog(e, true)
                currentControl?.actionEvent(EventConstant.SYS_READER_FINSH_ID, true)
                clearReferences()
            } catch (e: Exception) {
                if (currentReader != null && !currentReader.isAborted()) {
                    currentControl?.getSysKit()?.getErrorKit()?.writerLog(e, true)
                    currentControl?.actionEvent(EventConstant.SYS_READER_FINSH_ID, true)
                    clearReferences()
                }
            }
        }.also { job = it }
    }

    @Synchronized
    fun setDie(die: Boolean) {
        if (die) {
            job?.cancel()
            job = null
        }
    }

    @Synchronized
    fun cancel() {
        job?.cancel()
        job = null
    }

    @Synchronized
    fun dispose() {
        cancel()
        scope.cancel()
        clearReferences()
    }

    private fun clearReferences() {
        control = null
        reader = null
    }
}
