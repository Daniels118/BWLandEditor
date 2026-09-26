package it.ld.bw.l3d;

import it.ld.utils.EndianDataInputStream;
import it.ld.utils.EndianDataOutputStream;
import it.ld.utils.Struct;

public class L3DFootprintEntry extends Struct {
	public int width;
	public int height;
	public int unknown;
	public int unknown1;
	public int unknown2;
	public int triangleCount;
	public L3DFootprintTriangle[] triangles;
	public short[] texels;
	
	@Override
	public void read(EndianDataInputStream str) throws Exception {
		little(str);
		width = str.readInt();
		height = str.readInt();
		unknown = str.readInt();
		unknown1 = str.readInt();
		unknown2 = str.readInt();
		triangleCount = str.readInt();
		triangles = new L3DFootprintTriangle[triangleCount];
		for (int i = 0; i < triangleCount; i++) {
			L3DFootprintTriangle item = new L3DFootprintTriangle();
			item.read(str);
			triangles[i] = item;
		}
		texels = new short[width * height];
		for (int i = 0; i < texels.length; i++) {
			texels[i] = str.readShort();
		}
	}
	
	@Override
	public void write(EndianDataOutputStream str) throws Exception {
		throw new RuntimeException("Method not implemented");
	}
	
	/**
	 * @return an array of pixels in packed ABGR format ({@code a<<24 | b<<16 | g<<8 | r}).
	 */
	public int[] getPixels() {
		int[] res = new int[texels.length];
		for (int i = 0; i < texels.length; i++) {
			int c = texels[i] & 0xFFFF;
			int a = (c & 0xF000) >> 8;
			int r = (c & 0x0F00) >> 4;
			int g = (c & 0x00F0);
			int b = (c & 0x000F) << 4;
			res[i] = a<<24 | b<<16 | g<<8 | r;
		}
		return res;
	}
}
