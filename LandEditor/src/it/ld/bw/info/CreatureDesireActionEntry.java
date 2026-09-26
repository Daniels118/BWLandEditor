package it.ld.bw.info;

import it.ld.utils.EndianDataInputStream;
import it.ld.utils.EndianDataOutputStream;
import it.ld.utils.Struct;

public class CreatureDesireActionEntry extends Struct {
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
	public int field0x74;
	
	@Override
	public void read(EndianDataInputStream str) throws Exception {
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
		field0x74 = str.readInt();
	}
	
	@Override
	public void write(EndianDataOutputStream str) throws Exception {
		throw new RuntimeException("Method not implemented");
	}
}
