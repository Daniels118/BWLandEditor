/* Copyright (c) 2026 Daniele Lombardi / Daniels118 */
package it.ld.bw.info;

import java.io.IOException;
import it.ld.utils.EndianDataInputStream;
import it.ld.utils.EndianDataOutputStream;
import it.ld.utils.Struct;

public class CreatureInitialDesireInfo extends Struct {
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
    public int field0x38;
    public float field0x3c;
    public float field0x40;
    public float field0x44;
    public float field0x48;
    public float field0x4c;
    public float field0x50;
    public float field0x54;
    public float field0x58;
    public int field0x5c;
    public float field0x60;
    public float field0x64;
    public float field0x68;
    public int field0x6c;
    public String field0x70 = "";
    public String field0xb0 = "";
    public String field0xf0 = "";
    public String field0x130 = "";
    public String field0x170 = "";

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
        field0x38 = str.readInt();
        field0x3c = str.readFloat();
        field0x40 = str.readFloat();
        field0x44 = str.readFloat();
        field0x48 = str.readFloat();
        field0x4c = str.readFloat();
        field0x50 = str.readFloat();
        field0x54 = str.readFloat();
        field0x58 = str.readFloat();
        field0x5c = str.readInt();
        field0x60 = str.readFloat();
        field0x64 = str.readFloat();
        field0x68 = str.readFloat();
        field0x6c = str.readInt();
        field0x70 = readFixedString(str, 64, true);
        field0xb0 = readFixedString(str, 64, true);
        field0xf0 = readFixedString(str, 64, true);
        field0x130 = readFixedString(str, 64, true);
        field0x170 = readFixedString(str, 64, true);
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
        str.writeInt(field0x38);
        str.writeFloat(field0x3c);
        str.writeFloat(field0x40);
        str.writeFloat(field0x44);
        str.writeFloat(field0x48);
        str.writeFloat(field0x4c);
        str.writeFloat(field0x50);
        str.writeFloat(field0x54);
        str.writeFloat(field0x58);
        str.writeInt(field0x5c);
        str.writeFloat(field0x60);
        str.writeFloat(field0x64);
        str.writeFloat(field0x68);
        str.writeInt(field0x6c);
        writeFixedString(str, field0x70, 64);
        writeFixedString(str, field0xb0, 64);
        writeFixedString(str, field0xf0, 64);
        writeFixedString(str, field0x130, 64);
        writeFixedString(str, field0x170, 64);
    }
}
