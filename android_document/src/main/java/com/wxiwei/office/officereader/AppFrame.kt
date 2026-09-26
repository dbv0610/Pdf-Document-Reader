package com.wxiwei.office.officereader

import android.content.Context
import android.graphics.Color
import android.widget.LinearLayout

class AppFrame(context: Context) : LinearLayout(context) {
    init { orientation = VERTICAL; setBackgroundColor(Color.LTGRAY) }
    fun dispose() { /* removeAllViews() */ }
}
