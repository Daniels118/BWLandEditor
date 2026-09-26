/* Copyright (c) 2026 Daniele Lombardi / Daniels118 */
package it.ld.bw.info;

import java.io.IOException;
import it.ld.utils.EndianDataInputStream;
import it.ld.utils.EndianDataOutputStream;
import it.ld.utils.Struct;

public class GFootballPositionInfo extends Struct {
    public float[] offset = new float[2];
    public int startState;
    public int state;
    public int side;

    @Override
    public void read(EndianDataInputStream str) throws IOException {
        little(str);
        offset = readFloatArray(str, offset);
        startState = str.readInt();
        state = str.readInt();
        side = str.readInt();
    }

    @Override
    public void write(EndianDataOutputStream str) throws IOException {
        little(str);
        writeFloatArray(str, offset);
        str.writeInt(startState);
        str.writeInt(state);
        str.writeInt(side);
    }
}
