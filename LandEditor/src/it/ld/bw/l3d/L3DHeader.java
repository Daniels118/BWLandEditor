/* Copyright (c) 2026 Daniele Lombardi / Daniels118
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */
package it.ld.bw.l3d;

import java.io.IOException;
import java.nio.ByteOrder;

import it.ld.utils.EndianDataInputStream;
import it.ld.utils.EndianDataOutputStream;
import it.ld.utils.Struct;

public class L3DHeader extends Struct {
	public static final int SIZE = 76;
	public static final String MAGIC = "L3D0";
	public static final int NO_OFFSET = -1;
	
	public String magicHeader = MAGIC;
	public int flags;
	public int size;
	public int submeshCount;
	public int submeshOffsetsOffset;
	public int boundingBoxUnknown;
	public final L3DVec3 boundingBoxCentre = new L3DVec3();
	public final L3DVec3 boundingBoxSize = new L3DVec3();
	public float boundingBoxDiagonalLength;
	public int anotherOffset;
	public int skinCount;
	public int skinOffsetsOffset;
	public int extraDataCount;
	public int extraDataOffset;
	public int footprintDataOffset;
	
	@Override
	public void read(EndianDataInputStream str) throws Exception {
		str.order(ByteOrder.LITTLE_ENDIAN);
		magicHeader = readFixedString(str, 4, true);
		if (!MAGIC.equals(magicHeader)) throw new IOException("Unrecognized L3D header: " + magicHeader);
		flags = str.readInt();
		size = str.readInt();
		submeshCount = str.readInt();
		submeshOffsetsOffset = str.readInt();
		boundingBoxUnknown = str.readInt();
		boundingBoxCentre.read(str);
		boundingBoxSize.read(str);
		boundingBoxDiagonalLength = str.readFloat();
		anotherOffset = str.readInt();
		skinCount = str.readInt();
		skinOffsetsOffset = str.readInt();
		extraDataCount = str.readInt();
		extraDataOffset = str.readInt();
		footprintDataOffset = str.readInt();
	}
	
	@Override
	public void write(EndianDataOutputStream str) throws Exception {
		str.order(ByteOrder.LITTLE_ENDIAN);
		writeFixedString(str, magicHeader, 4);
		str.writeInt(flags);
		str.writeInt(size);
		str.writeInt(submeshCount);
		str.writeInt(submeshOffsetsOffset);
		str.writeInt(boundingBoxUnknown);
		boundingBoxCentre.write(str);
		boundingBoxSize.write(str);
		str.writeFloat(boundingBoxDiagonalLength);
		str.writeInt(anotherOffset);
		str.writeInt(skinCount);
		str.writeInt(skinOffsetsOffset);
		str.writeInt(extraDataCount);
		str.writeInt(extraDataOffset);
		str.writeInt(footprintDataOffset);
	}
	
	public boolean hasBones() {
		return (flags & (0x100)) != 0;
	}
	
	public boolean hasDoorPosition() {
		return (flags & (0x800)) != 0;
	}
	
	public boolean isPacked() {
		return (flags & (0x1000)) != 0;
	}
	
	public boolean isNoDraw() {
		return (flags & (0x2000)) != 0;
	}
	
	public boolean containsLandscapeFeature() {
		return (flags & (0x8000)) != 0;
	}
	
	public boolean containsUV2() {
		return (flags & (0x40000)) != 0;
	}
	
	public boolean containsNameData() {
		return (flags & (0x80000)) != 0;
	}
	
	public boolean containsExtraMetrics() {
		return (flags & (0x100000)) != 0;
	}
	
	public boolean containsEBone() {
		return (flags & (0x200000)) != 0;
	}
	
	public boolean containsTnLData() {
		return (flags & (0x400000)) != 0;
	}
	
	public boolean containsNewEP() {
		return (flags & (0x800000)) != 0;
	}
}
