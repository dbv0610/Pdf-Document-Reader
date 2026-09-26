package com.wxiwei.office.wp.model

import com.wxiwei.office.constant.wp.WPModelConstant
import com.wxiwei.office.simpletext.model.AbstractElement

class CellElement : AbstractElement() {
    override fun getType(): Short = WPModelConstant.TABLE_CELL_ELEMENT
}
