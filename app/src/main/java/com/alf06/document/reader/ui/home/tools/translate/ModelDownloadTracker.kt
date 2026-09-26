package com.alf06.document.reader.ui.home.tools.translate

import android.app.DownloadManager
import android.content.Context
import android.util.Log

internal class ModelDownloadTracker(context: Context, private val models: Int) {
    private val downloads = context.getSystemService(DownloadManager::class.java)
    private val before: Set<Long> = query().keys

    private val seen = mutableMapOf<Long, Float>()

    fun percent(): Int {
        val current = query()
        seen.keys.forEach { id ->

            if (id !in current) seen[id] = 1f
        }
        current.forEach { (id, fraction) -> if (id !in before) seen[id] = fraction }
        return (seen.values.sum() / models.coerceAtLeast(1) * 100).toInt().coerceIn(0, 100)
    }

    private fun query(): Map<Long, Float> = try {
        downloads?.query(DownloadManager.Query())?.use { cursor ->
            val idColumn = cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_ID)
            val statusColumn = cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_STATUS)
            val doneColumn = cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR)
            val totalColumn = cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_TOTAL_SIZE_BYTES)
            buildMap {
                while (cursor.moveToNext()) {
                    val total = cursor.getLong(totalColumn)
                    put(
                        cursor.getLong(idColumn),
                        when (cursor.getInt(statusColumn)) {
                            DownloadManager.STATUS_SUCCESSFUL -> 1f
                            DownloadManager.STATUS_FAILED -> 0f

                            else -> if (total > 0) cursor.getLong(doneColumn).toFloat() / total else 0f
                        }
                    )
                }
            }
        }.orEmpty()
    } catch (e: Exception) {
        Log.w(TAG, "Cannot read download progress", e)
        emptyMap()
    }

    private companion object {
        const val TAG = "ModelDownloadTracker"
    }
}
