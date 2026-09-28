package com.alf06.document.reader.ui.home.document

import android.content.ContentValues
import android.content.Context
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import java.io.File

/** A picture into Pictures/[folder] (MediaStore on Android 10+, the folder itself before); false when it could not be written. */
fun Context.savePictureToGallery(folder: String, fileName: String, bytes: ByteArray, mime: String): Boolean = try {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        val values = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, fileName)
            put(MediaStore.Images.Media.MIME_TYPE, mime)
            put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/" + folder)
        }
        val uri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values) ?: throw IllegalStateException("No media entry")
        contentResolver.openOutputStream(uri)!!.use { it.write(bytes) }
        true
    } else {
        @Suppress("DEPRECATION")
        val dir = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), folder).apply { mkdirs() }
        File(dir, fileName).writeBytes(bytes)
        true
    }
} catch (e: Exception) {
    false
}
