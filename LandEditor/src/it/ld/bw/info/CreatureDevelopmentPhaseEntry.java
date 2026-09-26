/* Copyright (c) 2026 Daniele Lombardi / Daniels118 */
package it.ld.bw.info;

import java.io.IOException;
import it.ld.utils.EndianDataInputStream;
import it.ld.utils.EndianDataOutputStream;
import it.ld.utils.Struct;

public class CreatureDevelopmentPhaseEntry extends Struct {
    public String name = "";
    public float field0x30;
    public float field0x34;
    public float field0x38;
    public int field0x3c;
    public int field0x40;
    public int field0x44;
    public int field0x48;
    public int field0x4c;
    public int field0x50;
    public int field0x54;
    public int field0x58;
    public int field0x5c;
    public int field0x60;
    public int field0x64;
    public int field0x68;
    public int field0x6c;
    public int field0x70;

    @Override
    public void read(EndianDataInputStream str) throws IOException {
        little(str);
        name = readFixedString(str, 48, true);
        field0x30 = str.readFloat();
        field0x34 = str.readFloat();
        field0x38 = str.readFloat();
        field0x3c = str.readInt();
        field0x40 = str.readInt();
        field0x44 = str.readInt();
        field0x48 = str.readInt();
        field0x4c = str.readInt();
        field0x50 = str.readInt();
        field0x54 = str.readInt();
        field0x58 = str.readInt();
        field0x5c = str.readInt();
        field0x60 = str.readInt();
        field0x64 = str.readInt();
        field0x68 = str.readInt();
        field0x6c = str.readInt();
        field0x70 = str.readInt();
    }

    @Override
    public void write(EndianDataOutputStream str) throws IOException {
        little(str);
        writeFixedString(str, name, 48);
        str.writeFloat(field0x30);
        str.writeFloat(field0x34);
        str.writeFloat(field0x38);
        str.writeInt(field0x3c);
        str.writeInt(field0x40);
        str.writeInt(field0x44);
        str.writeInt(field0x48);
        str.writeInt(field0x4c);
        str.writeInt(field0x50);
        str.writeInt(field0x54);
        str.writeInt(field0x58);
        str.writeInt(field0x5c);
        str.writeInt(field0x60);
        str.writeInt(field0x64);
        str.writeInt(field0x68);
        str.writeInt(field0x6c);
        str.writeInt(field0x70);
    }
    
    @Override
    public String toString() {
    	return name != null ? name : super.toString();
    }
}
