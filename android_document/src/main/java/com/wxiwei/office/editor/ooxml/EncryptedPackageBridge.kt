package com.wxiwei.office.editor.ooxml

import com.wxiwei.office.fc.poifs.filesystem.DirectoryNode
import com.wxiwei.office.fc.poifs.filesystem.POIFSFileSystem
import com.wxiwei.office.system.EncryptedOoxml
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.IOException
import java.io.OutputStream
import java.lang.reflect.InvocationTargetException
import java.nio.ByteBuffer
import java.nio.ByteOrder

/**
 * Reuses EncryptedOoxml's engines without its Activity/cache-file wrapper or global password mutation.
 * Its engines are currently private; keep this reflection isolated until that class exposes a
 * stream API. Renaming/obfuscating those private engines requires updating this bridge/keep rules.
 */
internal object EncryptedPackageBridge {
    fun decrypt(file: File, password: String?): ByteArray {
        if (password == null) throw IOException("Password required for encrypted OOXML package")
        val fs = file.inputStream().use { POIFSFileSystem(it) }
        val root = fs.root
        val info = root.createDocumentInputStream("EncryptionInfo").use { it.readBytes() }
        if (info.size < 8) throw IOException("Truncated EncryptionInfo")
        val header = ByteBuffer.wrap(info).order(ByteOrder.LITTLE_ENDIAN)
        val major = header.short.toInt()
        val minor = header.short.toInt()
        val agile = major == 4 && minor == 4
        if (!agile && !(minor == 2 && major in 3..4)) throw IOException("Unsupported encryption version $major.$minor")
        try {
            val engineClass = EncryptedOoxml::class.java.declaredClasses.single {
                it.simpleName == if (agile) "Agile" else "Standard"
            }
            val constructor = engineClass.getDeclaredConstructor(if (agile) String::class.java else ByteArray::class.java)
                .apply { isAccessible = true }
            val engine = constructor.newInstance(if (agile) String(info, 8, info.size - 8, Charsets.UTF_8) else info)
            val key = engineClass.getDeclaredMethod("secretKey", String::class.java).apply { isAccessible = true }
                .invoke(engine, password) as ByteArray? ?: throw IOException("Password is incorrect")
            return ByteArrayOutputStream().also { out ->
                engineClass.getDeclaredMethod("decryptPackage", DirectoryNode::class.java, ByteArray::class.java, OutputStream::class.java)
                    .apply { isAccessible = true }.invoke(engine, root, key, out)
            }.toByteArray()
        } catch (e: InvocationTargetException) {
            throw IOException("Cannot decrypt OOXML package", e.targetException)
        } catch (e: ReflectiveOperationException) {
            throw IOException("EncryptedOoxml engine unavailable", e)
        }
    }
}
