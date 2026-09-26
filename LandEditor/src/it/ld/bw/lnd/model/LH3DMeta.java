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
package it.ld.bw.lnd.model;

import java.nio.ByteOrder;

import it.ld.utils.EndianDataInputStream;
import it.ld.utils.EndianDataOutputStream;
import it.ld.utils.Struct;

public class LH3DMeta extends Struct {
	public static final String MAGIC = "META";
	
	//private int size;
	private byte[] data = new byte[0];
	
	public void set(LH3DMeta ref) {
		this.data = new byte[ref.data.length];
		System.arraycopy(ref.data, 0, this.data, 0, this.data.length);
	}
	
	public static boolean checkMagic(String magic) {
		return MAGIC.equals(magic);
	}
	
	@Override
	public void read(EndianDataInputStream str) throws Exception {
		try {
			str.order(ByteOrder.LITTLE_ENDIAN);
			int size = str.readInt();
			if (size > 0) {
				data = new byte[size];
				int n = str.read(data);
				if (n < data.length) throw new Exception("Not enough data: expect "+size+", received "+n);
			}
		} finally {
			
		}
	}
	
	@Override
	public void write(EndianDataOutputStream str) throws Exception {
		try {
			str.order(ByteOrder.LITTLE_ENDIAN);
			Struct.writeFixedString(str, MAGIC, 4);
			str.writeInt(data.length);
			str.write(data);
		} finally {
			
		}
	}
	
	public byte[] getData() {
		return data;
	}
	
	public void setData(byte[] data) {
		this.data = data;
	}
}
