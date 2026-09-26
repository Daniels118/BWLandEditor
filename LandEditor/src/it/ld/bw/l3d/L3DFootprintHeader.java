package it.ld.bw.l3d;

import it.ld.utils.EndianDataInputStream;
import it.ld.utils.EndianDataOutputStream;
import it.ld.utils.Struct;

public class L3DFootprintHeader extends Struct {
	public static final int SIZE = 12;
	
	public int count;
	public int offset;
	public int size;
	
	@Override
	public void read(EndianDataInputStream str) throws Exception {
		little(str);
		count = str.readInt();
		offset = str.readInt();
		size = str.readInt();
	}
	
	@Override
	public void write(EndianDataOutputStream str) throws Exception {
		throw new RuntimeException("Method not implemented");
	}
}
