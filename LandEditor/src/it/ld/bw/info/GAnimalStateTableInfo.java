package it.ld.bw.info;

import it.ld.utils.EndianDataInputStream;
import it.ld.utils.EndianDataOutputStream;
import it.ld.utils.Struct;

public class GAnimalStateTableInfo extends Struct {
	public int field0x0;
	public int field0x4;
	public float field0x8;
	public int field0xc;
	public int field0x10;
	public int field0x14;
	public int field0x18;
	public int field0x1c;
	public int field0x20;
	public String name;
	public int field0xa4;
	
	@Override
	public void read(EndianDataInputStream str) throws Exception {
		little(str);
		field0x0 = str.readInt();
		field0x4 = str.readInt();
		field0x8 = str.readFloat();
		field0xc = str.readInt();
		field0x10 = str.readInt();
		field0x14 = str.readInt();
		field0x18 = str.readInt();
		field0x1c = str.readInt();
		field0x20 = str.readInt();
		name = readFixedString(str, 0x80, true);
		field0xa4 = str.readInt();
	}

	@Override
	public void write(EndianDataOutputStream str) throws Exception {
		throw new RuntimeException("Method not implemented");
	}
}
