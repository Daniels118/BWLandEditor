/* Copyright (c) 2026 Daniele Lombardi / Daniels118 */
package it.ld.bw.info;

import java.io.IOException;
import it.ld.utils.EndianDataInputStream;
import it.ld.utils.EndianDataOutputStream;
import it.ld.utils.Struct;

public class GScriptOpposingCreature extends Struct {
    public int field0x0;
    public int field0x4;
    public int field0x8;

    @Override
    public void read(EndianDataInputStream str) throws IOException {
        little(str);
        field0x0 = str.readInt();
        field0x4 = str.readInt();
        field0x8 = str.readInt();
    }

    @Override
    public void write(EndianDataOutputStream str) throws IOException {
        little(str);
        str.writeInt(field0x0);
        str.writeInt(field0x4);
        str.writeInt(field0x8);
    }
}
