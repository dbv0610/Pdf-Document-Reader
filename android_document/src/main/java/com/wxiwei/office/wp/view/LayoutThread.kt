package com.wxiwei.office.wp.view

import com.wxiwei.office.system.*

import android.util.Log
import com.wxiwei.office.simpletext.control.IWord
import com.wxiwei.office.simpletext.view.IRoot
import com.wxiwei.office.simpletext.view.IView
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class LayoutThread(private var root: IRoot?) {
    /** Made on first start: the root is not attached to its document when this is constructed. */
    private var scope: CoroutineScope? = null
    private var layoutJob: Job? = null

    @Synchronized
    fun start() {
        if (layoutJob?.isActive == true) {
            Log.d("OfficePageLayout", "layout coroutine already active")
            return
        }
        val scope = scope ?: DocumentCoroutines.childScope((root as? IView)?.getControl()).also { scope = it }
        layoutJob = scope.launch {
            try {
                while (isActive) {
                    val currentRoot = root ?: break
                    if (currentRoot.canBackLayout()) {
                        Log.d("OfficePageLayout", "layout coroutine backLayout")
                        currentRoot.backLayout()
                        delay(50L)
                    } else {
                        delay(1000L)
                    }
                }
            } catch (e: CancellationException) {
                Log.d("OfficePageLayout", "layout coroutine cancelled")
                throw e
            } catch (e: Exception) {
                val word = (root as? IView)?.getContainer()
                val control = word?.getControl()
                if (word != null && control != null) {
                    control.getSysKit().getErrorKit().writerLog(e)
                }
            }
        }
    }

    @Synchronized
    fun setDied(isDied: Boolean) {
        if (isDied) dispose()
    }

    @Synchronized
    fun dispose() {
        Log.d("OfficePageLayout", "dispose layout coroutine")
        layoutJob?.cancel()
        layoutJob = null
        scope?.cancel()
        scope = null
        root = null
    }
}
