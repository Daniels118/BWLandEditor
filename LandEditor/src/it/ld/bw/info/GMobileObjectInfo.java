/* Copyright (c) 2026 Daniele Lombardi / Daniels118 */
package it.ld.bw.info;

import java.io.IOException;
import it.ld.utils.EndianDataInputStream;
import it.ld.utils.EndianDataOutputStream;

public class GMobileObjectInfo extends GMobileInfo {
    public int mobileType;
    public int meshId;
    public float startScale;
    public float finalScale;

    @Override
    public void read(EndianDataInputStream str) throws IOException {
        little(str);
        super.read(str);
        mobileType = str.readInt();
        meshId = str.readInt();
        startScale = str.readFloat();
        finalScale = str.readFloat();
    }

    @Override
    public void write(EndianDataOutputStream str) throws IOException {
        little(str);
        super.write(str);
        str.writeInt(mobileType);
        str.writeInt(meshId);
        str.writeFloat(startScale);
        str.writeFloat(finalScale);
    }
}
