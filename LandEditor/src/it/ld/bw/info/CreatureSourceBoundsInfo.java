package it.ld.bw.info;

import it.ld.utils.EndianDataInputStream;
import it.ld.utils.EndianDataOutputStream;
import it.ld.utils.Struct;

public class CreatureSourceBoundsInfo extends Struct {
	public float[] field0x0 = new float[3];
	
	@Override
	public void read(EndianDataInputStream str) throws Exception {
		little(str);
		field0x0 = readFloatArray(str, field0x0);
	}
	
	@Override
	public void write(EndianDataOutputStream str) throws Exception {
		throw new RuntimeException("Method not implemented");
	}
}
