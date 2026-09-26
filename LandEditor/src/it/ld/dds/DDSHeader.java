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

import it.ld.utils.EndianDataInputStream;
import it.ld.utils.EndianDataOutputStream;
import it.ld.utils.Struct;

public class DDSHeader extends Struct {
	public static final int SIZE = 124;
	
	public static final int DDSD_CAPS = 0x1;
	public static final int DDSD_HEIGHT = 0x2;
	public static final int DDSD_WIDTH = 0x4;
	public static final int DDSD_PITCH = 0x8;
	public static final int DDSD_PIXELFORMAT = 0x1000;
	public static final int DDSD_MIPMAPCOUNT = 0x20000;
	public static final int DDSD_LINEARSIZE = 0x80000;
	public static final int DDSD_DEPTH = 0x800000;
	
	public static final int DDSCAPS_COMPLEX = 0x8;
	public static final int DDSCAPS_MIPMAP = 0x400000;
	public static final int DDSCAPS_TEXTURE = 0x1000;
	
	public static final int DDSCAPS2_CUBEMAP = 0x200;
	public static final int DDSCAPS2_CUBEMAP_POSITIVEX = 0x400;
	public static final int DDSCAPS2_CUBEMAP_NEGATIVEX = 0x800;
	public static final int DDSCAPS2_CUBEMAP_POSITIVEY = 0x1000;
	public static final int DDSCAPS2_CUBEMAP_NEGATIVEY = 0x2000;
	public static final int DDSCAPS2_CUBEMAP_POSITIVEZ = 0x4000;
	public static final int DDSCAPS2_CUBEMAP_NEGATIVEZ = 0x8000;
	public static final int DDSCAPS2_VOLUME = 0x2000000;
	
	public int				size = SIZE;
	public int				flags = DDSD_CAPS | DDSD_HEIGHT | DDSD_WIDTH | DDSD_PIXELFORMAT;
	public int				height;
	public int				width;
	public int				pitchOrLinearSize;
	public int				depth;
	public int				mipMapCount;
	public final int[]		reserved1 = new int[11];
	public final DDSPixelFormat	ddspf = new DDSPixelFormat();
	public int				caps = DDSCAPS_TEXTURE;
	public int				caps2;
	public int				caps3;
	public int				caps4;
	public int				reserved2;
	
	@Override
	public void read(EndianDataInputStream str) throws Exception {
		try {
			str.order(ByteOrder.LITTLE_ENDIAN);
			size = str.readInt();
			if (size != SIZE) throw new Exception("Invalid DDSHeader size (" + size + ")");
			flags = str.readInt();
			height = str.readInt();
			if (height % 4 != 0) throw new Exception("Height must be a multiple of 4");
			width = str.readInt();
			if (width % 4 != 0) throw new Exception("Width must be a multiple of 4");
			pitchOrLinearSize = str.readInt();
			depth = str.readInt();
			mipMapCount = str.readInt();
			//if (mipMapCount != 0) throw new Exception("MipMaps are not supported");
			if (mipMapCount < 0) throw new Exception("Invalid mipmap count");
			for (int i = 0; i < reserved1.length; i++) {
				reserved1[i] = str.readInt();
			}
			ddspf.read(str);
			caps = str.readInt();
			caps2 = str.readInt();
			caps3 = str.readInt();
			caps4 = str.readInt();
			reserved2 = str.readInt();
		} finally {
			
		}
	}
	
	@Override
	public void write(EndianDataOutputStream str) throws Exception {
		try {
			str.order(ByteOrder.LITTLE_ENDIAN);
			str.writeInt(size);
			str.writeInt(flags);
			str.writeInt(height);
			str.writeInt(width);
			str.writeInt(pitchOrLinearSize);
			str.writeInt(depth);
			str.writeInt(mipMapCount);
			for (int i = 0; i < reserved1.length; i++) {
				str.writeInt(reserved1[i]);
			}
			ddspf.write(str);
			str.writeInt(caps);
			str.writeInt(caps2);
			str.writeInt(caps3);
			str.writeInt(caps4);
			str.writeInt(reserved2);
		} finally {
			
		}
	}
	
	public boolean hasHeader10() {
		return (ddspf.flags & DDSPixelFormat.DDPF_FOURCC) != 0 && "DX10".equals(ddspf.fourCC);
	}
}
