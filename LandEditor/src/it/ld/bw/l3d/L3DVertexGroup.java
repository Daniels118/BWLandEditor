package it.ld.bw.l3d;

import java.nio.ByteOrder;

import it.ld.utils.EndianDataInputStream;
import it.ld.utils.EndianDataOutputStream;
import it.ld.utils.Struct;

public class L3DVertexGroup extends Struct {
	public static final int SIZE = 4;
	public int vertexCount;
	public int boneIndex;

	@Override
	public void read(EndianDataInputStream str) throws Exception {
		str.order(ByteOrder.LITTLE_ENDIAN);
		vertexCount = str.readUnsignedShort();
		boneIndex = str.readUnsignedShort();
	}

	@Override
	public void write(EndianDataOutputStream str) throws Exception {
		str.order(ByteOrder.LITTLE_ENDIAN);
		str.writeShort(vertexCount);
		str.writeShort(boneIndex);
	}
}
