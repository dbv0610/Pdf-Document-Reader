package com.wxiwei.office.system

import com.wxiwei.office.fc.poifs.filesystem.DirectoryNode
import com.wxiwei.office.fc.poifs.filesystem.POIFSFileSystem
import org.w3c.dom.Element
import java.io.File
import java.io.FileInputStream
import java.io.InputStream
import java.io.OutputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.security.MessageDigest
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec
import javax.xml.parsers.DocumentBuilderFactory

/**
 * A password-protected .docx/.xlsx/.pptx is an OLE2 container holding EncryptionInfo and
 * EncryptedPackage streams. Decrypts it to a plain OOXML file in the app's private cache so the
 * normal OOXML readers can open it.
 */
internal object EncryptedOoxml {
    private const val ENCRYPTION_INFO = "EncryptionInfo"
    private const val ENCRYPTED_PACKAGE = "EncryptedPackage"
    private const val DIR_NAME = "decrypted"

    fun isEncrypted(file: File): Boolean = try {
        FileInputStream(file).use { POIFSFileSystem(it).root.hasEntry(ENCRYPTION_INFO) }
    } catch (e: Exception) {
        false
    }

    /** @return path of the decrypted copy, named with [extension] so readers see an OOXML file */
    fun decrypt(control: IControl, file: File, extension: String): String {
        val password = DocumentPasswords.get(file.path)
            ?: throw OpenFileException(OpenFileException.Reason.PASSWORD_REQUIRED, file.path,
                Exception("Document with password"))
        val fs = FileInputStream(file).use { POIFSFileSystem(it) }
        val root = fs.root
        val info = root.createDocumentInputStream(ENCRYPTION_INFO).use { it.readBytes() }
        val major = ByteBuffer.wrap(info, 0, 2).order(ByteOrder.LITTLE_ENDIAN).short.toInt()
        val minor = ByteBuffer.wrap(info, 2, 2).order(ByteOrder.LITTLE_ENDIAN).short.toInt()

        val dir = File(control.getActivity().cacheDir, DIR_NAME)
        // one document is open at a time; don't leave earlier plaintext copies behind
        dir.listFiles()?.forEach { it.delete() }
        if (!dir.exists() && !dir.mkdirs()) {
            throw OpenFileException(OpenFileException.Reason.STORAGE, file.path,
                Exception("Cannot create $dir"))
        }
        val out = File(dir, "document.$extension")
        if (major == 4 && minor == 4) {
            val agile = Agile(String(info, 8, info.size - 8, Charsets.UTF_8))
            val key = agile.secretKey(password) ?: throw incorrect(file)
            out.outputStream().use { agile.decryptPackage(root, key, it) }
        } else if (minor == 2 && (major == 3 || major == 4)) {
            val standard = Standard(info)
            val key = standard.secretKey(password) ?: throw incorrect(file)
            out.outputStream().use { standard.decryptPackage(root, key, it) }
        } else {
            throw OpenFileException(OpenFileException.Reason.UNKNOWN, file.path,
                Exception("Unsupported encryption version $major.$minor"))
        }
        return out.path
    }

    /**
     * MS-OFFCRYPTO 2.3.4.5 standard encryption (Office 2007): AES-ECB, SHA-1 key derivation.
     * The bundled POI EcmaDecryptor reads past the end of EncryptedPackage, so this is done here.
     */
    private class Standard(info: ByteArray) {
        private val keyBits: Int
        private val salt: ByteArray
        private val encryptedVerifier: ByteArray
        private val encryptedVerifierHash: ByteArray

        init {
            val b = ByteBuffer.wrap(info).order(ByteOrder.LITTLE_ENDIAN)
            b.position(8)
            val headerSize = b.int
            val headerStart = b.position()
            keyBits = b.getInt(headerStart + 16)
            b.position(headerStart + headerSize)
            salt = ByteArray(b.int).also { b.get(it) }
            encryptedVerifier = ByteArray(16).also { b.get(it) }
            b.int // verifierHashSize, always 20 (SHA-1)
            encryptedVerifierHash = ByteArray(b.remaining()).also { b.get(it) }
        }

        fun secretKey(password: String): ByteArray? {
            val sha1 = MessageDigest.getInstance("SHA-1")
            var h = sha1.run { update(salt); digest(password.toByteArray(Charsets.UTF_16LE)) }
            val counter = ByteArray(4)
            repeat(SPIN_COUNT) { i ->
                ByteBuffer.wrap(counter).order(ByteOrder.LITTLE_ENDIAN).putInt(i)
                h = sha1.run { update(counter); digest(h) }
            }
            val hFinal = sha1.run { update(h); digest(ByteArray(4)) } // block number 0
            fun xorDigest(fill: Int) = sha1.digest(ByteArray(64) { (fill xor (hFinal.getOrElse(it) { 0 }.toInt())).toByte() })
            val key = (xorDigest(0x36) + xorDigest(0x5c)).copyOf(keyBits / 8)

            val verifier = ecb(key, encryptedVerifier)
            val verifierHash = ecb(key, encryptedVerifierHash)
            val expected = sha1.digest(verifier)
            return if (MessageDigest.isEqual(expected, verifierHash.copyOf(expected.size))) key else null
        }

        fun decryptPackage(root: DirectoryNode, key: ByteArray, out: OutputStream) {
            root.createDocumentInputStream(ENCRYPTED_PACKAGE).use { input ->
                val sizeBytes = ByteArray(8)
                readExactly(input, sizeBytes, 8)
                var remaining = ByteBuffer.wrap(sizeBytes).order(ByteOrder.LITTLE_ENDIAN).long
                val cipher = Cipher.getInstance("AES/ECB/NoPadding").apply {
                    init(Cipher.DECRYPT_MODE, SecretKeySpec(key, "AES"))
                }
                val chunk = ByteArray(CHUNK)
                while (remaining > 0) {
                    val plainLen = minOf(remaining, CHUNK.toLong()).toInt()
                    val cipherLen = (plainLen + 15) / 16 * 16
                    readExactly(input, chunk, cipherLen)
                    out.write(cipher.update(chunk, 0, cipherLen), 0, plainLen)
                    remaining -= plainLen
                }
            }
        }

        private fun ecb(key: ByteArray, data: ByteArray): ByteArray =
            Cipher.getInstance("AES/ECB/NoPadding").run {
                init(Cipher.DECRYPT_MODE, SecretKeySpec(key, "AES"))
                doFinal(data)
            }

        companion object {
            const val SPIN_COUNT = 50000
            const val CHUNK = 4096
        }
    }

    private fun readExactly(input: InputStream, buffer: ByteArray, length: Int) {
        var off = 0
        while (off < length) {
            val n = input.read(buffer, off, length - off)
            if (n < 0) throw java.io.EOFException("EncryptedPackage truncated")
            off += n
        }
    }

    private fun incorrect(file: File) = OpenFileException(OpenFileException.Reason.PASSWORD_INCORRECT,
        file.path, Exception("Password is incorrect"))

    /** MS-OFFCRYPTO 2.3.4.10 agile encryption (Office 2010+), password key encryptor only. */
    private class Agile(xml: String) {
        private val keyData: Element
        private val encryptedKey: Element

        init {
            val factory = DocumentBuilderFactory.newInstance().apply { isNamespaceAware = true }
            val doc = factory.newDocumentBuilder().parse(xml.byteInputStream())
            keyData = doc.getElementsByTagNameNS("*", "keyData").item(0) as Element
            encryptedKey = doc.getElementsByTagNameNS("*", "encryptedKey").item(0) as Element
        }

        fun secretKey(password: String): ByteArray? {
            val hash = hashName(encryptedKey)
            val salt = b64(encryptedKey, "saltValue")
            val keyBytes = int(encryptedKey, "keyBits") / 8
            val iv = fit(salt, int(encryptedKey, "blockSize"))

            var h = digest(hash, salt, password.toByteArray(Charsets.UTF_16LE))
            val counter = ByteArray(4)
            repeat(int(encryptedKey, "spinCount")) { i ->
                ByteBuffer.wrap(counter).order(ByteOrder.LITTLE_ENDIAN).putInt(i)
                h = digest(hash, counter, h)
            }
            fun derive(block: ByteArray) = fit(digest(hash, h, block), keyBytes)

            val verifierInput = aes(derive(VERIFIER_INPUT_BLOCK), iv, b64(encryptedKey, "encryptedVerifierHashInput"))
                .copyOf(salt.size)
            val verifierHash = aes(derive(VERIFIER_HASH_BLOCK), iv, b64(encryptedKey, "encryptedVerifierHashValue"))
            val expected = digest(hash, verifierInput)
            if (!MessageDigest.isEqual(expected, verifierHash.copyOf(expected.size))) return null
            return aes(derive(KEY_VALUE_BLOCK), iv, b64(encryptedKey, "encryptedKeyValue"))
                .copyOf(int(keyData, "keyBits") / 8)
        }

        fun decryptPackage(root: DirectoryNode, key: ByteArray, out: OutputStream) {
            val hash = hashName(keyData)
            val salt = b64(keyData, "saltValue")
            val blockSize = int(keyData, "blockSize")
            root.createDocumentInputStream(ENCRYPTED_PACKAGE).use { input ->
                val sizeBytes = ByteArray(8)
                readExactly(input, sizeBytes, 8)
                var remaining = ByteBuffer.wrap(sizeBytes).order(ByteOrder.LITTLE_ENDIAN).long
                val chunk = ByteArray(SEGMENT)
                val index = ByteArray(4)
                var segment = 0
                while (remaining > 0) {
                    val plainLen = minOf(remaining, SEGMENT.toLong()).toInt()
                    // cipher text is padded to the AES block size
                    val cipherLen = (plainLen + blockSize - 1) / blockSize * blockSize
                    readExactly(input, chunk, cipherLen)
                    ByteBuffer.wrap(index).order(ByteOrder.LITTLE_ENDIAN).putInt(segment)
                    val iv = fit(digest(hash, salt, index), blockSize)
                    out.write(aes(key, iv, chunk.copyOf(cipherLen)), 0, plainLen)
                    remaining -= plainLen
                    segment++
                }
            }
        }

        private fun aes(key: ByteArray, iv: ByteArray, data: ByteArray): ByteArray =
            Cipher.getInstance("AES/CBC/NoPadding").run {
                init(Cipher.DECRYPT_MODE, SecretKeySpec(key, "AES"), IvParameterSpec(iv))
                doFinal(data)
            }

        private fun digest(hash: String, vararg parts: ByteArray): ByteArray =
            MessageDigest.getInstance(hash).run { parts.forEach { update(it) }; digest() }

        /** Truncate, or pad with 0x36, to [size] bytes (MS-OFFCRYPTO 2.3.4.11). */
        private fun fit(bytes: ByteArray, size: Int): ByteArray =
            ByteArray(size) { if (it < bytes.size) bytes[it] else 0x36 }

        private fun hashName(e: Element): String = when (val name = e.getAttribute("hashAlgorithm").uppercase()) {
            "SHA1" -> "SHA-1"
            "SHA256" -> "SHA-256"
            "SHA384" -> "SHA-384"
            "SHA512" -> "SHA-512"
            "MD5" -> "MD5"
            else -> throw OpenFileException(OpenFileException.Reason.UNKNOWN, null,
                Exception("Unsupported hash algorithm $name"))
        }

        private fun int(e: Element, name: String) = e.getAttribute(name).toInt()

        private fun b64(e: Element, name: String): ByteArray =
            android.util.Base64.decode(e.getAttribute(name), android.util.Base64.DEFAULT)

        companion object {
            const val SEGMENT = 4096
            val VERIFIER_INPUT_BLOCK = byteArrayOf(0xfe.toByte(), 0xa7.toByte(), 0xd2.toByte(), 0x76, 0x3b, 0x4b, 0x9e.toByte(), 0x79)
            val VERIFIER_HASH_BLOCK = byteArrayOf(0xd7.toByte(), 0xaa.toByte(), 0x0f, 0x6d, 0x30, 0x61, 0x34, 0x4e)
            val KEY_VALUE_BLOCK = byteArrayOf(0x14, 0x6e, 0x0b, 0xe7.toByte(), 0xab.toByte(), 0xac.toByte(), 0xd0.toByte(), 0xd6.toByte())
        }
    }
}
