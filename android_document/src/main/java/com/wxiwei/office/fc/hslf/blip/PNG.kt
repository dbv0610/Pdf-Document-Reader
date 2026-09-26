package com.wxiwei.office.fc.hslf.blip

import com.wxiwei.office.fc.hslf.model.Picture

class PNG : Bitmap() {
    override fun getData(): ByteArray? {
        return super.getData()
    }

    override fun getType(): Int {
        return Picture.PNG
    }

    override fun getSignature(): Int {
        return 0x6E00
    }
}
