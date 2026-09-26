package com.wxiwei.office.fc.openxml4j.opc

import com.wxiwei.office.fc.openxml4j.exceptions.OpenXML4JRuntimeException
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.util.zip.ZipException

class ZipPackageTest {
    @get:Rule val temporaryFolder = TemporaryFolder()

    @Test fun placeholderDocxReportsFormatErrorAndPreservesZipFailure() {
        val file = temporaryFolder.newFile("50mb.docx")
        file.writeText("examplefile.com ")
        try {
            ZipPackage(file.absolutePath)
            fail("Placeholder content must not open as DOCX")
        } catch (error: OpenXML4JRuntimeException) {
            assertTrue(error.message.orEmpty().contains("Format error"))
            assertTrue(error.cause is ZipException)
        }
    }

    @Test fun emptyDocxReportsFormatErrorWithCause() {
        val file = temporaryFolder.newFile("empty.docx")
        try {
            ZipPackage(file.absolutePath)
            fail("Empty content must not open as DOCX")
        } catch (error: OpenXML4JRuntimeException) {
            assertTrue(error.message.orEmpty().contains("Format error"))
            assertNotNull(error.cause)
        }
    }
}
