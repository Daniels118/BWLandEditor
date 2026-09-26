package it.ld.bw.info;

import it.ld.utils.EndianDataInputStream;
import it.ld.utils.EndianDataOutputStream;
import it.ld.utils.Struct;

public class GVillagerStateTableInfo extends Struct {
	public int field0x0;
	public int field0x4;
	public float field0x8;
	public int isFinalState;
	public int field0x10;
	public int field0x14;
	public int isScriptState;
	public int isScriptInterruptableState;
	public int field0x20;
	public int field0x24;
	public String name;
	public int field0xa8;
	public int field0xac;
	public int field0xb0;
	public int field0xb4;
	public int field0xb8;
	public int field0xbc;
	public int field0xc0;
	public int field0xc4;
	public float field0xc8;
	public float field0xcc;
	public int field0xd0;
	public int field0xd4;
	public int field0xd8;
	public int field0xdc;
	public int field0xe0;
	public int field0xe4;
	public int field0xe8;
	public int field0xec;
	public int field0xf0;
	public int field0xf4;
	public float field0xf8;
	public int field0xfc;
	public int field0x100;
	
	@Override
	public void read(EndianDataInputStream str) throws Exception {
		little(str);
		field0x0 = str.readInt();
		field0x4 = str.readInt();
		field0x8 = str.readFloat();
		isFinalState = str.readInt();
		field0x10 = str.readInt();
		field0x14 = str.readInt();
		isScriptState = str.readInt();
		isScriptInterruptableState = str.readInt();
		field0x20 = str.readInt();
		field0x24 = str.readInt();
		name = readFixedString(str, 0x80, true);
		field0xa8 = str.readInt();
		field0xac = str.readInt();
		field0xb0 = str.readInt();
		field0xb4 = str.readInt();
		field0xb8 = str.readInt();
		field0xbc = str.readInt();
		field0xc0 = str.readInt();
		field0xc4 = str.readInt();
		field0xc8 = str.readFloat();
		field0xcc = str.readFloat();
		field0xd0 = str.readInt();
		field0xd4 = str.readInt();
		field0xd8 = str.readInt();
		field0xdc = str.readInt();
		field0xe0 = str.readInt();
		field0xe4 = str.readInt();
		field0xe8 = str.readInt();
		field0xec = str.readInt();
		field0xf0 = str.readInt();
		field0xf4 = str.readInt();
		field0xf8 = str.readFloat();
		field0xfc = str.readInt();
		field0x100 = str.readInt();
	}
	
	@Override
	public void write(EndianDataOutputStream str) throws Exception {
		throw new RuntimeException("Method not implemented");
	}
}
