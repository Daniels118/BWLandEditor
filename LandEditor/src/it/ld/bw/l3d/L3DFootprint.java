package it.ld.bw.l3d;

import it.ld.utils.EndianDataInputStream;
import it.ld.utils.EndianDataOutputStream;
import it.ld.utils.Struct;

public class L3DFootprint extends Struct {
	public L3DFootprintHeader header = new L3DFootprintHeader();
	public L3DFootprintEntry[] entries;
	
	@Override
	public void read(EndianDataInputStream str) throws Exception {
		header.read(str);
		entries = new L3DFootprintEntry[header.count];
		for (int i = 0; i < header.count; i++) {
			L3DFootprintEntry item = new L3DFootprintEntry();
			item.read(str);
			entries[i] = item;
		}
	}

	@Override
	public void write(EndianDataOutputStream str) throws Exception {
		throw new RuntimeException("Method not implemented");
	}
}
