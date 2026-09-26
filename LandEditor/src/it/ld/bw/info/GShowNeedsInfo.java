/* Copyright (c) 2026 Daniele Lombardi / Daniels118 */
package it.ld.bw.info;

import java.io.IOException;
import it.ld.utils.EndianDataInputStream;
import it.ld.utils.EndianDataOutputStream;

public class GShowNeedsInfo extends GObjectInfo {
    public int mesh;
    public int animId;
    public float maxNeedValue;
    public float showNeedGreater;
    public float maxHieght;

    @Override
    public void read(EndianDataInputStream str) throws IOException {
        little(str);
        super.read(str);
        mesh = str.readInt();
        animId = str.readInt();
        maxNeedValue = str.readFloat();
        showNeedGreater = str.readFloat();
        maxHieght = str.readFloat();
    }

    @Override
    public void write(EndianDataOutputStream str) throws IOException {
        little(str);
        super.write(str);
        str.writeInt(mesh);
        str.writeInt(animId);
        str.writeFloat(maxNeedValue);
        str.writeFloat(showNeedGreater);
        str.writeFloat(maxHieght);
    }
}
