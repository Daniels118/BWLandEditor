package it.ld.bw.l3d;

import it.ld.utils.EndianDataInputStream;
import it.ld.utils.EndianDataOutputStream;
import it.ld.utils.Struct;

public class L3DFootprintFooter extends Struct {
	public int unknown1;
	public float unknown2;
	public int unknown3;
	
	@Override
	public void read(EndianDataInputStream str) throws Exception {
		little(str);
		unknown1 = str.readInt();
		unknown2 = str.readFloat();
		unknown3 = str.readInt();
	}
	
	@Override
	public void write(EndianDataOutputStream str) throws Exception {
		throw new RuntimeException("Method not implemented");
	}
}
