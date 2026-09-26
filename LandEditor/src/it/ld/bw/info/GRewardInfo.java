/* Copyright (c) 2026 Daniele Lombardi / Daniels118 */
package it.ld.bw.info;

import java.io.IOException;
import it.ld.utils.EndianDataInputStream;
import it.ld.utils.EndianDataOutputStream;

public class GRewardInfo extends GMobileObjectInfo {
    public int seed;
    public int gestureType;
    public int powerUp;
    public int helpTextEnum;
    public float beliefValue;
    public int mobileStatic;
    public int scaffold;

    @Override
    public void read(EndianDataInputStream str) throws IOException {
        little(str);
        super.read(str);
        seed = str.readInt();
        gestureType = str.readInt();
        powerUp = str.readInt();
        helpTextEnum = str.readInt();
        beliefValue = str.readFloat();
        mobileStatic = str.readInt();
        scaffold = str.readInt();
    }

    @Override
    public void write(EndianDataOutputStream str) throws IOException {
        little(str);
        super.write(str);
        str.writeInt(seed);
        str.writeInt(gestureType);
        str.writeInt(powerUp);
        str.writeInt(helpTextEnum);
        str.writeFloat(beliefValue);
        str.writeInt(mobileStatic);
        str.writeInt(scaffold);
    }
}
