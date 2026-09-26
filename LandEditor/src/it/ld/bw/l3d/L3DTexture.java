package it.ld.bw.l3d;

import it.ld.utils.EndianDataInputStream;
import it.ld.utils.EndianDataOutputStream;
import it.ld.utils.Struct;

public class L3DTexture extends Struct {
	public static final int WIDTH = 256;
	public static final int HEIGHT = 256;
	public static final int SIZE = 4 + 2 * WIDTH * HEIGHT;
	
	public int id;
	/**Array of 256 * 256 pixels in RGBA4444 format*/
	public final short[] texels = new short[256 * 256];
	
	@Override
	public void read(EndianDataInputStream str) throws Exception {
		little(str);
		id = str.readInt();
		for (int i = 0; i < texels.length; i++) {
			texels[i] = str.readShort();
		}
	}
	
	@Override
	public void write(EndianDataOutputStream str) throws Exception {
		throw new RuntimeException("Method not implemented");
	}
	
	/**
	 * @return an array of {@code WIDTH*HEIGHT} pixels in packed ABGR format ({@code a<<24 | b<<16 | g<<8 | r}).
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
