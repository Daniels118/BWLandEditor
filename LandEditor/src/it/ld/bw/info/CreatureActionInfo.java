package it.ld.bw.info;

import it.ld.utils.EndianDataInputStream;
import it.ld.utils.EndianDataOutputStream;
import it.ld.utils.Struct;

public class CreatureActionInfo extends Struct {
	public float field0x0;
	public float field0x4;
	public float field0x8;
	public int field0xc;
	public float field0x10;
	public String name;
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
	public int field0x78;
	public int field0x7c;
	public int field0x80;
	public int field0x84;
	public int field0x88;
	public int field0x8c;
	public int field0x90;
	public int field0x94;
	public int field0x98;
	public int field0x9c;
	public int field0xa0;
	public int field0xa4;
	public int field0xa8;
	public int field0xac;
	public int field0xb0;
	public int field0xb4;
	public int field0xb8;
	public int field0xbc;
	public float field0xc0;
	public float field0xc4;
	public int field0xc8;
	public float field0xcc;
	public float field0xd0;
	public float field0xd4;
	public int field0xd8;
	public int field0xdc;
	public int field0xe0;
	public int field0xe4;
	public float field0xe8;
	public float field0xec;
	public int field0xf0;
	public int field0xf4;
	public int field0xf8;
	public int field0xfc;	//since patch 1.20
	
	@Override
	public void read(EndianDataInputStream str) throws Exception {
		little(str);
		field0x0 = str.readFloat();
		field0x4 = str.readFloat();
		field0x8 = str.readFloat();
		field0xc = str.readInt();
		field0x10 = str.readFloat();
		name = readFixedString(str, 0x20, true);
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
		field0x78 = str.readInt();
		field0x7c = str.readInt();
		field0x80 = str.readInt();
		field0x84 = str.readInt();
		field0x88 = str.readInt();
		field0x8c = str.readInt();
		field0x90 = str.readInt();
		field0x94 = str.readInt();
		field0x98 = str.readInt();
		field0x9c = str.readInt();
		field0xa0 = str.readInt();
		field0xa4 = str.readInt();
		field0xa8 = str.readInt();
		field0xac = str.readInt();
		field0xb0 = str.readInt();
		field0xb4 = str.readInt();
		field0xb8 = str.readInt();
		field0xbc = str.readInt();
		field0xc0 = str.readFloat();
		field0xc4 = str.readFloat();
		field0xc8 = str.readInt();
		field0xcc = str.readFloat();
		field0xd0 = str.readFloat();
		field0xd4 = str.readFloat();
		field0xd8 = str.readInt();
		field0xdc = str.readInt();
		field0xe0 = str.readInt();
		field0xe4 = str.readInt();
		field0xe8 = str.readFloat();
		field0xec = str.readFloat();
		field0xf0 = str.readInt();
		field0xf4 = str.readInt();
		field0xf8 = str.readInt();
		field0xfc = str.readInt();	//since patch 1.20
	}
	
	@Override
	public void write(EndianDataOutputStream str) throws Exception {
		throw new RuntimeException("Method not implemented");
	}
	
	@Override
    public String toString() {
    	return this.getClass().getSimpleName() + "(" + name + ")";
    }
}
