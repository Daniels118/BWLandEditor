/* Copyright (c) 2026 Daniele Lombardi / Daniels118
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
package it.ld.bw.l3d;

import java.nio.ByteOrder;

import it.ld.utils.EndianDataInputStream;
import it.ld.utils.EndianDataOutputStream;
import it.ld.utils.Struct;

public class L3DTriangle extends Struct {
	public static final int SIZE = 6;
	
	public short index1;
	public short index2;
	public short index3;

	public L3DTriangle() {}
	
	public L3DTriangle(short index1, short index2, short index3) {
		this.index1 = index1;
		this.index2 = index2;
		this.index3 = index3;
	}

	@Override
	public void read(EndianDataInputStream str) throws Exception {
		str.order(ByteOrder.LITTLE_ENDIAN);
		index1 = str.readShort();
		index2 = str.readShort();
		index3 = str.readShort();
	}

	@Override
	public void write(EndianDataOutputStream str) throws Exception {
		str.order(ByteOrder.LITTLE_ENDIAN);
		str.writeShort(index1);
		str.writeShort(index2);
		str.writeShort(index3);
	}
	
	@Override
	public String toString() {
		return "Triangle("+index1+", "+index2+", "+index3+")";
	}
}
