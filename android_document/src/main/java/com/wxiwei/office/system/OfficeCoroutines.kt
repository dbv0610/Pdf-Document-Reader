package com.wxiwei.office.system

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/**
 * Entry points onto a document's [SysKit.coroutineScope] for code that only holds an [IControl],
 * including the Java readers. Work launched here is cancelled when the document is disposed.
 */
object DocumentCoroutines {
    /** Already cancelled: work for a document that is gone must not start. */
    private val disposedScope = CoroutineScope(Job().apply { cancel() })

    /** The document scope of [control]; a cancelled one once the document is disposed. */
    @JvmStatic
    fun scopeOf(control: IControl?): CoroutineScope =
        try {
            control?.getSysKit()?.coroutineScope
        } catch (_: Exception) {
            // getSysKit() throws on a disposed control (mainControl!!).
            null
        } ?: disposedScope

    @JvmStatic
    fun launch(control: IControl?, task: Runnable): Job = scopeOf(control).launch { task.run() }

    fun launchSuspend(control: IControl?, block: suspend CoroutineScope.() -> Unit): Job =
        scopeOf(control).launch(block = block)

    /**
     * A scope of its own for a component (reader, layout) under [control]'s document scope:
     * cancelling it stops only that component, disposing the document stops it too. Without a
     * document to hang under it stands alone, as these scopes did before, rather than never
     * running: the component still cancels it itself in dispose().
     */
    fun childScope(control: IControl?, dispatcher: CoroutineDispatcher = Dispatchers.Default): CoroutineScope {
        val parent = scopeOf(control).coroutineContext
        if (parent[Job]?.isActive != true) {
            OpenTrace.e("no live document scope for ${control?.javaClass?.simpleName}; using a standalone one")
            return CoroutineScope(
                SupervisorJob() + dispatcher +
                    CoroutineExceptionHandler { _, error -> OpenTrace.e("standalone coroutine failed", error) }
            )
        }
        return CoroutineScope(parent + SupervisorJob(parent[Job]) + dispatcher)
    }
}

/**
 * Process-wide background work that must outlive a document, e.g. deleting its temp files while
 * it is being disposed, or searching the file system. Document work goes to [DocumentCoroutines].
 */
object OfficeCoroutineExecutor {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    @JvmStatic
    fun launch(task: Runnable): Job = scope.launch {
        task.run()
    }

    fun launchSuspend(block: suspend () -> Unit): Job =
        scope.launch { block() }
}
