package com.wxiwei.office.editor.ooxml

import com.wxiwei.office.editor.EditResult
import com.wxiwei.office.editor.runEdit
import com.wxiwei.office.fc.dom4j.Document
import com.wxiwei.office.fc.dom4j.DocumentHelper
import com.wxiwei.office.fc.dom4j.Element
import com.wxiwei.office.fc.dom4j.Namespace
import com.wxiwei.office.fc.dom4j.QName
import com.wxiwei.office.fc.dom4j.io.OutputFormat
import com.wxiwei.office.fc.dom4j.io.SAXReader
import com.wxiwei.office.fc.dom4j.io.XMLWriter
import com.wxiwei.office.system.DocumentPasswords
import com.wxiwei.office.system.EncryptedOoxml
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileNotFoundException
import java.io.IOException
import java.nio.file.Files
import java.nio.file.StandardCopyOption
import java.util.Locale
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream

/** In-memory OPC package. Mutable documents and this package are not thread safe. */
class OoxmlPackage private constructor(
    private val parts: LinkedHashMap<String, ByteArray>,
    val wasEncrypted: Boolean
) {
    data class Rel(val id: String, val type: String, val target: String, val targetMode: String?)

    private val documents = linkedMapOf<String, Document>()
    private val rootNamespaces = mutableMapOf<String, Map<String, String>>()

    fun partNames(): List<String> = parts.keys.toList()
    fun has(part: String): Boolean = parts.containsKey(name(part))

    /** Includes edits made through xml(); the returned array is a copy. */
    fun bytes(part: String): ByteArray? {
        val key = name(part)
        return documents[key]?.let { serialize(it, rootNamespaces[key].orEmpty()) }
            ?: parts[key]?.copyOf()
    }

    fun putBytes(part: String, bytes: ByteArray) {
        val key = name(part)
        require(key.isNotEmpty()) { "Empty part name" }
        parts[key] = bytes.copyOf()
        documents.remove(key)
        rootNamespaces.remove(key)
    }

    fun remove(part: String) {
        val key = name(part)
        parts.remove(key)
        documents.remove(key)
        rootNamespaces.remove(key)
    }

    /** Returning a document marks it dirty, including subsequent mutations through retained references. */
    fun xml(part: String): Document {
        val key = name(part)
        return documents.getOrPut(key) {
            val data = parts[key] ?: throw FileNotFoundException("Missing OOXML part: $key")
            parse(data).also { rootNamespaces[key] = namespaces(it.rootElement) }
        }
    }

    fun putXml(part: String, doc: Document) {
        val key = name(part)
        require(key.isNotEmpty()) { "Empty part name" }
        requireNotNull(doc.rootElement) { "XML part needs a root element" }
        if (!parts.containsKey(key)) parts[key] = byteArrayOf()
        documents[key] = doc
        rootNamespaces[key] = namespaces(doc.rootElement)
    }

    fun relsPartOf(part: String): String {
        val key = name(part)
        if (key.isEmpty()) return "_rels/.rels"
        val folder = key.substringBeforeLast('/', "")
        return (if (folder.isEmpty()) "" else "$folder/") + "_rels/" + key.substringAfterLast('/') + ".rels"
    }

    fun relationships(part: String): List<Rel> {
        val key = relsPartOf(part)
        if (!has(key)) return emptyList()
        return children(xml(key).rootElement, RELS, "Relationship").map {
            Rel(it.attributeValue("Id"), it.attributeValue("Type"),
                it.attributeValue("Target"), it.attributeValue("TargetMode"))
        }
    }

    fun addRelationship(part: String, type: String, target: String, external: Boolean = false): String {
        require(type.isNotBlank() && target.isNotBlank()) { "Relationship type and target are required" }
        val ids = relationships(part).map { it.id }.toSet()
        var n = 1
        while ("rId$n" in ids) n++
        val id = "rId$n"
        val key = relsPartOf(part)
        if (!has(key)) putXml(key, document(RELS, "Relationships"))
        xml(key).rootElement.addElement(QName("Relationship", Namespace.get("", RELS))).apply {
            addAttribute("Id", id)
            addAttribute("Type", type)
            addAttribute("Target", target)
            if (external) addAttribute("TargetMode", "External")
        }
        ensureDefaultContentType("rels", "application/vnd.openxmlformats-package.relationships+xml")
        return id
    }

    /** Resolves internal relationship targets only; external URIs have no package part. */
    fun resolveTarget(part: String, target: String): String {
        require(!Regex("^[A-Za-z][A-Za-z0-9+.-]*:").containsMatchIn(target)) { "External target: $target" }
        val base = name(part).substringBeforeLast('/', "")
        val path = if (target.startsWith('/')) target else "$base/$target"
        val segments = mutableListOf<String>()
        for (segment in path.split('/')) when (segment) {
            "", "." -> Unit
            ".." -> {
                require(segments.isNotEmpty()) { "Target escapes package root: $target" }
                segments.removeAt(segments.lastIndex)
            }
            else -> segments.add(segment)
        }
        return segments.joinToString("/")
    }

    fun ensureDefaultContentType(ext: String, contentType: String) {
        val extension = ext.removePrefix(".").lowercase(Locale.ROOT)
        require(extension.isNotBlank() && contentType.isNotBlank())
        val root = contentTypes()
        val matches = children(root, TYPES, "Default").filter {
            it.attributeValue("Extension").equals(extension, ignoreCase = true)
        }
        val entry = matches.firstOrNull() ?: root.addElement(QName("Default", Namespace.get("", TYPES)))
        entry.addAttribute("Extension", extension).addAttribute("ContentType", contentType)
        matches.drop(1).forEach { root.remove(it) }
    }

    fun ensureOverride(partName: String, contentType: String) {
        val key = "/" + name(partName)
        require(key != "/" && contentType.isNotBlank())
        val root = contentTypes()
        val matches = children(root, TYPES, "Override").filter { it.attributeValue("PartName") == key }
        val entry = matches.firstOrNull() ?: root.addElement(QName("Override", Namespace.get("", TYPES)))
        entry.addAttribute("PartName", key).addAttribute("ContentType", contentType)
        matches.drop(1).forEach { root.remove(it) }
    }

    fun addMedia(folder: String, bytes: ByteArray, ext: String): String {
        val extension = ext.removePrefix(".").lowercase(Locale.ROOT)
        val mime = when (extension) {
            "png" -> "image/png"
            "jpeg", "jpg" -> "image/jpeg"
            "gif" -> "image/gif"
            "bmp" -> "image/bmp"
            "webp" -> "image/webp"
            else -> throw IllegalArgumentException("Unsupported image extension: $ext")
        }
        val dir = name(folder).trimEnd('/')
        require(dir.isNotBlank() && dir.split('/').none { it == ".." || it == "." })
        var n = 1
        while (has("$dir/image$n.$extension")) n++
        val part = "$dir/image$n.$extension"
        ensureDefaultContentType(extension, mime)
        putBytes(part, bytes)
        return part
    }

    /** Atomically replaces target; a failed write or rename leaves the old target intact. */
    fun saveTo(target: File): EditResult = runEdit {
        val parent = target.absoluteFile.parentFile ?: throw IOException("Target has no parent")
        val temp = File.createTempFile("ooxml-", ".tmp", parent)
        try {
            ZipOutputStream(temp.outputStream().buffered()).use { zip ->
                val order = listOf(CONTENT_TYPES) + parts.keys.filter { it != CONTENT_TYPES }
                for (key in order) {
                    val data = bytes(key) ?: throw IOException("Missing $key")
                    zip.putNextEntry(ZipEntry(key).apply { method = ZipEntry.DEFLATED })
                    zip.write(data)
                    zip.closeEntry()
                }
            }
            // No non-atomic fallback: on unsupported filesystems return IO and keep the target.
            Files.move(temp.toPath(), target.toPath(), StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING)
            EditResult.Ok(target, if (wasEncrypted) listOf("Saved an unencrypted copy of the encrypted document.") else emptyList())
        } finally {
            temp.delete()
        }
    }

    private fun contentTypes(): Element {
        if (!has(CONTENT_TYPES)) putXml(CONTENT_TYPES, document(TYPES, "Types"))
        return xml(CONTENT_TYPES).rootElement
    }

    companion object {
        private const val CONTENT_TYPES = "[Content_Types].xml"
        private const val RELS = "http://schemas.openxmlformats.org/package/2006/relationships"
        private const val TYPES = "http://schemas.openxmlformats.org/package/2006/content-types"

        fun open(file: File, password: String? = DocumentPasswords.get(file.path)): OoxmlPackage {
            val encrypted = EncryptedOoxml.isEncrypted(file)
            val input = if (encrypted) EncryptedPackageBridge.decrypt(file, password).inputStream() else file.inputStream()
            val parts = linkedMapOf<String, ByteArray>()
            ZipInputStream(input.buffered()).use { zip ->
                while (true) {
                    val entry = zip.nextEntry ?: break
                    val key = name(entry.name)
                    if (key.isEmpty() || parts.containsKey(key)) throw IOException("Invalid or duplicate ZIP entry: $key")
                    parts[key] = zip.readBytes()
                    zip.closeEntry()
                }
            }
            if (!parts.containsKey(CONTENT_TYPES)) throw IOException("Not an OOXML package: missing $CONTENT_TYPES")
            return OoxmlPackage(parts, encrypted)
        }

        private fun name(part: String) = part.trimStart('/')

        private fun children(root: Element, uri: String, local: String): List<Element> =
            root.elements().filterIsInstance<Element>().filter { it.namespaceURI == uri && it.name == local }

        private fun document(uri: String, local: String): Document = DocumentHelper.createDocument().apply {
            addElement(QName(local, Namespace.get("", uri)))
        }

        // Android's bundled SAX parser rejects the Xerces-only features, so each one is best effort.
        private fun parse(bytes: ByteArray): Document = SAXReader().apply {
            setFeature("http://xml.org/sax/features/namespaces", true)
            runCatching { setFeature("http://apache.org/xml/features/disallow-doctype-decl", true) }
            runCatching { setFeature("http://xml.org/sax/features/external-general-entities", false) }
            runCatching { setFeature("http://xml.org/sax/features/external-parameter-entities", false) }
        }.read(bytes.inputStream())

        private fun namespaces(root: Element): Map<String, String> = linkedMapOf<String, String>().apply {
            if (root.namespaceURI.isNotEmpty()) put(root.namespacePrefix, root.namespaceURI)
            root.declaredNamespaces().filterIsInstance<Namespace>().forEach { put(it.prefix, it.uri) }
        }

        private fun serialize(doc: Document, original: Map<String, String>): ByteArray {
            // Preserve declarations even when their only use is in mc:Ignorable's string value.
            val required = original + namespaces(doc.rootElement)
            val declared = namespaces(doc.rootElement)
            required.filter { (prefix, uri) -> declared[prefix] != uri }.forEach { (prefix, uri) ->
                doc.rootElement.addNamespace(prefix, uri)
            }
            var result = writeXml(doc)
            val roundTrip = parse(result)
            val missing = required.filter { (prefix, uri) -> namespaces(roundTrip.rootElement)[prefix] != uri }
            if (missing.isNotEmpty()) {
                missing.forEach { (prefix, uri) -> roundTrip.rootElement.addNamespace(prefix, uri) }
                result = writeXml(roundTrip)
                val actual = namespaces(parse(result).rootElement)
                if (required.any { (prefix, uri) -> actual[prefix] != uri }) {
                    throw IOException("XML writer dropped root namespace declarations")
                }
            }
            return result
        }

        private fun writeXml(doc: Document): ByteArray {
            val out = ByteArrayOutputStream()
            val format = OutputFormat("", false, "UTF-8").apply {
                isTrimText = false
                isNewLineAfterDeclaration = false
            }
            val writer = object : XMLWriter(out, format) {
                override fun writeDeclaration() {
                    writer.write("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>")
                }
            }
            try { writer.write(doc) } finally { writer.close() }
            return out.toByteArray()
        }
    }
}
