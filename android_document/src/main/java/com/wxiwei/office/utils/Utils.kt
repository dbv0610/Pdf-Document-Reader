package com.wxiwei.office.utils

import android.content.Context
import android.net.Uri
import android.provider.MediaStore
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream

object Utils {

    @JvmStatic
    fun getPathFromOutlook(context: Context, uri: Uri): String? {
        // uri2 = Uri.parse("content://com.microsoft.office.outlook.fileprovider/outlookfile/data/user/0/com.microsoft.office.outlook/cache/file-download/file--1723028522/Sachvui.Com-Phi-ly-tri-Dan-Ariely-scan.pdf");
        ///outlookfile/data/data/com.microsoft.office.outlook/cache/file-download/file-1754115030/Doc 19-02-2021 15_00 CH.pdf
        return uri.path?.replace("/outlookfile/data", "storage/emulated/0")
    }

    @JvmStatic
    fun getPathUriGmail(context: Context, uri: Uri): String? {
        var `is`: InputStream? = null
        var os: FileOutputStream? = null
        var fullPath: String? = null

        try {
            val scheme = uri.scheme
            var name: String? = null

            if (scheme == "content") {
                val cursor = context.contentResolver.query(
                    uri, arrayOf(MediaStore.MediaColumns.DISPLAY_NAME), null, null, null
                )
                cursor!!.moveToFirst()
                val nameIndex = cursor.getColumnIndex(MediaStore.MediaColumns.DISPLAY_NAME)
                if (nameIndex >= 0) {
                    name = cursor.getString(nameIndex)
                }
            } else {
                return null
            }

            if (name == null) {
                return null
            }

            val n = name.lastIndexOf(".")

            if (n == -1 || n == name.length - 1) {
                return null
            }

            fullPath = context.cacheDir.toString() + "/" + name

            `is` = context.contentResolver.openInputStream(uri)
            os = FileOutputStream(fullPath)

            val buffer = ByteArray(4096)
            var count: Int
            while (`is`!!.read(buffer).also { count = it } > 0) {
                os.write(buffer, 0, count)
            }
            os.close()
            `is`.close()
        } catch (e: Exception) {
            if (`is` != null) {
                try {
                    `is`.close()
                } catch (e1: Exception) {
                }
            }
            if (os != null) {
                try {
                    os.close()
                } catch (e1: Exception) {
                }
            }
            if (fullPath != null) {
                val f = File(fullPath)
                f.delete()
            }
            e.printStackTrace()
        }

        return fullPath
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is Google Gmail.
     */
    @JvmStatic
    fun isGoogleGmailUri(uri: Uri): Boolean {
        return "com.google.android.gm.sapi" == uri.authority
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is Outlook email.
     */
    @JvmStatic
    fun isOutlookUri(uri: Uri): Boolean {
        return "com.microsoft.office.outlook.fileprovider" == uri.authority
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is WhatApp.
     */
    @JvmStatic
    fun isWhatAppUri(uri: Uri): Boolean {
        return "com.whatsapp.provider.media" == uri.authority
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is Telegram.
     */
    @JvmStatic
    fun isTelegramUri(uri: Uri): Boolean {
        return "org.telegram.messenger.provider" == uri.authority
    }
}
