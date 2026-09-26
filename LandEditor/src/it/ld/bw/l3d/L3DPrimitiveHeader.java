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

/** Header di primitiva L3D secondo L3DPrimitiveHeader di OpenBlack. */
public class L3DPrimitiveHeader extends Struct {
	public static final int SIZE = 48;

	public L3DMaterial material = new L3DMaterial();
	public int numVertices;
	public int verticesOffset;
	public int numTriangles;
	public int trianglesOffset;
	public int numGroups;
	public int groupsOffset;
	public int numVertexBlends;
	public int vertexBlendsOffset;

	public final ArrayList<L3DVertex> vertices = new ArrayList<L3DVertex>();
	public final ArrayList<L3DTriangle> triangles = new ArrayList<L3DTriangle>();
	public final ArrayList<L3DVertexGroup> vertexGroups = new ArrayList<L3DVertexGroup>();
	public final ArrayList<L3DVertexBlend> vertexBlends = new ArrayList<L3DVertexBlend>();

	@Override
	public void read(EndianDataInputStream str) throws Exception {
		str.order(ByteOrder.LITTLE_ENDIAN);
		material.read(str);
		numVertices = str.readInt();
		verticesOffset = str.readInt();
		numTriangles = str.readInt();
		trianglesOffset = str.readInt();
		numGroups = str.readInt();
		groupsOffset = str.readInt();
		numVertexBlends = str.readInt();
		vertexBlendsOffset = str.readInt();
	}

	@Override
	public void write(EndianDataOutputStream str) throws Exception {
		str.order(ByteOrder.LITTLE_ENDIAN);
		material.write(str);
		str.writeInt(numVertices);
		str.writeInt(verticesOffset);
		str.writeInt(numTriangles);
		str.writeInt(trianglesOffset);
		str.writeInt(numGroups);
		str.writeInt(groupsOffset);
		str.writeInt(numVertexBlends);
		str.writeInt(vertexBlendsOffset);
	}
}
