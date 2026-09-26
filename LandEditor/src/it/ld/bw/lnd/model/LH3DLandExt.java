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

public class LH3DLandExt extends Struct {
	public static final String MAGIC = "EXT0";
	
	//private int size;
	private byte version = 1;
	private byte altitudeBits = 8;
	
	public void set(LH3DLandExt ref) {
		this.version = ref.version;
		this.altitudeBits = ref.altitudeBits;
	}
	
	public static boolean checkMagic(String magic) {
		return MAGIC.equals(magic);
	}
	
	@Override
	public void read(EndianDataInputStream str) throws Exception {
		try {
			str.order(ByteOrder.LITTLE_ENDIAN);
			
			int size = str.readInt();
			int bytesToRead = size;
			if ((bytesToRead -= 8) <= 0) return;
			
			version = str.readByte();
			if ((bytesToRead -= 1) <= 0) return;
			
			altitudeBits = str.readByte();
			if ((bytesToRead -= 1) <= 0) return;
			
			if (bytesToRead > 0) {
				byte[] unknown = new byte[bytesToRead];
				int n = str.read(unknown);
				if (n < unknown.length) throw new Exception("Not enough data: expected "+bytesToRead+", received "+n);
			}
		} finally {
			
		}
	}
	
	@Override
	public void write(EndianDataOutputStream str) throws Exception {
		try {
			str.order(ByteOrder.LITTLE_ENDIAN);
			Struct.writeFixedString(str, MAGIC, 4);
			str.writeInt(getSize());
			str.writeByte(version);
			str.writeByte(altitudeBits);
		} finally {
			
		}
	}
	
	public int getSize() {
		return 4 + 4 + 1 + 1;
	}

	public int getVersion() {
		return version & 0xFF;
	}

	public void setVersion(int version) {
		this.version = (byte)version;
	}

	public int getAltitudeBits() {
		return altitudeBits & 0xFF;
	}

	void setAltitudeBits(int altitudeBits) {
		if (altitudeBits < 8 || altitudeBits > 16) throw new IllegalArgumentException("Altitude bits must be in the range [8, 16]");
		this.altitudeBits = (byte)altitudeBits;
	}
}
