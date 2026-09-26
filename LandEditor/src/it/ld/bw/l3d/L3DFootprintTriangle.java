package it.ld.bw.l3d;

import it.ld.utils.EndianDataInputStream;
import it.ld.utils.EndianDataOutputStream;
import it.ld.utils.Struct;

public class L3DFootprintTriangle extends Struct {
	public L3DVec2[] world = new L3DVec2[3];
	public L3DVec2[] uv = new L3DVec2[3];
	
	@Override
	public void read(EndianDataInputStream str) throws Exception {
		for (int i = 0; i < 3; i++) {
			L3DVec2 item = new L3DVec2();
			item.read(str);
			world[i] = item;
		}
		for (int i = 0; i < 3; i++) {
			L3DVec2 item = new L3DVec2();
			item.read(str);
			uv[i] = item;
		}
	}
	
	@Override
	public void write(EndianDataOutputStream str) throws Exception {
		throw new RuntimeException("Method not implemented");
	}
}
