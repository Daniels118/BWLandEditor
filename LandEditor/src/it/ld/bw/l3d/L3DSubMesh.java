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

/**
 * Header di primitiva L3D secondo L3DPrimitiveHeader di OpenBlack.
 * Il nome classe resta L3DSubMesh per compatibilità con il codice esistente.
 */
public class L3DSubMesh extends Struct {
	public static final int SIZE = 48;

	public int materialType;
	public int alphaCutoutThreshold;
	public int cullMode;
	public int materialUnused;
	public int skinId;
	public int color;
	public int numVertices;
	public int verticesOffset;
	public int numTriangles;
	public int trianglesOffset;
	public int numGroups;
	public int groupsOffset;
	public int numVertexBlends;
	public int vertexBlendsOffset;

	/* Alias legacy. */
	public int unknown1;
	public int unknown2;
	public int unknown3;
	public int boneVertLutSize;
	public int boneVertLutOffset;

	public final ArrayList<L3DVertex> vertices = new ArrayList<L3DVertex>();
	public final ArrayList<L3DTriangle> triangles = new ArrayList<L3DTriangle>();
	public final ArrayList<L3DVertexGroup> vertexGroups = new ArrayList<L3DVertexGroup>();
	public byte[] boneVertLut = new byte[0];
	public final ArrayList<L3DVertexBlend> vertexBlends = new ArrayList<L3DVertexBlend>();

	@Override
	public void read(EndianDataInputStream str) throws Exception {
		str.order(ByteOrder.LITTLE_ENDIAN);
		materialType = str.readInt();
		int materialPacked = str.readInt();
		alphaCutoutThreshold = materialPacked & 0xFF;
		cullMode = (materialPacked >>> 8) & 0xFF;
		materialUnused = (materialPacked >>> 16) & 0xFFFF;
		skinId = str.readInt();
		color = str.readInt();
		numVertices = str.readInt();
		verticesOffset = str.readInt();
		numTriangles = str.readInt();
		trianglesOffset = str.readInt();
		numGroups = str.readInt();
		groupsOffset = str.readInt();
		numVertexBlends = str.readInt();
		vertexBlendsOffset = str.readInt();
		syncLegacyFields(materialPacked);
	}

	@Override
	public void write(EndianDataOutputStream str) throws Exception {
		str.order(ByteOrder.LITTLE_ENDIAN);
		str.writeInt(materialType);
		int materialPacked = (alphaCutoutThreshold & 0xFF) | ((cullMode & 0xFF) << 8) | ((materialUnused & 0xFFFF) << 16);
		str.writeInt(materialPacked);
		str.writeInt(skinId);
		str.writeInt(color);
		str.writeInt(numVertices);
		str.writeInt(verticesOffset);
		str.writeInt(numTriangles);
		str.writeInt(trianglesOffset);
		str.writeInt(numGroups);
		str.writeInt(groupsOffset);
		str.writeInt(numVertexBlends);
		str.writeInt(vertexBlendsOffset);
	}

	private void syncLegacyFields(int materialPacked) {
		unknown1 = materialType;
		unknown2 = materialPacked;
		unknown3 = color;
		boneVertLutSize = numGroups;
		boneVertLutOffset = groupsOffset;
	}
}
