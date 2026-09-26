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

import it.ld.utils.EndianDataInputStream;
import it.ld.utils.EndianDataOutputStream;
import it.ld.utils.Listeners;
import it.ld.utils.Struct;
import it.ld.utils.UChangeListener.EventType;

public class LNDCountry extends Struct {
	/**This is the value that appears in all official maps. The binary representation seems to have some meaning,
	 * but what it means stays unclear.
	 * 1111111111111111111111111111111111001100110011001100110011001100
	 */
	public static final int DEFAULT_TERRAIN_TYPE = -858993460;
	
	public enum Property {TERRAIN_TYPE, MAP_MATERIALS, INDEX, NAME}
	public final Listeners listeners = new Listeners(this);
	
	public static final int STRUCT_SIZE = 3076;
	
	/**Always the same in all official maps.
	 */
	private int terrainType = DEFAULT_TERRAIN_TYPE;
	private LNDMapMaterial[] mapMaterials = new LNDMapMaterial[256]; //altitude 0-255
	
	private LndFile land;
	private int index = -1;
	private String name = "";
	
	public LNDCountry() {
		for (int i = 0; i < mapMaterials.length; i++) {
			mapMaterials[i] = new LNDMapMaterial();
		}
	}
	
	@Override
	public LNDCountry clone() {
		LNDCountry res = new LNDCountry();
		res.set(this);
		return res;
	}
	
	public void set(LNDCountry ref) {
		this.setTerrainType(ref.terrainType);
		this.setMapMaterials(ref.mapMaterials);
	}
	
	@Override
	public void read(EndianDataInputStream str) throws Exception {
		try {
			str.order(ByteOrder.LITTLE_ENDIAN);
			terrainType = str.readInt();
			for (int i = 0; i < mapMaterials.length; i++) {
				mapMaterials[i].read(str);
			}
			extractName();
		} finally {
			
		}
	}
	
	@Override
	public void write(EndianDataOutputStream str) throws Exception {
		try {
			str.order(ByteOrder.LITTLE_ENDIAN);
			str.writeInt(terrainType);
			for (LNDMapMaterial material : mapMaterials) {
				material.write(str);
			}
		} finally {
			
		}
	}

	/**Get the terrain type.
	 * @return
	 */
	public int getTerrainType() {
		return terrainType;
	}

	/**Set the terrain type, which is always {@link #DEFAULT_TERRAIN_TYPE} in all official maps.
	 * @param terrainType
	 */
	public void setTerrainType(int terrainType) {
		if (terrainType !=  this.terrainType) {
			Object oldValue = this.terrainType;
			this.terrainType = terrainType;
			listeners.notify(EventType.CHANGE, Property.TERRAIN_TYPE, oldValue, this.terrainType);
		}
	}

	public LNDMapMaterial[] getMapMaterialsForRead() {
		return mapMaterials;
	}
	
	public BulkUpdate<LNDMapMaterial> getMapMaterialsForUpdate() {
		return new BulkUpdate<LNDMapMaterial>(mapMaterials, (changes) -> {
			if (changes != null) {
				//For mapMaterials we generate a single event, since having one event per elevation would generate too much overhead
				listeners.notify(EventType.CHANGE, Property.MAP_MATERIALS, null, this.mapMaterials);
			}
		});
	}

	public void setMapMaterials(LNDMapMaterial[] mapMaterials) {
		if (mapMaterials.length != 256) throw new IllegalArgumentException("Materials must have 256 values");
		Object oldValue = this.mapMaterials;
		LNDMapMaterial[] newMaterials = new LNDMapMaterial[256];
		for (int i = 0; i < 256; i++) {
			newMaterials[i] = mapMaterials[i].clone();
		}
		this.mapMaterials = newMaterials;
		this.setName(name);
		listeners.notify(EventType.CHANGE, Property.MAP_MATERIALS, oldValue, this.mapMaterials);
	}
	
	public boolean usesMaterial(int index) {
		for (LNDMapMaterial mat : mapMaterials) {
			if (mat.usesMaterial(index)) return true;
		}
		return false;
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
			int oldVal = this.index;
			this.index = index;
			listeners.notify(EventType.CHANGE, Property.INDEX, oldVal, this.index);
		}
	}
	
	public String getName() {
		return this.name;
	}
	
	private void extractName() {
		StringBuilder buffer = new StringBuilder(256);
		for (int i = 0; i < 256; i++) {
			LNDMapMaterial mat = mapMaterials[i];
			if (mat.getFirstMaterialIndex() == mat.getSecondMaterialIndex()) {
				if (mat.getCoefficient() <= 0 || mat.getCoefficient() >= 255) break;
				buffer.appendCodePoint(mat.getCoefficient());
			}
		}
		this.name = buffer.toString();
	}
	
	public void setName(String name) {
		String oldValue = this.name;
		char[] chars = name.toCharArray();
		for (int i = 0; i < chars.length; i++) {
			if (chars[i] <= 0 || chars[i] >= 255) {
				throw new IllegalArgumentException("Only ASCII strings are supported");
			}
		}
		int src = 0;
		int dst = 0;
		for (; src < chars.length && dst < 256; dst++) {
			LNDMapMaterial mat = mapMaterials[dst];
			if (mat.getFirstMaterialIndex() == mat.getSecondMaterialIndex()) {
				mat.setCoefficient(chars[src++]);
			}
		}
		for (; dst < 256; dst++) {
			LNDMapMaterial mat = mapMaterials[dst];
			if (mat.getFirstMaterialIndex() == mat.getSecondMaterialIndex()) {
				mat.setCoefficient(0);	//Null terminator
				break;
			}
		}
		this.name = name.substring(0, src);
		if (!this.name.equals(oldValue)) {
			listeners.notify(EventType.CHANGE, Property.NAME, oldValue, this.name);
		}
	}
	
	@Override
	public String toString() {
		if (!name.isEmpty()) return index + " - " + name;
		if (index >= 0) return String.valueOf(index);
		return super.toString();
	}
}
