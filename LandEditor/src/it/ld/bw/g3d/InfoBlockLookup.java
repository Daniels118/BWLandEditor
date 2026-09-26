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

public class InfoBlockLookup extends Struct {
	private int blockId;
	private int unknown;

	public InfoBlockLookup() {}

	public InfoBlockLookup(int blockId, int unknown) {
		this.blockId = blockId;
		this.unknown = unknown;
	}

	@Override
	public void read(EndianDataInputStream str) throws Exception {
		str.order(ByteOrder.LITTLE_ENDIAN);
		blockId = str.readInt();
		unknown = str.readInt();
	}

	@Override
	public void write(EndianDataOutputStream str) throws Exception {
		str.order(ByteOrder.LITTLE_ENDIAN);
		str.writeInt(blockId);
		str.writeInt(unknown);
	}

	public int getBlockId() {
		return blockId;
	}

	public int getUnknown() {
		return unknown;
	}
}
