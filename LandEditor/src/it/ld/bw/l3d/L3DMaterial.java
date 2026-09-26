package it.ld.bw.l3d;

import it.ld.utils.EndianDataInputStream;
import it.ld.utils.EndianDataOutputStream;
import it.ld.utils.Struct;

public class L3DMaterial extends Struct {
	public MaterialType type;	//int
	public int alphaCutoutThreshold;	//byte
	public byte cullMode;
	//public short filler;
	public int skinID;
	public int color;
	
	@Override
	public void read(EndianDataInputStream str) throws Exception {
		little(str);
		type = MaterialType.values()[str.readInt()];
		alphaCutoutThreshold = str.readUnsignedByte();
		cullMode = str.readByte();
		str.readShort();	//filler
		skinID = str.readInt();
		color = str.readInt();
	}
	
	@Override
	public void write(EndianDataOutputStream str) throws Exception {
		throw new RuntimeException("Method not implemented");
	}
}
