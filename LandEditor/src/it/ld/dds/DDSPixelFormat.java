/* Copyright (c) 2024-2026 Daniele Lombardi / Daniels118
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package it.ld.dds;

import java.nio.ByteOrder;
import java.util.HashSet;
import java.util.Set;

import it.ld.utils.Compat;
import it.ld.utils.EndianDataInputStream;
import it.ld.utils.EndianDataOutputStream;
import it.ld.utils.Struct;

public class DDSPixelFormat extends Struct {
	public static final int SIZE = 32;
	
	public static int DDPF_ALPHAPIXELS = 0x1;
	public static int DDPF_ALPHA = 0x2;
	public static int DDPF_FOURCC = 0x4;
	public static int DDPF_RGB = 0x40;
	public static int DDPF_YUV = 0x200;
	public static int DDPF_LUMINANCE = 0x20000;
	
	private static final Set<String> CompressedFormats = new HashSet<>();
	private static final Set<String> SupportedCompressions = new HashSet<>();
	
	public int size = SIZE;
	public int flags = DDPF_FOURCC;
	public String fourCC = "DXT3";
	public int rgbBitCount;
	public int rBitMask;
	public int gBitMask;
	public int bBitMask;
	public int aBitMask;
	
	static {
		CompressedFormats.add("DXT1");
		CompressedFormats.add("DXT2");
		CompressedFormats.add("DXT3");
		CompressedFormats.add("DXT4");
		CompressedFormats.add("DXT5");
		//
		SupportedCompressions.add("DXT1");
		SupportedCompressions.add("DXT3");
	}
	
	@Override
	public void read(EndianDataInputStream str) throws Exception {
		try {
			str.order(ByteOrder.LITTLE_ENDIAN);
			size = str.readInt();
			if (size != SIZE) throw new RuntimeException("Invalid DDSPixelFormat size");
			flags = str.readInt();
			if (flags != DDPF_FOURCC) {
				throw new RuntimeException("Unsupported flags: " + flags);
			}
			fourCC = new String(Compat.readNBytes(str, 4), ASCII);
			if (!SupportedCompressions.contains(fourCC)) {
				throw new RuntimeException("Unsupported compression type: " + fourCC);
			}
			rgbBitCount = str.readInt();
			rBitMask = str.readInt();
			gBitMask = str.readInt();
			bBitMask = str.readInt();
			aBitMask = str.readInt();
		} finally {
			
		}
	}
	
	@Override
	public void write(EndianDataOutputStream str) throws Exception {
		try {
			str.order(ByteOrder.LITTLE_ENDIAN);
			str.writeInt(size);
			str.writeInt(flags);
			str.write(fourCC.getBytes(ASCII));
			str.writeInt(rgbBitCount);
			str.writeInt(rBitMask);
			str.writeInt(gBitMask);
			str.writeInt(bBitMask);
			str.writeInt(aBitMask);
		} finally {
			
		}
	}
	
	public boolean isCompressed() {
		return CompressedFormats.contains(fourCC);
	}
	
	@Override
	public String toString() {
		return fourCC;
	}
}
