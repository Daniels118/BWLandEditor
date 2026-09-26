package it.ld.bw.info;

import it.ld.utils.EndianDataInputStream;
import it.ld.utils.EndianDataOutputStream;
import it.ld.utils.Struct;

public class CreatureMimicInfo extends Struct {
	public String name;
	public float field0x80;
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
	
	@Override
	public void read(EndianDataInputStream str) throws Exception {
		little(str);
		name = readFixedString(str, 0x80, true);
		field0x80 = str.readFloat();
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
	}
	
	@Override
	public void write(EndianDataOutputStream str) throws Exception {
		throw new RuntimeException("Method not implemented");
	}
}
