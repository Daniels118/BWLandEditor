/* Copyright (c) 2026 Daniele Lombardi / Daniels118
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */
package it.ld.bw.g3d;

import java.nio.ByteOrder;

import it.ld.utils.EndianDataInputStream;
import it.ld.utils.EndianDataOutputStream;
import it.ld.utils.Struct;

public class BodyBlockLookup extends Struct {
	private int offset;
	private int unknown;

	public BodyBlockLookup() {}

	public BodyBlockLookup(int offset, int unknown) {
		this.offset = offset;
		this.unknown = unknown;
	}

	@Override
	public void read(EndianDataInputStream str) throws Exception {
		str.order(ByteOrder.LITTLE_ENDIAN);
		offset = str.readInt();
		unknown = str.readInt();
	}

	@Override
	public void write(EndianDataOutputStream str) throws Exception {
		str.order(ByteOrder.LITTLE_ENDIAN);
		str.writeInt(offset);
		str.writeInt(unknown);
	}

	public int getOffset() {
		return offset;
	}

	public int getUnknown() {
		return unknown;
	}
}
