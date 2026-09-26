/* Copyright (c) 2026 Daniele Lombardi / Daniels118 */
package it.ld.bw.info;

import java.io.IOException;
import it.ld.utils.EndianDataInputStream;
import it.ld.utils.EndianDataOutputStream;

public class GFieldInfo extends GMultiMapFixedInfo {
    public float[] size = new float[2];
    public int maxNoFarmersPerField;
    public int[] gateNumberMissing = new int[2];
    public int fence;
    public int deleteIfSame;
    public int cropPlanted;
    public int maxNumFarmers;
    public int nurtureEffect;
    public float rainEffectMultiplier;

    @Override
    public void read(EndianDataInputStream str) throws IOException {
        little(str);
        super.read(str);
        size = readFloatArray(str, size);
        maxNoFarmersPerField = str.readInt();
        gateNumberMissing = readIntArray(str, gateNumberMissing);
        fence = str.readInt();
        deleteIfSame = str.readInt();
        cropPlanted = str.readInt();
        maxNumFarmers = str.readInt();
        nurtureEffect = str.readInt();
        rainEffectMultiplier = str.readFloat();
    }

    @Override
    public void write(EndianDataOutputStream str) throws IOException {
        little(str);
        super.write(str);
        writeFloatArray(str, size);
        str.writeInt(maxNoFarmersPerField);
        writeIntArray(str, gateNumberMissing);
        str.writeInt(fence);
        str.writeInt(deleteIfSame);
        str.writeInt(cropPlanted);
        str.writeInt(maxNumFarmers);
        str.writeInt(nurtureEffect);
        str.writeFloat(rainEffectMultiplier);
    }
}
