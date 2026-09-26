package it.ld.bw.info;

import it.ld.utils.EndianDataInputStream;
import it.ld.utils.EndianDataOutputStream;
import it.ld.utils.Struct;

public class CreatureDesireDependency extends Struct {
	public int field0x0;
	public float field0x4;
	public float field0x8;
	public float field0xc;
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
	public float field0x40;
	public int field0x44;
	public int field0x48;
	public int field0x4c;
	public int field0x50;
	public int field0x54;
	public int field0x58;
	public float field0x5c;
	public float field0x60;
	public float field0x64;
	public float field0x68;
	public float field0x6c;
	public float field0x70;
	public float field0x74;
	public float field0x78;
	public float field0x7c;
	public float field0x80;
	public float field0x84;
	public float field0x88;
	public float field0x8c;
	public float field0x90;
	public float field0x94;
	public int field0x98;
	public int field0x9c;
	
	@Override
	public void read(EndianDataInputStream str) throws Exception {
		little(str);
		field0x0 = str.readInt();
		field0x4 = str.readFloat();
		field0x8 = str.readFloat();
		field0xc = str.readFloat();
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
		field0x40 = str.readFloat();
		field0x44 = str.readInt();
		field0x48 = str.readInt();
		field0x4c = str.readInt();
		field0x50 = str.readInt();
		field0x54 = str.readInt();
		field0x58 = str.readInt();
		field0x5c = str.readFloat();
		field0x60 = str.readFloat();
		field0x64 = str.readFloat();
		field0x68 = str.readFloat();
		field0x6c = str.readFloat();
		field0x70 = str.readFloat();
		field0x74 = str.readFloat();
		field0x78 = str.readFloat();
		field0x7c = str.readFloat();
		field0x80 = str.readFloat();
		field0x84 = str.readFloat();
		field0x88 = str.readFloat();
		field0x8c = str.readFloat();
		field0x90 = str.readFloat();
		field0x94 = str.readFloat();
		field0x98 = str.readInt();
		field0x9c = str.readInt();
	}
	
	@Override
	public void write(EndianDataOutputStream str) throws Exception {
		throw new RuntimeException("Method not implemented");
	}
}
