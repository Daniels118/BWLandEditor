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
import it.ld.utils.MathUtils;
import it.ld.utils.Struct;

public class LNDMapMaterial extends Struct {
	private int firstMaterialIndex;
	private int secondMaterialIndex;
	private int coefficient;
	
	public LNDMapMaterial() {}
	
	public LNDMapMaterial(int firstMaterialIndex, int secondMaterialIndex, int coefficient) {
		this.firstMaterialIndex = firstMaterialIndex;
		this.secondMaterialIndex = secondMaterialIndex;
		this.coefficient = coefficient;
	}
	
	@Override
	public LNDMapMaterial clone() {
		return new LNDMapMaterial(firstMaterialIndex, secondMaterialIndex, coefficient);
	}
	
	@Override
	public void read(EndianDataInputStream str) throws Exception {
		try {
			str.order(ByteOrder.LITTLE_ENDIAN);
			secondMaterialIndex = str.readInt();
			firstMaterialIndex = str.readInt();
			coefficient = str.readInt();
		} finally {
			
		}
	}
	
	@Override
	public void write(EndianDataOutputStream str) throws Exception {
		try {
			str.order(ByteOrder.LITTLE_ENDIAN);
			str.writeInt(secondMaterialIndex);
			str.writeInt(firstMaterialIndex);
			str.writeInt(coefficient);
		} finally {
			
		}
	}
	
	public boolean usesMaterial(int index) {
		return index == firstMaterialIndex || index == secondMaterialIndex;
	}

	public int getFirstMaterialIndex() {
		return firstMaterialIndex;
	}

	public void setFirstMaterialIndex(int firstMaterialIndex) {
		this.firstMaterialIndex = firstMaterialIndex;
	}

	public int getSecondMaterialIndex() {
		return secondMaterialIndex;
	}

	public void setSecondMaterialIndex(int secondMaterialIndex) {
		this.secondMaterialIndex = secondMaterialIndex;
	}
	
	public float getBlend() {
		return (float)coefficient / 255f;
	}
	
	public void setBlend(float weight) {
		this.coefficient = MathUtils.clamp(Math.round(weight * 255f), 0, 255);
	}
	
	public int getCoefficient() {
		return coefficient;
	}

	public void setCoefficient(int coefficient) {
		this.coefficient = coefficient;
	}
}
