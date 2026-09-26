package com.wxiwei.office.model

import android.os.Environment
import java.io.File

class Constants {
    interface logEventName {
        companion object {
            const val SPLASH_LOADING_AD = "splash_loading_ad"
            const val SPLASH_NO_INTERNET = "splash_no_internet"
            const val SPLASH_FROM = "splash_open_from"
            const val SPLASH_OPEN_FROM_OTHER_DATA = "splash_open_from_other_data"
            const val SPLASH_OPEN_FROM_OTHER_ERROR = "splash_open_from_other_error"
        }
    }

    companion object {
        const val TIME_FOR_WAITING_FULL_AD_SPLASH: Long = 30000
        const val TIME_FOR_WAITING_SCREEN_SHOT: Long = 180

        const val REQUEST_WRITE_EXTERNAL_STORAGE_PERMISSION = 1
        const val REQUEST_MANAGER_EXTERNAL_STORAGE_PERMISSION = 2

        const val REQUEST_CODE_FOR_START_VIEW = 45

        const val EXTRA_OPEN_IN_APP = "openInApp"
        const val PREF_KEY_CLICK_RATED = "CLICK_RATED"
        const val PREF_KEY_CLICK_BACK_PRESS_APP = "ON_BACK_PRESS_APP"
        const val PREF_KEY_CLICK_MAYBE_LATER = "CLICK_MAYBE_LATER"
        const val PREF_KEY_FILE_INFO = "PREF_KEY_FILE_CACHE_INFO"

        const val ORDER_TYPE_NAME = 0
        const val ORDER_TYPE_CREATED = 1
        const val ORDER_TYPE_ACCESSED = 2

        const val EXTRA_KEY_FILE_INFO = "EXTRA_KEY_FILE_INFO"
        const val EXTRA_KEY_FILE_INFO_LIST = "EXTRA_KEY_FILE_INFO_LIST"

        const val ITEM_TYPE_FILE = "FILE"
        const val ITEM_TYPE_ADS = "ADS"
        const val FILE_NAME_WORD_CONVERTED_TO_PDF = "Word_Converted_To_PDF_alldocumentreader.pdf"
        const val OPENING_SCREEN_SHOT_FILE_PATH = "OPENING_SCREEN_SHOT_FILE_PATH"
        const val FLAG_EDITOR_IMAGE = "FLAG_EDITOR_IMAGE"
        val SCREENSHOT_FOLDER_PATH = File(
            Environment.getExternalStorageDirectory(),
            "AllDocumentReaders/Screenshots"
        )
        val SNAP_SCREEN_FILE_PATH = Environment.getExternalStorageDirectory().toString() + File.separator + "allDocumentsReader_screenshot.png"
        val EXTENSIONS_DOC_FILE = arrayOf("txt", "pdf", "ppt", "pptx", "doc", "docx", "xls", "xlsx")
        val EXTENSIONS_SCREENSHOT_FILE = arrayOf("png", "jpg", "JPG", "PNG", "JPEG")

    }
}
