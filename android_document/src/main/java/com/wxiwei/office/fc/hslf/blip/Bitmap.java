package com.wxiwei.office.fc.hslf.blip;

import com.wxiwei.office.fc.hslf.usermodel.PictureData;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

public abstract class Bitmap extends PictureData {
    public byte[] getData() {
        byte[] rawdata = getRawData();
        if (rawdata == null || rawdata.length <= 17) return new byte[0];
        byte[] imgdata = new byte[rawdata.length - 17];
        System.arraycopy(rawdata, 17, imgdata, 0, imgdata.length);
        return imgdata;
    }

    public void setData(byte[] data) throws IOException {
        if (data == null) return;
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        byte[] checksum = getChecksum(data);
        out.write(checksum);
        out.write(0);
        out.write(data);
        setRawData(out.toByteArray());
    }
}
