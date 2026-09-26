/* Copyright (c) 2026 Daniele Lombardi / Daniels118 */
package it.ld.bw.info;

import java.io.IOException;
import it.ld.utils.EndianDataInputStream;
import it.ld.utils.EndianDataOutputStream;

public class GArrowInfo extends GMobileObjectInfo {
    public int field0x104;
    public int field0x108;
    public int field0x10c;
    public int field0x110;
    public int field0x114;
    public int field0x118;
    public int field0x11c;

    @Override
    public void read(EndianDataInputStream str) throws IOException {
        little(str);
        super.read(str);
        field0x104 = str.readInt();
        field0x108 = str.readInt();
        field0x10c = str.readInt();
        field0x110 = str.readInt();
        field0x114 = str.readInt();
        field0x118 = str.readInt();
        field0x11c = str.readInt();
    }

    @Override
    public void write(EndianDataOutputStream str) throws IOException {
        little(str);
        super.write(str);
        str.writeInt(field0x104);
        str.writeInt(field0x108);
        str.writeInt(field0x10c);
        str.writeInt(field0x110);
        str.writeInt(field0x114);
        str.writeInt(field0x118);
        str.writeInt(field0x11c);
    }
}
