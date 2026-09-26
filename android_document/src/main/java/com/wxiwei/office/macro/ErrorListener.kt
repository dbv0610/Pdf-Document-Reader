/*
 * 文件名称:          ErrorListener.java
 *
 * 编译器:            android2.2
 * 时间:              下午2:55:51
 */
package com.wxiwei.office.macro

import com.wxiwei.office.system.ErrorUtil

/**
 * error listener
 */
interface ErrorListener {

    /**
     * when engine error occurred callback this method
     *
     * @param   errorCode  error code
     *          @see ErrorListener#INSUFFICIENT_MEMORY
     *          @see ErrorListener#SYSTEM_CRASH
     *          @see ErrorListener#BAD_FILE
     *          @see ErrorListener#OLD_DOCUMENT
     *          @see ErrorListener#RTF_DOCUMENT
     */
    fun error(errorCode: Int)

    /**
     * when need destroy office engine instance callback this method
     */
    //public void destroyEngine();

    companion object {
        const val INSUFFICIENT_MEMORY = ErrorUtil.INSUFFICIENT_MEMORY //"Unable to complete operation due to insufficient memory";
        //
        const val SYSTEM_CRASH = ErrorUtil.SYSTEM_CRASH //"System crash, terminate running";
        //
        const val BAD_FILE = ErrorUtil.BAD_FILE //"Bad file";
        //
        const val OLD_DOCUMENT = ErrorUtil.OLD_DOCUMENT //"The document is too old - Office 95 or older, which is not supported";
        //
        const val PARSE_ERROR = ErrorUtil.PARSE_ERROR //"File parsing error";
        //
        const val RTF_DOCUMENT = ErrorUtil.RTF_DOCUMENT //"The document is really a RTF file, which is not supported";
        //
        const val PASSWORD_DOCUMENT = ErrorUtil.PASSWORD_DOCUMENT //"It's a document with password which cannot parsed";
        //
        const val PASSWORD_INCORRECT = ErrorUtil.PASSWORD_INCORRECT //"document's password sent to engine is error";
        //
        const val SD_CARD_ERROR = ErrorUtil.SD_CARD_ERROR //"SD Card read or write error"
        //
        const val SD_CARD_WRITEDENIED = ErrorUtil.SD_CARD_WRITEDENIED //"SD Card Write Permission denied"
        //
        const val SD_CARD_NOSPACELEFT = ErrorUtil.SD_CARD_NOSPACELEFT //"SD Card has no space left"
    }
}
