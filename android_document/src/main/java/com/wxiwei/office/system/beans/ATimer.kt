package com.wxiwei.office.system.beans

import com.wxiwei.office.system.DocumentCoroutines
import com.wxiwei.office.system.IControl
import com.wxiwei.office.system.ITimerListener
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

/**
 * Calls [listener] every [delay] ms until stopped, as a coroutine in [control]'s document scope,
 * so disposing the document stops it. Runs off the main thread, like the java.util.Timer it replaces.
 */
class ATimer(
    private var delay: Int,
    private var listener: ITimerListener?,
    private val control: IControl?,
) {
    @JvmField
    @Volatile
    var isRunning = false
    private var job: Job? = null

    @Synchronized
    fun start() {
        if (isRunning) {
            return
        }
        isRunning = true
        // Looked up here, not when constructed: owners build their timer before the document is wired.
        job = DocumentCoroutines.scopeOf(control).launch {
            while (isActive) {
                delay(delay.toLong())
                try {
                    listener?.actionPerformed()
                } catch (_: Exception) {
                }
            }
        }
    }

    fun isRunning(): Boolean {
        return isRunning
    }

    /** Also safe from inside actionPerformed: the loop ends at its next delay. */
    @Synchronized
    fun stop() {
        if (isRunning) {
            job?.cancel()
            job = null
            isRunning = false
        }
    }

    @Synchronized
    fun restart() {
        stop()
        start()
    }

    @Synchronized
    fun dispose() {
        stop()
        listener = null
    }
}
