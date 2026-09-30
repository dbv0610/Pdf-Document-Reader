package com.reader.pdfviewer.tools

import org.apache.pdfbox.Loader
import org.apache.pdfbox.pdfwriter.compress.CompressParameters
import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.pdmodel.PDPage
import org.apache.pdfbox.pdmodel.PDPageContentStream
import org.apache.pdfbox.pdmodel.common.PDRectangle
import org.apache.pdfbox.pdmodel.encryption.InvalidPasswordException
import org.apache.pdfbox.pdmodel.font.PDType1Font
import org.apache.pdfbox.pdmodel.font.Standard14Fonts
import org.apache.pdfbox.text.PDFTextStripper
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File
import java.io.FileOutputStream
import java.util.concurrent.CancellationException

/** Encrypted copies made by PdfEncryptor, read back with Apache PDFBox. */
class PdfEncryptorTest {
    @get:Rule val folder = TemporaryFolder()

    private val title = "Tiêu đề (bí mật) \\ end"

    /** One page saying "Private text", titled [title]; [compress]: object streams and a cross-reference stream. */
    private fun plain(name: String, compress: Boolean): File {
        val file = folder.newFile(name)
        PDDocument().use { pd ->
            val page = PDPage(PDRectangle.A4)
            pd.addPage(page)
            PDPageContentStream(pd, page).use { cs ->
                cs.beginText()
                cs.setFont(PDType1Font(Standard14Fonts.FontName.HELVETICA), 12f)
                cs.newLineAtOffset(50f, 700f)
                cs.showText("Private text")
                cs.endText()
            }
            pd.documentInformation.title = title
            pd.save(file, if (compress) CompressParameters.DEFAULT_COMPRESSION else CompressParameters.NO_COMPRESSION)
        }
        return file
    }

    private fun encrypted(source: File, rights: Int = PdfEncryptor.ALL, user: String = "1234", owner: String = "owner"): File {
        val out = File(folder.root, source.nameWithoutExtension + "-locked.pdf")
        PdfEncryptor.encrypt(source, out, user, owner, rights)
        return out
    }

    private fun assertOpens(file: File, password: String = "1234") {
        Loader.loadPDF(file, password).use { pd ->
            assertTrue(pd.isEncrypted)
            assertEquals(5, pd.encryption.version)
            assertEquals(6, pd.encryption.revision)
            assertTrue(PDFTextStripper().getText(pd).contains("Private text"))
            assertEquals(title, pd.documentInformation.title)
        }
    }

    @Test fun classicCrossReference() {
        val source = plain("classic.pdf", compress = false)
        assertFalse(source.readText(Charsets.ISO_8859_1).contains("/ObjStm"))
        assertOpens(encrypted(source))
    }

    @Test fun objectStreams() {
        val source = plain("compressed.pdf", compress = true)
        val raw = source.readText(Charsets.ISO_8859_1)
        assertTrue("the source has object streams", raw.contains("/ObjStm") && raw.contains("/XRef"))
        assertOpens(encrypted(source))
    }

    @Test fun incrementalUpdate() {
        val source = plain("updated.pdf", compress = false)
        Loader.loadPDF(source).use { pd ->
            pd.documentInformation.title = "New title"
            pd.documentInformation.cosObject.isNeedToBeUpdated = true
            pd.documentCatalog.cosObject.isNeedToBeUpdated = true
            val updated = File(folder.root, "updated2.pdf")
            FileOutputStream(updated).use { out -> pd.saveIncremental(out) }
            assertTrue("the update has a /Prev", updated.readText(Charsets.ISO_8859_1).contains("/Prev"))
            Loader.loadPDF(encrypted(updated), "1234").use { locked ->
                assertEquals("New title", locked.documentInformation.title)
                assertTrue(PDFTextStripper().getText(locked).contains("Private text"))
            }
        }
    }

    @Test fun wrongOrNoPassword() {
        val locked = encrypted(plain("wrong.pdf", compress = false))
        assertTrue(runCatching { Loader.loadPDF(locked, "12345").close() }.exceptionOrNull() is InvalidPasswordException)
        assertTrue(runCatching { Loader.loadPDF(locked).close() }.exceptionOrNull() is InvalidPasswordException)
    }

    @Test fun permissions() {
        val rights = PdfEncryptor.ALL and (PdfEncryptor.PRINT or PdfEncryptor.PRINT_HIGH or PdfEncryptor.COPY).inv()
        val locked = encrypted(plain("rights.pdf", compress = true), rights)
        Loader.loadPDF(locked, "1234").use { pd ->
            val p = pd.currentAccessPermission
            assertFalse(p.isOwnerPermission)
            assertFalse(p.canPrint())
            assertFalse(p.canPrintFaithful())
            assertFalse(p.canExtractContent())
            assertTrue(p.canModify())
            assertTrue(p.canFillInForm())
            assertTrue(p.canExtractForAccessibility())
        }
        Loader.loadPDF(locked, "owner").use { pd ->
            assertTrue(pd.currentAccessPermission.isOwnerPermission)
            assertTrue(PDFTextStripper().getText(pd).contains("Private text"))
        }
    }

    @Test fun unicodePassword() {
        val locked = encrypted(plain("unicode.pdf", compress = false), user = "mật khẩu ✓")
        assertOpens(locked, "mật khẩu ✓")
    }

    @Test fun cancel() {
        val source = plain("cancel.pdf", compress = false)
        val failed = runCatching { PdfEncryptor.encrypt(source, File(folder.root, "c.pdf"), "1", "2") { true } }.exceptionOrNull()
        assertTrue("$failed", failed is CancellationException)
    }
}
