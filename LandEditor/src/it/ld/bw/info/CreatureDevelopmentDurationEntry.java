/* Copyright (c) 2026 Daniele Lombardi / Daniels118 */
package it.ld.bw.info;

import java.io.IOException;
import it.ld.utils.EndianDataInputStream;
import it.ld.utils.EndianDataOutputStream;
import it.ld.utils.Struct;

public class CreatureDevelopmentDurationEntry extends Struct {
    public int field0x0;
    public int field0x4;
    public int field0x8;
    public int field0xc;
    public int field0x10;
    public int field0x14;
    public int field0x18;
    public int field0x1c;
    public int field0x20;
    public int field0x24;
    public int field0x28;
    public int field0x2c;
    public int field0x30;
    public int field0x34;

    @Override
    public void read(EndianDataInputStream str) throws IOException {
        little(str);
        field0x0 = str.readInt();
        field0x4 = str.readInt();
        field0x8 = str.readInt();
        field0xc = str.readInt();
        field0x10 = str.readInt();
        field0x14 = str.readInt();
        field0x18 = str.readInt();
        field0x1c = str.readInt();
        field0x20 = str.readInt();
        field0x24 = str.readInt();
        field0x28 = str.readInt();
        field0x2c = str.readInt();
        field0x30 = str.readInt();
        field0x34 = str.readInt();
    }

    @Override
    public void write(EndianDataOutputStream str) throws IOException {
        little(str);
        str.writeInt(field0x0);
        str.writeInt(field0x4);
        str.writeInt(field0x8);
        str.writeInt(field0xc);
        str.writeInt(field0x10);
        str.writeInt(field0x14);
        str.writeInt(field0x18);
        str.writeInt(field0x1c);
        str.writeInt(field0x20);
        str.writeInt(field0x24);
        str.writeInt(field0x28);
        str.writeInt(field0x2c);
        str.writeInt(field0x30);
        str.writeInt(field0x34);
    }
}
