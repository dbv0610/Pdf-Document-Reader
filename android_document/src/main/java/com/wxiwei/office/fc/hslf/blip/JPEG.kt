package com.wxiwei.office.fc.hslf.blip

import com.wxiwei.office.fc.hslf.model.Picture

class JPEG : Bitmap() {
    override fun getType(): Int {
        return Picture.JPEG
    }

    override fun getData(): ByteArray? {
        return super.getData()
    }

    override fun setData(data: ByteArray?) {
        super.setData(data)
    }

    override fun getSignature(): Int {
        return 0x46A0
    }
}
