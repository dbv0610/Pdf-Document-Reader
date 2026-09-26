package com.wxiwei.office.utils

import android.annotation.SuppressLint
import android.content.ContentUris
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.DocumentsContract
import android.provider.MediaStore
import android.provider.OpenableColumns
import android.util.Log
import androidx.loader.content.CursorLoader
import java.io.File
import java.io.FileOutputStream

object RealPathUtil {

    @JvmStatic
    fun getRealPath(context: Context, fileUri: Uri): String? {
        // SDK < API11
        return if (Build.VERSION.SDK_INT < 11) {
            getRealPathFromURIBelowAPI11(context, fileUri)
        }
        // SDK >= 11 && SDK < 19
        else if (Build.VERSION.SDK_INT < 19) {
            getRealPathFromURIAPI11to18(context, fileUri)
        }
        // SDK > 19 (Android 4.4) and up
        else {
            getRealPathFromURIAPI19(context, fileUri)
        }
    }

    @SuppressLint("NewApi")
    @JvmStatic
    fun getRealPathFromURIAPI11to18(context: Context, contentUri: Uri): String? {
        val proj = arrayOf(MediaStore.Images.Media.DATA)
        var result: String? = null

        val cursorLoader = CursorLoader(context, contentUri, proj, null, null, null)
        val cursor = cursorLoader.loadInBackground()

        if (cursor != null) {
            val columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
            cursor.moveToFirst()
            result = cursor.getString(columnIndex)
            cursor.close()
        }
        return result
    }

    @JvmStatic
    fun getRealPathFromURIBelowAPI11(context: Context, contentUri: Uri): String {
        val proj = arrayOf(MediaStore.Images.Media.DATA)
        val cursor = context.contentResolver.query(contentUri, proj, null, null, null)
        var result = ""
        if (cursor != null) {
            val columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
            cursor.moveToFirst()
            result = cursor.getString(columnIndex)
            cursor.close()
            return result
        }
        return result
    }

    /**
     * Get a file path from a Uri. This will get the the path for Storage Access
     * Framework Documents, as well as the _data field for the MediaStore and
     * other file-based ContentProviders.
     *
     * @param context The context.
     * @param uri     The Uri to query.
     * @author paulburke
     */
    @SuppressLint("NewApi")
    @JvmStatic
    fun getRealPathFromURIAPI19(context: Context, uri: Uri): String? {

        // ExternalStorageProvider
        if (isExternalStorageDocument(uri)) {
            val docId = DocumentsContract.getDocumentId(uri)
            val split = docId.split(":".toRegex()).toTypedArray()
            val type = split[0]

            if ("primary".equals(type, ignoreCase = true)) {
                return Environment.getExternalStorageDirectory().toString() + "/" + split[1]
            }
        } else if (Utils.isGoogleGmailUri(uri)) {
            return Utils.getPathUriGmail(context, uri)
        } else if (Utils.isOutlookUri(uri)) {
            return Utils.getPathFromOutlook(context, uri)
        }
        if (Utils.isWhatAppUri(uri)) {
            return Utils.getPathUriGmail(context, uri)
        }
        // telegram
        else if (Utils.isTelegramUri(uri)) {
            val nameFile = getNameFile(context, uri, null, null)
            val path = Environment.getExternalStorageDirectory()
                .toString() + "/Telegram/Telegram Documents/" + nameFile
            val file = File(path)
            if (file.exists()) {
                return path
            }
            return null
        }
        // DownloadsProvider
        else if (isDownloadsDocument(uri)) {
            val fileName = getNameFile(context, uri, null, null)
            if (fileName != null) {
                return Environment.getExternalStorageDirectory()
                    .toString() + "/Download/" + fileName
            }
            val id = DocumentsContract.getDocumentId(uri)
//                final Uri contentUri = ContentUris.withAppendedId(Uri.parse("content://downloads/all_downloads"), Long.valueOf(id));
            val contentUri = ContentUris.withAppendedId(
                Uri.parse("content://downloads/public_downloads"),
                id.toLong()
            )
            return getDataColumn(context, contentUri, null, null)
        } else if (isVsmart(uri)) {
            val nameFile = getNameFile(context, uri, null, null)
            Log.e("nameFile", nameFile!!)
            DocumentsContract.getDocumentId(uri)
            return getPathVsmart(context, uri, null, null)
        }
        // MediaProvider
        else if (isMediaDocument(uri)) {
            val docId = DocumentsContract.getDocumentId(uri)
            val split = docId.split(":".toRegex()).toTypedArray()
            val type = split[0]

            var contentUri: Uri? = null

            if ("image" == type) {
                contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
            } else if ("video" == type) {
                contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI
            } else if ("audio" == type) {
                contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
            }

            val selection = "_id=?"
            val selectionArgs = arrayOf(split[1])

            return getDataColumn(context, contentUri, selection, selectionArgs)
        } else if (isGoogleDriveUri(uri)) {
            return getDriveFilePath(uri, context)
        }


        return null
    }

    /**
     * Get the value of the data column for this Uri. This is useful for
     * MediaStore Uris, and other file-based ContentProviders.
     *
     * @param context       The context.
     * @param uri           The Uri to query.
     * @param selection     (Optional) Filter used in the query.
     * @param selectionArgs (Optional) Selection arguments used in the query.
     * @return The value of the _data column, which is typically a file path.
     */
    @JvmStatic
    fun getDataColumn(
        context: Context, uri: Uri?, selection: String?,
        selectionArgs: Array<String>?
    ): String? {
        var cursor: Cursor? = null

        try {
            cursor = context.contentResolver.query(uri!!, null, selection, selectionArgs, null)

            if (cursor != null && cursor.moveToFirst()) {
//                final int index = cursor.getColumnIndexOrThrow(column);
                val documentId = cursor.getColumnIndexOrThrow("document_id")
                val lastModified = cursor.getColumnIndexOrThrow("last_modified")
                val mimeType = cursor.getColumnIndexOrThrow("mime_type")
                val flags = cursor.getColumnIndexOrThrow("flags")
                Log.e("ColumsData", cursor.getString(documentId))
                Log.e("ColumsData", cursor.getString(lastModified))
                Log.e("ColumsData", cursor.getString(mimeType))
                Log.e("ColumsData", cursor.getString(flags))
//                return cursor.getString(index);
            }
        } catch (e: Exception) {
            Log.e("getDataColumn", "error: " + e.message)
            e.printStackTrace()
        } finally {
            cursor?.close()
        }
        return null
    }

    @JvmStatic
    fun getPathVsmart(
        context: Context, uri: Uri, selection: String?,
        selectionArgs: Array<String>?
    ): String? {
        var cursor: Cursor? = null

        try {
            cursor = context.contentResolver.query(uri, null, selection, selectionArgs, null)

            if (cursor != null && cursor.moveToFirst()) {
//                final int index = cursor.getColumnIndexOrThrow(column);
                val documentId = cursor.getColumnIndexOrThrow("document_id")
                val lastModified = cursor.getColumnIndexOrThrow("last_modified")
                val mimeType = cursor.getColumnIndexOrThrow("mime_type")
                val flags = cursor.getColumnIndexOrThrow("flags")
                Log.e("ColumsData", cursor.getString(documentId))
                Log.e("ColumsData", cursor.getString(lastModified))
                Log.e("ColumsData", cursor.getString(mimeType))
                Log.e("ColumsData", cursor.getString(flags))
                val doc = cursor.getString(documentId)
                return if (doc.contains("home")) {
                    Environment.getExternalStorageDirectory()
                        .toString() + "/Documents/" + doc.replace("home:", "")
                } else if (doc.contains("downloads")) {
                    Environment.getExternalStorageDirectory()
                        .toString() + "/Download/" + doc.replace("downloads:", "")
                } else {
                    Environment.getExternalStorageDirectory().toString() + doc.replace(
                        "primary:",
                        "/"
                    )
                }
//                return cursor.getString(index);
            }
        } catch (e: Exception) {
            Log.e("getDataColumn", "error: " + e.message)
            e.printStackTrace()
        } finally {
            cursor?.close()
        }
        return null
    }

    @JvmStatic
    fun getNameFile(
        context: Context, uri: Uri, selection: String?,
        selectionArgs: Array<String>?
    ): String? {
        var cursor: Cursor? = null
        val column = MediaStore.Images.Media.DISPLAY_NAME

        try {
            cursor = context.contentResolver.query(uri, null, selection, selectionArgs, null)

            if (cursor != null && cursor.moveToFirst()) {
                val index = cursor.getColumnIndexOrThrow(column)
                return cursor.getString(index)
            }
        } catch (e: Exception) {
            Log.e("getNameFile", "error: " + e.message)
            e.printStackTrace()
        } finally {
            cursor?.close()
        }
        return null
    }

    private fun getDriveFilePath(uri: Uri, context: Context): String {
        val returnCursor = context.contentResolver.query(uri, null, null, null, null)
        /*
         * Get the column indexes of the data in the Cursor,
         *     * move to the first row in the Cursor, get the data,
         *     * and display it.
         * */
        val nameIndex = returnCursor!!.getColumnIndex(OpenableColumns.DISPLAY_NAME)
        returnCursor.getColumnIndex(OpenableColumns.SIZE)
        returnCursor.moveToFirst()

        val name = returnCursor.getString(nameIndex)
        val file = File(context.cacheDir, name)
        try {
            val inputStream = context.contentResolver.openInputStream(uri)
            val outputStream = FileOutputStream(file)
            var read: Int
            val maxBufferSize = 1024 * 1024
            val bytesAvailable = inputStream!!.available()

            //int bufferSize = 1024;
            val bufferSize = Math.min(bytesAvailable, maxBufferSize)

            val buffers = ByteArray(bufferSize)
            while (inputStream.read(buffers).also { read = it } != -1) {
                outputStream.write(buffers, 0, read)
            }
            Log.e("File Size", "Size " + file.length())
            inputStream.close()
            outputStream.close()
            Log.e("File Path", "Path " + file.path)
        } catch (e: Exception) {
            Log.e("Exception", e.message!!)
        }
        return file.path
    }

    private fun isGoogleDriveUri(uri: Uri): Boolean {
        return "com.google.android.apps.docs.storage" == uri.authority
                || "com.google.android.apps.docs.storage.legacy" == uri.authority
                || "com.google.android.apps.docs.editors.kix.storage.legacy" == uri.authority
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is ExternalStorageProvider.
     */
    @JvmStatic
    fun isExternalStorageDocument(uri: Uri): Boolean {
        return "com.android.externalstorage.documents" == uri.authority
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is DownloadsProvider.
     */
    @JvmStatic
    fun isDownloadsDocument(uri: Uri): Boolean {
        return "com.android.providers.downloads.documents" == uri.authority
    }

    @JvmStatic
    fun isVsmart(uri: Uri): Boolean {
        return "com.vsmart.android.externalstorage.documents" == uri.authority
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is MediaProvider.
     */
    @JvmStatic
    fun isMediaDocument(uri: Uri): Boolean {
        return "com.android.providers.media.documents" == uri.authority
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is Google Photos.
     */
    @JvmStatic
    fun isGooglePhotosUri(uri: Uri): Boolean {
        return "com.google.android.apps.photos.content" == uri.authority
    }

    @JvmStatic
    fun isDriveFile(uri: Uri): Boolean {
        if ("com.google.android.apps.docs.storage" == uri.authority) {
            return true
        }
        return "com.google.android.apps.docs.storage.legacy" == uri.authority
    }
}
