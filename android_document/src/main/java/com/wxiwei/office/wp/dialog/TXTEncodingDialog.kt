package com.wxiwei.office.wp.dialog

import com.wxiwei.office.system.*

import android.content.Context
import android.content.res.Configuration
import android.view.View
import android.webkit.WebView
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.LinearLayout.LayoutParams
import android.widget.ScrollView
import android.widget.Spinner
import com.wxiwei.office.system.IControl
import com.wxiwei.office.system.IDialogAction
import com.wxiwei.office.system.beans.ADialog
import java.io.BufferedReader
import java.io.File
import java.io.FileInputStream
import java.io.InputStreamReader
import java.util.Vector

class TXTEncodingDialog(
    control: IControl,
    context: Context,
    action: IDialogAction?,
    model: Vector<Any>,
    dialogID: Int
) : ADialog(
    control,
    context,
    action!!,
    model,
    dialogID,
        action.getControl()!!.getMainFrame().getLocalString("DIALOG_ENCODING_TITLE")
) {
    private var spinner: Spinner? = null
    private var previewText: WebView? = null
    private var buffer: CharArray? = CharArray(1024)
    private var scrollView: ScrollView? = null

    init {
        init(context)
    }

    fun init(context: Context) {
        val adapter = ArrayAdapter(context, android.R.layout.simple_spinner_item, ENCODING_ITEMS)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner = Spinner(context).also { spinner ->
            spinner.adapter = adapter
            spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                    setPreviewText(spinner.selectedItem.toString())
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {
                }
            }
            dialogFrame!!.addView(spinner)
        }

        previewText = WebView(context).also { webView ->
            webView.setPadding(5, 2, 5, 2)
        }
        scrollView = ScrollView(context).also { scroll ->
            scroll.isFillViewport = true
            scroll.addView(previewText)
            dialogFrame!!.addView(scroll)
        }

        ok = Button(context).also { button ->
            button.text = action!!.getControl()!!.getMainFrame().getLocalString("BUTTON_OK")
            button.setOnClickListener(this)
            dialogFrame!!.addView(button)
        }
    }

    override fun doLayout() {
        var mWidth = context.resources.displayMetrics.widthPixels
        var mHeight = context.resources.displayMetrics.heightPixels
        mHeight -= window!!.decorView.height - dialogFrame!!.height
        mWidth -= GAP * 10
        mHeight -= GAP * 10
        val params = LayoutParams(mWidth - GAP * 2, mHeight - (spinner?.bottom ?: 0) - ok!!.height)
        params.leftMargin = GAP
        params.rightMargin = GAP
        params.bottomMargin = GAP
        scrollView?.layoutParams = params
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        doLayout()
    }

    override fun onClick(v: View?) {
        val vector = Vector<Any>()
        vector.add(spinner?.selectedItem.toString())
        action!!.doAction(dialogID, vector)
        dismiss()
    }

    override fun onBackPressed() {
        val vector = Vector<Any>()
        vector.add(BACK_PRESSED)
        action!!.doAction(dialogID, vector)
        super.onBackPressed()
    }

    private fun setPreviewText(code: String?) {
        try {
            val chars = buffer ?: return
            if (code != null) {
                val file = File(model!![0].toString())
                val br = BufferedReader(InputStreamReader(FileInputStream(file), code))
                val len = br.read(chars)
                if (len > 0) {
                    val str = "<a>" + String(chars, 0, len) + "</a>"
                    previewText?.loadDataWithBaseURL(null, str.replace("\\r\\n".toRegex(), "<br />"), "text/html", "UTF-8", null)
                }
                br.close()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun dispose() {
        super.dispose()
        spinner = null
        previewText = null
        buffer = null
        scrollView = null
    }

    companion object {
        const val BACK_PRESSED = "BP"

        @JvmField
        val ENCODING_ITEMS = arrayOf(
            "GBK", "GB2312", "BIG5", "Unicode", "UTF-8", "UTF-16",
            "UTF-16LE", "UTF-16BE", "UTF-7", "UTF-32", "UTF-32LE",
            "UTF-32BE", "US-ASCII", "ISO-8859-1", "ISO-8859-2", "ISO-8859-3",
            "ISO-8859-4", "ISO-8859-5", "ISO-8859-6", "ISO-8859-7", "ISO-8859-8",
            "ISO-8859-9", "ISO-8859-10", "ISO-8859-11", "ISO-8859-13", "ISO-8859-14",
            "ISO-8859-15", "ISO-8859-16", "ISO-2022-JP", "ISO-2022-KR", "ISO-2022-CN",
            "ISO-2022-CN-EXT", "UCS-2", "UCS-4", "Windows-1250", "Windows-1251",
            "Windows-1252", "Windows-1253", " Windows-1254", "Windows-1255", "Windows-1256",
            "Windows-1257", "Windows-1258", "KOI8-R", "Shift_JIS", "CP864", "EUC-JP",
            "EUC-KR", "BOCU-1", "CESU-8", "SCSU", "HZ-GB-2312", "TIS-620", "macintosh",
            "x-UTF-16LE-BOM", "x-iscii-as", "x-iscii-be", "x-iscii-de", "x-iscii-gu",
            "x-iscii-ka", "x-iscii-ma", "x-iscii-or", "x-iscii-pa", "x-iscii-ta",
            "x-iscii-te", "x-mac-cyrillic"
        )
    }
}
