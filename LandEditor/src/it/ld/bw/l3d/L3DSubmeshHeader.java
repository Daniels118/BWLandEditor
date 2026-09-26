/* Copyright (c) 2026 Daniele Lombardi / Daniels118
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */
package it.ld.bw.l3d;

import java.nio.ByteOrder;
import java.util.ArrayList;

import it.ld.utils.EndianDataInputStream;
import it.ld.utils.EndianDataOutputStream;
import it.ld.utils.Struct;

public class L3DSubmeshHeader extends Struct {
	public static final int SIZE = 20;
	
	public static final int LOD_MAX = 7;

	public int flags;
	public int numPrimitives;
	public int primitivesOffset;
	public int numBones;
	public int bonesOffset;

	public final ArrayList<Integer> primitiveOffsets = new ArrayList<Integer>();
	public final ArrayList<L3DPrimitiveHeader> primitives = new ArrayList<L3DPrimitiveHeader>();
	public final ArrayList<L3DBone> bones = new ArrayList<L3DBone>();
	public final ArrayList<L3DMatrix4> boneMatrices = new ArrayList<L3DMatrix4>();
	
	@Override
	public void read(EndianDataInputStream str) throws Exception {
		str.order(ByteOrder.LITTLE_ENDIAN);
		flags = str.readInt();
		numPrimitives = str.readInt();
		primitivesOffset = str.readInt();
		numBones = str.readInt();
		bonesOffset = str.readInt();
	}

	@Override
	public void write(EndianDataOutputStream str) throws Exception {
		str.order(ByteOrder.LITTLE_ENDIAN);
		str.writeInt(flags);
		str.writeInt(numPrimitives);
		str.writeInt(primitivesOffset);
		str.writeInt(numBones);
		str.writeInt(bonesOffset);
	}

	public boolean hasBones() {
		return (flags & 0x1) != 0;
	}

	public int getStatus() {
		return (flags >>> 4) & 0x3F;
	}

	public boolean isWindow() {
		return (flags & 0x1000) != 0;
	}
	
	public boolean isPhysics() {
		return (flags & 0x2000) != 0;
	}
	
	public int getLOD() {
		return (flags >>> 29) & 0x7;
	}
}
