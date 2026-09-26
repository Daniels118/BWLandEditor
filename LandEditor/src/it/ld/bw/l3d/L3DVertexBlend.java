package it.ld.bw.l3d;

import java.nio.ByteOrder;

import it.ld.utils.EndianDataInputStream;
import it.ld.utils.EndianDataOutputStream;
import it.ld.utils.Struct;

public class L3DVertexBlend extends Struct {
	public static final int SIZE = 8;
	public int index1;
	public int index2;
	public float weight;

	@Override
	public void read(EndianDataInputStream str) throws Exception {
		str.order(ByteOrder.LITTLE_ENDIAN);
		index1 = str.readUnsignedShort();
		index2 = str.readUnsignedShort();
		weight = str.readFloat();
	}

	@Override
	public void write(EndianDataOutputStream str) throws Exception {
		str.order(ByteOrder.LITTLE_ENDIAN);
		str.writeShort(index1);
		str.writeShort(index2);
		str.writeFloat(weight);
	}
}
