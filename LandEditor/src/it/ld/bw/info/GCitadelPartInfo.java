/* Copyright (c) 2026 Daniele Lombardi / Daniels118 */
package it.ld.bw.info;

import java.io.IOException;
import it.ld.utils.EndianDataInputStream;
import it.ld.utils.EndianDataOutputStream;

public class GCitadelPartInfo extends GMultiMapFixedInfo {
    public int citadelType;
    public int meshType;
    public float startLife;
    public int startStrength;
    public float startDefence;

    @Override
    public void read(EndianDataInputStream str) throws IOException {
        little(str);
        super.read(str);
        citadelType = str.readInt();
        meshType = str.readInt();
        startLife = str.readFloat();
        startStrength = str.readInt();
        startDefence = str.readFloat();
    }

    @Override
    public void write(EndianDataOutputStream str) throws IOException {
        little(str);
        super.write(str);
        str.writeInt(citadelType);
        str.writeInt(meshType);
        str.writeFloat(startLife);
        str.writeInt(startStrength);
        str.writeFloat(startDefence);
    }
}
