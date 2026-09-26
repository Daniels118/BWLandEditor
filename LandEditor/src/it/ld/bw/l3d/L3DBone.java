package it.ld.bw.l3d;

import java.nio.ByteOrder;

import it.ld.utils.EndianDataInputStream;
import it.ld.utils.EndianDataOutputStream;
import it.ld.utils.Struct;

public class L3DBone extends Struct {
	public static final int SIZE = 60;

	public int parent;
	public int firstChild;
	public int rightSibling;
	public final float[] orientation = new float[9];
	public final L3DVec3 position = new L3DVec3();

	@Override
	public void read(EndianDataInputStream str) throws Exception {
		str.order(ByteOrder.LITTLE_ENDIAN);
		parent = str.readInt();
		firstChild = str.readInt();
		rightSibling = str.readInt();
		for (int i = 0; i < orientation.length; i++) orientation[i] = str.readFloat();
		position.read(str);
	}

	@Override
	public void write(EndianDataOutputStream str) throws Exception {
		str.order(ByteOrder.LITTLE_ENDIAN);
		str.writeInt(parent);
		str.writeInt(firstChild);
		str.writeInt(rightSibling);
		for (float v : orientation) str.writeFloat(v);
		position.write(str);
	}
}
