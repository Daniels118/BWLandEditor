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

public class L3DVertex extends Struct {
	public static final int SIZE = L3DVec3.SIZE + L3DVec2.SIZE + L3DVec3.SIZE;

	public final L3DVec3 position = new L3DVec3();
	public final L3DVec2 texCoords = new L3DVec2();
	public final L3DVec3 normal = new L3DVec3();

	@Override
	public void read(EndianDataInputStream str) throws Exception {
		str.order(ByteOrder.LITTLE_ENDIAN);
		position.read(str);
		texCoords.read(str);
		normal.read(str);
	}

	@Override
	public void write(EndianDataOutputStream str) throws Exception {
		str.order(ByteOrder.LITTLE_ENDIAN);
		position.write(str);
		texCoords.write(str);
		normal.write(str);
	}
	
	@Override
	public String toString() {
		return "L3DVertex("+position+")";
	}
}
