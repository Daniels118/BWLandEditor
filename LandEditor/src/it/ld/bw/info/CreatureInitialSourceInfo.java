package it.ld.bw.info;

import it.ld.utils.EndianDataInputStream;
import it.ld.utils.EndianDataOutputStream;
import it.ld.utils.Struct;

public class CreatureInitialSourceInfo extends Struct {
	public float field0x0;
	public float field0x4;
	public float field0x8;
	public float field0xc;
	public float field0x10;
	public float field0x14;
	public float field0x18;
	public float field0x1c;
	public float field0x20;
	public float field0x24;
	public float field0x28;
	public float field0x2c;
	public float field0x30;
	public float field0x34;
	public float field0x38;
	public float field0x3c;
	public float field0x40;
	
	@Override
	public void read(EndianDataInputStream str) throws Exception {
		little(str);
		field0x0 = str.readFloat();
		field0x4 = str.readFloat();
		field0x8 = str.readFloat();
		field0xc = str.readFloat();
		field0x10 = str.readFloat();
		field0x14 = str.readFloat();
		field0x18 = str.readFloat();
		field0x1c = str.readFloat();
		field0x20 = str.readFloat();
		field0x24 = str.readFloat();
		field0x28 = str.readFloat();
		field0x2c = str.readFloat();
		field0x30 = str.readFloat();
		field0x34 = str.readFloat();
		field0x38 = str.readFloat();
		field0x3c = str.readFloat();
		field0x40 = str.readFloat();
	}
	
	@Override
	public void write(EndianDataOutputStream str) throws Exception {
		throw new RuntimeException("Method not implemented");
	}
}
