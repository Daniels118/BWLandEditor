package it.ld.bw.info;

import it.ld.utils.EndianDataInputStream;
import it.ld.utils.EndianDataOutputStream;
import it.ld.utils.Struct;

public class CreatureDesireSourceTable extends Struct {
	public int field0x0;
	public int field0x4;
	public float field0x8;
	public float field0xc;
	public float field0x10;
	public String field0x14;
	public String field0x54;
	
	@Override
	public void read(EndianDataInputStream str) throws Exception {
		little(str);
		field0x0 = str.readInt();
		field0x4 = str.readInt();
		field0x8 = str.readFloat();
		field0xc = str.readFloat();
		field0x10 = str.readFloat();
		field0x14 = readFixedString(str, 0x40, true);
		field0x54 = readFixedString(str, 0x40, true);
	}
	
	@Override
	public void write(EndianDataOutputStream str) throws Exception {
		throw new RuntimeException("Method not implemented");
	}
}
