package it.ld.bw.info;

import it.ld.utils.EndianDataInputStream;
import it.ld.utils.EndianDataOutputStream;
import it.ld.utils.Struct;

public class GSpotVisualInfo extends Struct {
	public String debugString;
	public ParticleType particleType;
	public int life;
	public int reactionType;
	public int singleZSort;
	public int targetOwnerObject;
	
	@Override
	public void read(EndianDataInputStream str) throws Exception {
		little(str);
		debugString = readFixedString(str, 0x30, true);
		particleType = ParticleType.values()[str.readInt()];
		life = str.readInt();
		reactionType = str.readInt();
		singleZSort = str.readInt();
		targetOwnerObject = str.readInt();
	}
	
	@Override
	public void write(EndianDataOutputStream str) throws Exception {
		throw new RuntimeException("Method not implemented");
	}
	
	@Override
	public String toString() {
		return debugString != null ? debugString : super.toString();
	}
}
