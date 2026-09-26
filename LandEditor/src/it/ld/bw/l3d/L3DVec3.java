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

public class L3DVec3 extends Struct {
	public static final int SIZE = 12;
	public float x;
	public float y;
	public float z;

	public L3DVec3() {}
	public L3DVec3(float x, float y, float z) { this.x = x; this.y = y; this.z = z; }

	@Override
	public void read(EndianDataInputStream str) throws Exception {
		str.order(ByteOrder.LITTLE_ENDIAN);
		x = str.readFloat();
		y = str.readFloat();
		z = str.readFloat();
	}

	@Override
	public void write(EndianDataOutputStream str) throws Exception {
		str.order(ByteOrder.LITTLE_ENDIAN);
		str.writeFloat(x);
		str.writeFloat(y);
		str.writeFloat(z);
	}
	
	@Override
	public String toString() {
		return "("+x+", "+y+", "+z+")";
	}
}
