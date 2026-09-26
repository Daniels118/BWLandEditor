/* Copyright (c) 2026 Daniele Lombardi / Daniels118 */
package it.ld.bw.info;

import java.io.IOException;
import it.ld.utils.EndianDataInputStream;
import it.ld.utils.EndianDataOutputStream;
import it.ld.utils.Struct;

public class CreatureActionKnownAboutEntry extends Struct {
    public String field0x0 = "";
    public int field0x40;
    public int field0x44;
    public float field0x48;
    public int field0x4c;
    public int field0x50;
    public int field0x54;

    @Override
    public void read(EndianDataInputStream str) throws IOException {
        little(str);
        field0x0 = readFixedString(str, 64, true);
        field0x40 = str.readInt();
        field0x44 = str.readInt();
        field0x48 = str.readFloat();
        field0x4c = str.readInt();
        field0x50 = str.readInt();
        field0x54 = str.readInt();
    }

    @Override
    public void write(EndianDataOutputStream str) throws IOException {
        little(str);
        writeFixedString(str, field0x0, 64);
        str.writeInt(field0x40);
        str.writeInt(field0x44);
        str.writeFloat(field0x48);
        str.writeInt(field0x4c);
        str.writeInt(field0x50);
        str.writeInt(field0x54);
    }
}
