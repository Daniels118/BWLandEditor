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
import java.util.Arrays;

import it.ld.utils.EndianDataInputStream;
import it.ld.utils.EndianDataOutputStream;
import it.ld.utils.Listeners;
import it.ld.utils.Struct;
import it.ld.utils.UChangeListener.EventType;

public class LNDMaterial extends Struct {
	public enum Property {MATERIAL_TYPE, TEXELS, INDEX}
	public final Listeners listeners = new Listeners(this);
	
	public static final int STRUCT_SIZE = 131074;
	
	public static final int width = 256;
	public static final int height = 256;
	
	private TerrainMaterialType materialType;	//short
	private final short[] texels = new short[width * height];	//256*256
	
	private LndFile land;
	private int index;
	private int hash;
	
	@Override
	public LNDMaterial clone() {
		LNDMaterial res = new LNDMaterial();
		res.set(this);
		return res;
	}
	
	public void set(LNDMaterial ref) {
		this.setMaterialType(ref.materialType);
		this.setTexels(ref.texels);
	}
	
	@Override
	public void read(EndianDataInputStream str) throws Exception {
		try {
			str.order(ByteOrder.LITTLE_ENDIAN);
			int shortValue = str.readShort() & 0xFFFF;
			TerrainMaterialType[] types = TerrainMaterialType.values();
			if (shortValue < types.length) {
				materialType = types[shortValue];
			} else {
				System.err.println("Invalid material type: " + shortValue);
				materialType = TerrainMaterialType.None;
			}
			for (int i = 0; i < texels.length; i++) {
				texels[i] = str.readShort();
			}
			hash = Arrays.hashCode(texels);
		} finally {
			
		}
	}
	
	@Override
	public void write(EndianDataOutputStream str) throws Exception {
		try {
			str.order(ByteOrder.LITTLE_ENDIAN);
			str.writeShort(materialType.ordinal());
			for (int i = 0; i < texels.length; i++) {
				str.writeShort(texels[i]);
			}
		} finally {
			
		}
	}

	public TerrainMaterialType getMaterialType() {
		return materialType;
	}

	public void setMaterialType(TerrainMaterialType materialType) {
		if (this.materialType != materialType) {
			Object oldValue = this.materialType;
			this.materialType = materialType;
			listeners.notify(EventType.CHANGE, Property.MATERIAL_TYPE, oldValue, this.materialType);
		}
	}

	public short[] getTexels() {
		return texels;
	}

	public void setTexels(short[] texels) {
		if (texels.length != this.texels.length) throw new IllegalArgumentException("Wrong data size");
		if (!Arrays.equals(texels, this.texels)) {
			Object oldValue = this.texels;
			System.arraycopy(texels, 0, this.texels, 0, texels.length);
			hash = Arrays.hashCode(texels);
			listeners.notify(EventType.CHANGE, Property.TEXELS, oldValue, this.texels);
		}
	}
	
	public byte[] getByteRGBA() {
		byte[] pixels = new byte[width * height * 4];
		for (int i = 0, j = 0; i < texels.length; i++) {
			int texel = texels[i] & 0xFFFF;
			pixels[j++] = (byte) (((texel & 0b0111110000000000) >> 7) | ((texel & 0b0111000000000000) >> 12));
			pixels[j++] = (byte) (((texel & 0b0000001111100000) >> 2) | ((texel & 0b0000001110000000) >>  7));
			pixels[j++] = (byte) (((texel & 0b0000000000011111) << 3) | ((texel & 0b0000000000011100) >>  2));
			pixels[j++] = (byte) ((texel & 0b1000000000000000) == 0 ? 0xFF : 0);		
		}
		return pixels;
	}
	
	public void setRGBA(byte[] pixels) {
		if (pixels.length != width * height * 4) throw new RuntimeException("Image data must be int["+width*height*4+"]");
		short[] newTexels = new short[width * height];
		for (int src = 0, dst = 0; src < pixels.length; dst++) {
			int r = ((pixels[src++] & 0b11111000) << 7);
			int g = ((pixels[src++] & 0b11111000) << 2);
			int b = ((pixels[src++] & 0b11111000) >> 3);
			int a = ( pixels[src++] & 0xFF) < 128 ? 0x8000 : 0x0;
			newTexels[dst] = (short)(a | b | g | r);
		}
		setTexels(newTexels);
	}
	
	public int[] getIntARGB() {
		int[] pixels = new int[width * height];
		for (int i = 0, j = 0; i < texels.length; i++, j++) {
			int texel = texels[i] & 0xFFFF;
			pixels[j] =
					((texel & 0b1000000000000000) == 0 ? 0xFF000000 : 0) |
					(((texel & 0b0111110000000000) << 9) | ((texel & 0b0111000000000000) << 4)) |
					(((texel & 0b0000001111100000) << 6) | ((texel & 0b0000001110000000) << 1)) |
					(((texel & 0b0000000000011111) << 3) | ((texel & 0b0000000000011100) >> 2));
		}
		return pixels;
	}
	
	public void setARGB(int[] pixels) {
		if (pixels.length != width * height) throw new RuntimeException("Image data must be int["+width*height+"]");
		short[] newTexels = new short[256 * 256];
		for (int src = 0, dst = 0; src < pixels.length; src++, dst++) {
			int rgba = pixels[src];
			int a = ((rgba >> 24) & 0xFF) < 128 ? 0x8000 : 0x0;
			int b = ((rgba & 0b111110000000000000000000) >> 9);
			int g = ((rgba & 0b000000001111100000000000) >> 6);
			int r = ((rgba & 0b000000000000000011111000) >> 3);
			newTexels[dst] = (short)(a | b | g | r);
		}
		setTexels(newTexels);
	}
	
	public LndFile getLand() {
		return land;
	}
	
	void setLand(LndFile land) {
		this.land = land;
	}
	
	public int getIndex() {
		return index;
	}
	
	void setIndex(int index) {
		if (index != this.index) {
			Object oldValue = this.index;
			this.index = index;
			listeners.notify(EventType.CHANGE, Property.INDEX, oldValue, this.index);
		}
	}
	
	@Override
	public int hashCode() {
		return hash;
	}
	
	public int getTexelsHash() {
		return hash;
	}
	
	public boolean texelsEquals(LNDMaterial other) {
		return Arrays.equals(this.texels, other.texels);
	}
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
        if (!(obj instanceof LNDMaterial)) return false;
        LNDMaterial other = (LNDMaterial) obj;
        if (this.materialType != other.materialType) return false;
        //if (this.index >= 0 && other.index >= 0 && this.index != other.index) return false;
        if (this.hash != other.hash) return false;
        return Arrays.equals(this.texels, other.texels);
	}
	
	@Override
	public String toString() {
		if (index >= 0 && materialType != null) return index + " - " + materialType;
		if (index >= 0 ) return String.valueOf(index);
		if (materialType != null) return materialType.toString();
		return super.toString();
	}
}
