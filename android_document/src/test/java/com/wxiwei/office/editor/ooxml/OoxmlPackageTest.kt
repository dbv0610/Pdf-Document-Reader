package com.wxiwei.office.editor.ooxml

import com.wxiwei.office.editor.EditResult
import com.wxiwei.office.editor.Reason
import com.wxiwei.office.editor.runEdit
import com.wxiwei.office.fc.dom4j.Element
import com.wxiwei.office.fc.dom4j.Namespace
import com.wxiwei.office.fc.dom4j.QName
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File
import java.io.IOException
import java.util.zip.ZipEntry
import java.util.zip.ZipFile
import java.util.zip.ZipOutputStream

class OoxmlPackageTest {
    @get:Rule val temp = TemporaryFolder()
    private val types = Namespace.get("", "http://schemas.openxmlformats.org/package/2006/content-types")
    private val w14 = "http://schemas.microsoft.com/office/word/2010/wordml"

    private fun fixture(): File = temp.newFile("source.docx").also { file ->
        val entries = linkedMapOf(
            "word/document.xml" to """<?xml version="1.0" encoding="UTF-8" standalone="yes"?><w:document xmlns:w="${W.uri}" xmlns:mc="${MC.uri}" xmlns:w14="$w14" xmlns:r="${R.uri}" xmlns:a="${A.uri}" xmlns:p="${P.uri}" xmlns:wp="${WP.uri}" xmlns:v="urn:schemas-microsoft-com:vml" mc:Ignorable="w14"><w:body><w:p><w:r><w:t xml:space="preserve"> before &amp; after </w:t></w:r></w:p><w:sectPr/></w:body></w:document>""",
            "[Content_Types].xml" to """<Types xmlns="${types.uri}"><Default Extension="rels" ContentType="application/vnd.openxmlformats-package.relationships+xml"/><Default Extension="xml" ContentType="application/xml"/><Override PartName="/word/document.xml" ContentType="application/vnd.openxmlformats-officedocument.wordprocessingml.document.main+xml"/></Types>""",
            "_rels/.rels" to """<Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships"><Relationship Id="rId1" Type="$REL_OFFICE_DOCUMENT" Target="word/document.xml"/></Relationships>""",
            "word/_rels/document.xml.rels" to """<Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships"/>"""
        )
        ZipOutputStream(file.outputStream()).use { zip ->
            entries.forEach { (name, xml) ->
                zip.putNextEntry(ZipEntry(name))
                zip.write(xml.toByteArray())
                zip.closeEntry()
            }
        }
    }

    private fun text(pkg: OoxmlPackage): Element = pkg.xml("word/document.xml").rootElement
        .firstChild(W, "body")!!.firstChild(W, "p")!!.firstChild(W, "r")!!.firstChild(W, "t")!!

    @Test fun roundTripPreservesTextNamespacesAndOrder() {
        val pkg = OoxmlPackage.open(fixture())
        assertFalse(pkg.wasEncrypted)
        text(pkg).text = " changed < & Vietnamese: Việt > "
        val output = File(temp.root, "changed.docx")
        assertTrue(pkg.saveTo(output) is EditResult.Ok)
        val reopened = OoxmlPackage.open(output)
        assertEquals(" changed < & Vietnamese: Việt > ", text(reopened).text)
        val root = reopened.xml("word/document.xml").rootElement
        assertEquals("w", root.namespacePrefix)
        assertEquals("w14", root.attributeValue(QName("Ignorable", MC)))
        val expected = listOf(W, MC, R, A, P, WP, Namespace.get("w14", w14), Namespace.get("v", "urn:schemas-microsoft-com:vml"))
        expected.forEach { assertEquals(it.uri, root.getNamespaceForPrefix(it.prefix)?.uri) }
        ZipFile(output).use { zip ->
            val entries = zip.entries().toList()
            assertEquals(listOf("[Content_Types].xml", "word/document.xml", "_rels/.rels", "word/_rels/document.xml.rels"), entries.map { it.name })
            assertTrue(entries.all { it.method == ZipEntry.DEFLATED })
            val xml = zip.getInputStream(zip.getEntry("word/document.xml")).bufferedReader().use { it.readText() }
            assertTrue(xml.startsWith("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>"))
        }
    }

    @Test fun relationshipsAreUniqueAndResolveAcrossFolders() {
        val pkg = OoxmlPackage.open(fixture())
        val first = pkg.addRelationship("word/document.xml", REL_IMAGE, "media/image1.png")
        val second = pkg.addRelationship("word/document.xml", REL_IMAGE, "../shared/image2.png")
        assertNotEquals(first, second)
        assertEquals("word/media/image1.png", pkg.resolveTarget("word/document.xml", "media/image1.png"))
        assertEquals("shared/image2.png", pkg.resolveTarget("word/document.xml", "../shared/image2.png"))
        assertEquals("word/document.xml", pkg.resolveTarget("", "/word/./document.xml"))
        assertEquals("_rels/.rels", pkg.relsPartOf(""))
        assertEquals("word/_rels/document.xml.rels", pkg.relsPartOf("/word/document.xml"))
        pkg.addRelationship("ppt/slides/slide1.xml", REL_IMAGE, "https://example.org/image.png", true)
        val output = File(temp.root, "rels.docx")
        assertTrue(pkg.saveTo(output) is EditResult.Ok)
        val reopened = OoxmlPackage.open(output)
        assertEquals(listOf(first, second), reopened.relationships("word/document.xml").map { it.id })
        assertEquals("External", reopened.relationships("ppt/slides/slide1.xml").single().targetMode)
    }

    @Test fun mediaUsesUniqueNamesAndOneContentType() {
        val pkg = OoxmlPackage.open(fixture())
        val data = byteArrayOf(1, 2, 3)
        assertEquals("word/media/image1.png", pkg.addMedia("word/media", data, "png"))
        assertEquals("word/media/image2.png", pkg.addMedia("word/media", data, ".PNG"))
        pkg.ensureOverride("word/document.xml", "application/vnd.openxmlformats-officedocument.wordprocessingml.document.main+xml")
        val output = File(temp.root, "media.docx")
        assertTrue(pkg.saveTo(output) is EditResult.Ok)
        val reopened = OoxmlPackage.open(output)
        val root = reopened.xml("[Content_Types].xml").rootElement
        val defaults = root.childrenNamed(types, "Default").filter { it.attributeValue("Extension") == "png" }
        assertEquals(1, defaults.size)
        assertEquals("image/png", defaults.single().attributeValue("ContentType"))
        assertEquals(1, root.childrenNamed(types, "Override").size)
        assertArrayEquals(data, reopened.bytes("word/media/image2.png"))
    }

    @Test fun saveOverSourceAndRepeatedSaveIncludeRetainedDocumentEdits() {
        val source = fixture()
        val pkg = OoxmlPackage.open(source)
        val node = text(pkg)
        node.text = "first edit"
        assertTrue(pkg.saveTo(source) is EditResult.Ok)
        assertEquals("first edit", text(OoxmlPackage.open(source)).text)
        node.text = "second edit"
        assertTrue(pkg.saveTo(source) is EditResult.Ok)
        assertEquals("second edit", text(OoxmlPackage.open(source)).text)
    }

    @Test fun bytesReplacementInvalidatesXmlAndRemoveDropsPart() {
        val pkg = OoxmlPackage.open(fixture())
        text(pkg).text = "discarded"
        pkg.putBytes("/word/document.xml", "<w:document xmlns:w=\"${W.uri}\"><w:body/></w:document>".toByteArray())
        assertNull(pkg.xml("word/document.xml").rootElement.firstChild(W, "body")!!.firstChild(W, "p"))
        pkg.remove("/word/document.xml")
        assertFalse(pkg.has("word/document.xml"))
        assertNull(pkg.bytes("word/document.xml"))
    }

    @Test fun failedSaveKeepsSourceAndCleansTemporaryFile() {
        val source = fixture()
        val before = source.readBytes()
        val pkg = OoxmlPackage.open(source)
        // Force serialization to fail after the temporary file has been created.
        pkg.putXml("word/broken.xml", com.wxiwei.office.fc.dom4j.DocumentHelper.createDocument(newElement(W, "document")))
        pkg.xml("word/broken.xml").rootElement = null
        assertTrue(pkg.saveTo(source) is EditResult.Error)
        assertArrayEquals(before, source.readBytes())
        assertFalse(temp.root.listFiles()!!.any { it.name.startsWith("ooxml-") })
        assertEquals(Reason.IO, (runEdit { throw IOException("disk") } as EditResult.Error).reason)
        assertEquals(Reason.INTERNAL, (runEdit { throw IllegalStateException("bad state") } as EditResult.Error).reason)
    }

    @Test fun encryptedStandardPackageUsesPasswordLookupAndSavesPlaintext() {
        val source = fixture()
        val encrypted = File(temp.root, "encrypted.docx")
        writeEncryptedStandard(source.readBytes(), encrypted, "secret")
        try {
            OoxmlPackage.open(encrypted)
            fail("Missing password must fail")
        } catch (expected: IOException) {
            assertTrue(expected.message!!.contains("Password required"))
        }
        try {
            OoxmlPackage.open(encrypted, "wrong")
            fail("Incorrect password must fail")
        } catch (expected: IOException) {
            assertTrue(expected.message!!.contains("incorrect"))
        }
        com.wxiwei.office.system.DocumentPasswords.set(encrypted.path, "secret")
        try {
            val pkg = OoxmlPackage.open(encrypted)
            assertTrue(pkg.wasEncrypted)
            text(pkg).text = "decrypted edit"
            val result = pkg.saveTo(encrypted) as EditResult.Ok
            assertEquals(1, result.warnings.size)
            val reopened = OoxmlPackage.open(encrypted)
            assertFalse(reopened.wasEncrypted)
            assertEquals("decrypted edit", text(reopened).text)
        } finally {
            com.wxiwei.office.system.DocumentPasswords.clear(encrypted.path)
        }
    }

    /** Small Office 2007 AES-128 fixture, including the OLE2 wrapper. */
    private fun writeEncryptedStandard(plain: ByteArray, target: File, password: String) {
        val salt = ByteArray(16) { it.toByte() }
        val sha = java.security.MessageDigest.getInstance("SHA-1")
        fun littleInt(n: Int) = java.nio.ByteBuffer.allocate(4).order(java.nio.ByteOrder.LITTLE_ENDIAN).putInt(n).array()
        var hash = sha.digest(salt + password.toByteArray(Charsets.UTF_16LE))
        repeat(50000) { hash = sha.digest(littleInt(it) + hash) }
        hash = sha.digest(hash + littleInt(0))
        val key = sha.digest(ByteArray(64) { (0x36 xor (hash.getOrElse(it) { 0 }.toInt())).toByte() }).copyOf(16)
        fun encrypt(data: ByteArray): ByteArray = javax.crypto.Cipher.getInstance("AES/ECB/NoPadding").run {
            init(javax.crypto.Cipher.ENCRYPT_MODE, javax.crypto.spec.SecretKeySpec(key, "AES"))
            doFinal(data.copyOf((data.size + 15) / 16 * 16))
        }
        val verifier = ByteArray(16) { (it + 20).toByte() }
        val info = java.nio.ByteBuffer.allocate(116).order(java.nio.ByteOrder.LITTLE_ENDIAN).apply {
            putShort(4.toShort()); putShort(2.toShort()); putInt(0x24)
            putInt(32) // EncryptionHeader length
            putInt(0x24); putInt(0); putInt(0x660e); putInt(0x8004)
            putInt(128); putInt(0x18); putInt(0); putInt(0)
            putInt(16); put(salt); put(encrypt(verifier)); putInt(20); put(encrypt(sha.digest(verifier)))
        }.array()
        val payload = java.nio.ByteBuffer.allocate(8).order(java.nio.ByteOrder.LITTLE_ENDIAN).putLong(plain.size.toLong()).array() + encrypt(plain)
        val fs = com.wxiwei.office.fc.poifs.filesystem.POIFSFileSystem()
        fs.createDocument(info.inputStream(), "EncryptionInfo")
        fs.createDocument(payload.inputStream(), "EncryptedPackage")
        target.outputStream().use { fs.writeFilesystem(it) }
    }

    @Test fun xmlHelpersMatchUrisAndConvertUnits() {
        val root = newElement(W, "body")
        root.add(newElement(Namespace.get("other", W.uri), "p"))
        assertEquals(1, root.childrenNamed(W, "p").size)
        assertEquals(914400L, pxToEmu(96))
        assertEquals(914400L, ptToEmu(72))
        assertEquals(6350L, ptToEmu(0.5))
    }
}
