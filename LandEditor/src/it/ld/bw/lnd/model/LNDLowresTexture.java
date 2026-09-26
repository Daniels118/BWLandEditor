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
package it.ld.bw.lnd.model;

import java.nio.ByteOrder;

import it.ld.dds.DDSHeader;
import it.ld.dds.DDSTexture;
import it.ld.utils.EndianDataInputStream;
import it.ld.utils.EndianDataOutputStream;
import it.ld.utils.Struct;

public class LNDLowresTexture extends Struct {
	public static final int HEADER_SIZE = 20;
	
	public final static int WIDTH = 256;
	public final static int HEIGHT = 256;
	public final static int HTEXTURES = 4;
	public final static int VTEXTURES = 4;
	public final static int COUNT = HTEXTURES * VTEXTURES;
	public final static int SUBW = WIDTH / HTEXTURES;
	public final static int SUBH = HEIGHT / VTEXTURES;
	
	private final static int MAX_TEXTURE_SIZE = 8 * 1024 * 1024;
	
	private int textureId;	//Runtime
	private int material;	//Runtime
	private int count;
	private int index;
	//private int size;	We will retrieve the size from the texture when required
	private final DDSTexture ddsTexture = new DDSTexture();
	
	@Override
	public LNDLowresTexture clone() {
		LNDLowresTexture res = new LNDLowresTexture();
		res.set(this);
		return res;
	}
	
	public void set(LNDLowresTexture ref) {
		this.count = ref.count;
		this.index = ref.index;
		this.ddsTexture.setPixels(WIDTH, HEIGHT, ref.ddsTexture.getPixels());
	}
	
	@Override
	public void read(EndianDataInputStream str) throws Exception {
		try {
			str.order(ByteOrder.LITTLE_ENDIAN);
			textureId = str.readInt();
			material = str.readInt();
			count = str.readInt();
			index = str.readInt();
			int size = str.readInt();
			//System.out.println("Texture size: " + size);
			if (size < DDSHeader.SIZE || size > MAX_TEXTURE_SIZE) throw new Exception("Invalid texture size: " + size);
			ddsTexture.read(str);
			if (ddsTexture.getSize() != size - 4) {
				throw new RuntimeException("Wrong texture size");
			}
		} finally {
			
		}
	}
	
	@Override
	public void write(EndianDataOutputStream str) throws Exception {
		try {
			str.order(ByteOrder.LITTLE_ENDIAN);
			str.writeInt(textureId);
			str.writeInt(material);
			str.writeInt(count);
			str.writeInt(index);
			str.writeInt(getSize());
			ddsTexture.write(str);
		} finally {
			
		}
	}
	
	public int getTextureId() {
		return textureId;
	}

	public void setTextureId(int textureId) {
		this.textureId = textureId;
	}

	public int getMaterial() {
		return material;
	}

	public void setMaterial(int material) {
		this.material = material;
	}

	public int getCount() {
		return count;
	}

	public void setCount(int count) {
		this.count = count;
	}

	public int getIndex() {
		return index;
	}

	public void setIndex(int index) {
		this.index = index;
	}

	public int getSize() {
		return ddsTexture.getSize() + 4;
	}
	
	public DDSTexture getDdsTexture() {
		return ddsTexture;
	}
}
