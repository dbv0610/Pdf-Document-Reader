/*
 * Copyright (c) 2026 dongb2002. All rights reserved.
 *
 * Proprietary and confidential. Unauthorized copying, modification or distribution of this
 * file, via any medium, is strictly prohibited without the written permission of dongb2002.
 */
package com.wxiwei.office.editor

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.wxiwei.office.editor.OpenDocument.onMain
import com.wxiwei.office.editor.docx.LiveDocxSession
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

/** A live edit lays the pages after it out again: the page count shown must not drop and grow back meanwhile. */
@RunWith(AndroidJUnit4::class)
class PageCountTest {
    private fun countsWhileEditing(name: String, at: (com.wxiwei.office.reader.OfficeReader) -> Long) {
        val source = OpenDocument.copySample("doc_test.docx", "docx_pagecount_$name.docx")
        OpenDocument.open(source, { it.layout != null }) { reader ->
            // the last count may still be held back by the reader's publish throttle
            delay(1500)
            val before = reader.state.value.pageCount
            assertTrue("several pages", before > 2)
            val session = onMain { LiveDocxSession(reader.control!!, source) }
            val offset = at(reader)
            val seen = ArrayList<Int>()
            coroutineScope {
                val job = launch { reader.state.collect { synchronized(seen) { seen.add(it.pageCount) } } }
                assertTrue(session.lastError?.toString(), onMain { session.insertText(offset, "x") })
                delay(4000)
                job.cancel()
            }
            android.util.Log.i("PageCountTest", "$name: $before -> $seen")
            assertEquals("counts published after the edit", listOf(before), seen.distinct())
        }
    }

    @Test fun editOnFirstPage() = countsWhileEditing("first") { 0L }

    /** In the middle of the document: a paragraph start there. */
    @Test fun editInTheMiddle() = countsWhileEditing("middle") { reader ->
        onMain {
            val doc = (reader.control!!.getView() as com.wxiwei.office.wp.control.Word).getDocument()
            doc.getParagraph(doc.getAreaEnd(0) / 2)!!.getStartOffset()
        }
    }
}
