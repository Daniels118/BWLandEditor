/* Copyright (c) 2026 Daniele Lombardi / Daniels118
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */
package it.ld.bw.l3d;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Objects;
import java.util.zip.InflaterInputStream;

import it.ld.utils.EndianDataInputStream;
import it.ld.utils.EndianDataOutputStream;
import it.ld.utils.Struct;

public class L3DFile extends Struct {
	public final L3DHeader header = new L3DHeader();
	
	public final ArrayList<L3DSubmeshHeader> submeshHeaders = new ArrayList<>();
	public final ArrayList<L3DPrimitiveHeader> primitiveHeaders = new ArrayList<>();
	public final ArrayList<L3DVertex> vertices = new ArrayList<>();
	public final ArrayList<L3DTriangle> triangles = new ArrayList<>();
	public final ArrayList<L3DVertexGroup> vertexGroups = new ArrayList<>();
	public final ArrayList<L3DVertexBlend> blends = new ArrayList<>();
	public final ArrayList<L3DBone> bones = new ArrayList<>();
	public final ArrayList<L3DVec3> extraPoints = new ArrayList<>();
	public final ArrayList<Integer> skinOffsets = new ArrayList<>();
	public final ArrayList<L3DTexture> skinTextures = new ArrayList<>();
	public L3DFootprint footprint;
	
	private String name;
	private File file;
	private int index;

	public void read(File file) throws IOException {
		this.file = file;
		this.name = file.getName().replaceAll("\\..+?$", "");
		if (file.getName().toLowerCase().endsWith(".zzz")) {
			readZzz(file);
			return;
		}
		try (EndianDataInputStream str = new EndianDataInputStream(new FileInputStream(file))) {
			read(str);
		}
	}

	public void read(byte[] data) throws IOException {
		try (EndianDataInputStream str = stream(data)) {
			read(str);
		}
	}

	public void write(File file) throws IOException {
		try (EndianDataOutputStream str = new EndianDataOutputStream(new FileOutputStream(file))) {
			write(str);
		}
	}

	@Override
	public void read(EndianDataInputStream str) throws IOException {
		str.order(ByteOrder.LITTLE_ENDIAN);
		byte[] rawData = readAll(str);
		parse(rawData);
	}

	@Override
	public void write(EndianDataOutputStream str) throws IOException {
		throw new RuntimeException("Method not implemented");
	}

	private void parse(byte[] data) throws IOException {
		clear();
		readStructAt(data, 0, header, L3DHeader.SIZE);

		int submeshCount = unsignedCount(header.submeshCount, "submeshCount");
		int skinCount = unsignedCount(header.skinCount, "skinCount");
		int extraDataCount = unsignedCount(header.extraDataCount, "extraDataCount");

		ArrayList<Integer> submeshOffsets = readOffsetTable(data, header.submeshOffsetsOffset, submeshCount, "submesh");
		readSkinOffsets(data, skinCount);
		readExtraPoints(data, extraDataCount);

		int totalPrimitives = 0;
		int totalBones = 0;
		for (int offset : submeshOffsets) {
			L3DSubmeshHeader submesh = new L3DSubmeshHeader();
			readStructAt(data, offset, submesh, L3DSubmeshHeader.SIZE);
			submeshHeaders.add(submesh);
			totalPrimitives = checkedAdd(totalPrimitives, unsignedCount(submesh.numPrimitives, "numPrimitives"), "totalPrimitives");
			totalBones = checkedAdd(totalBones, unsignedCount(submesh.numBones, "numBones"), "totalBones");
		}
		
		for (int i = 0; i < skinCount; i++) {
			int offset = skinOffsets.get(i);
			L3DTexture skin = new L3DTexture();
			readStructAt(data, offset, skin, L3DTexture.SIZE);
			skinTextures.add(skin);
		}

		ArrayList<Integer> primitiveOffsets = new ArrayList<Integer>(totalPrimitives);
		for (L3DSubmeshHeader submesh : submeshHeaders) {
			ArrayList<Integer> offsets = readOffsetTable(data, submesh.primitivesOffset, submesh.numPrimitives, "primitive");
			submesh.primitiveOffsets.addAll(offsets);
			primitiveOffsets.addAll(offsets);
		}

		for (int offset : primitiveOffsets) {
			L3DPrimitiveHeader primitive = new L3DPrimitiveHeader();
			readStructAt(data, offset, primitive, L3DPrimitiveHeader.SIZE);
			primitiveHeaders.add(primitive);
		}

		int primitiveIndex = 0;
		for (L3DSubmeshHeader submesh : submeshHeaders) {
			for (int i = 0; i < submesh.numPrimitives; i++) {
				L3DPrimitiveHeader primitive = primitiveHeaders.get(primitiveIndex++);
				readPrimitiveData(data, primitive);
				submesh.primitives.add(primitive);
			}
			readBoneData(data, submesh);
		}
		
		readFootprintData(data);
	}

	private void clear() {
		submeshHeaders.clear();
		primitiveHeaders.clear();
		vertices.clear();
		triangles.clear();
		vertexGroups.clear();
		blends.clear();
		bones.clear();
		extraPoints.clear();
		skinOffsets.clear();
		skinTextures.clear();
	}
	
	private void readFootprintData(byte[] data) throws IOException {
		if (header.containsLandscapeFeature() && header.footprintDataOffset > 0) {
			footprint = new L3DFootprint();
			readStructAt(data, header.footprintDataOffset, footprint, data.length - header.footprintDataOffset);
		}
	}
	
	private void readPrimitiveData(byte[] data, L3DPrimitiveHeader primitive) throws IOException {
		primitive.vertices.clear();
		primitive.triangles.clear();
		primitive.vertexGroups.clear();
		primitive.vertexBlends.clear();

		if (primitive.numVertices > 0 && primitive.verticesOffset != L3DHeader.NO_OFFSET) {
			checkRange(data, primitive.verticesOffset, primitive.numVertices, L3DVertex.SIZE, "vertex");
			for (int i = 0; i < primitive.numVertices; i++) {
				L3DVertex vertex = new L3DVertex();
				readStructAt(data, primitive.verticesOffset + i * L3DVertex.SIZE, vertex, L3DVertex.SIZE);
				primitive.vertices.add(vertex);
				vertices.add(vertex);
			}
		}

		if (primitive.numTriangles > 0 && primitive.trianglesOffset != L3DHeader.NO_OFFSET) {
			checkRange(data, primitive.trianglesOffset, primitive.numTriangles, L3DTriangle.SIZE, "triangle");
			for (int i = 0; i < primitive.numTriangles; i++) {
				L3DTriangle triangle = new L3DTriangle();
				readStructAt(data, primitive.trianglesOffset + i * L3DTriangle.SIZE, triangle, L3DTriangle.SIZE);
				primitive.triangles.add(triangle);
				triangles.add(triangle);
			}
		}

		if (primitive.numGroups > 0 && primitive.groupsOffset != L3DHeader.NO_OFFSET) {
			checkRange(data, primitive.groupsOffset, primitive.numGroups, L3DVertexGroup.SIZE, "vertex group");
			for (int i = 0; i < primitive.numGroups; i++) {
				L3DVertexGroup group = new L3DVertexGroup();
				readStructAt(data, primitive.groupsOffset + i * L3DVertexGroup.SIZE, group, L3DVertexGroup.SIZE);
				primitive.vertexGroups.add(group);
				vertexGroups.add(group);
			}
		}

		if (primitive.numVertexBlends > 0 && primitive.vertexBlendsOffset != L3DHeader.NO_OFFSET) {
			checkRange(data, primitive.vertexBlendsOffset, primitive.numVertexBlends, L3DVertexBlend.SIZE, "vertex blend");
			for (int i = 0; i < primitive.numVertexBlends; i++) {
				L3DVertexBlend blend = new L3DVertexBlend();
				readStructAt(data, primitive.vertexBlendsOffset + i * L3DVertexBlend.SIZE, blend, L3DVertexBlend.SIZE);
				primitive.vertexBlends.add(blend);
				blends.add(blend);
			}
		}
	}

	private void readBoneData(byte[] data, L3DSubmeshHeader submesh) throws IOException {
		submesh.bones.clear();
		submesh.boneMatrices.clear();
		if (submesh.numBones <= 0 || submesh.bonesOffset == L3DHeader.NO_OFFSET) return;
		checkRange(data, submesh.bonesOffset, submesh.numBones, L3DBone.SIZE, "bone");
		for (int i = 0; i < submesh.numBones; i++) {
			L3DBone bone = new L3DBone();
			readStructAt(data, submesh.bonesOffset + i * L3DBone.SIZE, bone, L3DBone.SIZE);
			submesh.bones.add(bone);
			bones.add(bone);

			L3DMatrix4 matrix = new L3DMatrix4();
			matrix.val[0] = bone.orientation[0];
			matrix.val[1] = bone.orientation[1];
			matrix.val[2] = bone.orientation[2];
			matrix.val[3] = bone.position.x;
			matrix.val[4] = bone.orientation[3];
			matrix.val[5] = bone.orientation[4];
			matrix.val[6] = bone.orientation[5];
			matrix.val[7] = bone.position.y;
			matrix.val[8] = bone.orientation[6];
			matrix.val[9] = bone.orientation[7];
			matrix.val[10] = bone.orientation[8];
			matrix.val[11] = bone.position.z;
			matrix.val[15] = 1f;
			submesh.boneMatrices.add(matrix);
		}
	}

	private void readSkinOffsets(byte[] data, int skinCount) throws IOException {
		if (skinCount <= 0 || header.skinOffsetsOffset == L3DHeader.NO_OFFSET) return;
		skinOffsets.addAll(readOffsetTable(data, header.skinOffsetsOffset, skinCount, "skin"));
	}

	private void readExtraPoints(byte[] data, int extraDataCount) throws IOException {
		if (extraDataCount <= 0 || header.extraDataOffset == L3DHeader.NO_OFFSET) return;
		checkRange(data, header.extraDataOffset, extraDataCount, L3DVec3.SIZE, "extra point");
		for (int i = 0; i < extraDataCount; i++) {
			L3DVec3 point = new L3DVec3();
			readStructAt(data, header.extraDataOffset + i * L3DVec3.SIZE, point, L3DVec3.SIZE);
			extraPoints.add(point);
		}
	}

	private static ArrayList<Integer> readOffsetTable(byte[] data, int offset, int count, String label) throws IOException {
		ArrayList<Integer> offsets = new ArrayList<Integer>(Math.max(0, count));
		if (count <= 0) return offsets;
		if (offset == L3DHeader.NO_OFFSET) throw new IOException("Missing L3D " + label + " offset table.");
		checkRange(data, offset, count, 4, label + " offset");
		for (int i = 0; i < count; i++) offsets.add(readIntAt(data, offset + i * 4));
		return offsets;
	}

	private void readZzz(File file) throws IOException {
		byte[] packed;
		try (EndianDataInputStream str = new EndianDataInputStream(new FileInputStream(file))) {
			str.order(ByteOrder.LITTLE_ENDIAN);
			int decompressedSize = str.readInt();
			packed = readAll(str);
			byte[] decompressed = inflate(packed, decompressedSize);
			read(decompressed);
		}
	}
	
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public File getFile() {
		return file;
	}
	
	public void setFile(File file) {
		this.file = file;
	}
	
	public int getIndex() {
		return index;
	}
	
	public void setIndex(int index) {
		this.index = index;
	}
	
	public L3DVec3 getDoorPosition() {
		return header.hasDoorPosition() ? extraPoints.get(0) : null;
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(file, index, name);
	}
	
	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof L3DFile)) return false;
		L3DFile other = (L3DFile)obj;
		if (file != null) return this.file.equals(other.file) && this.index == other.index;
		if (name != null) return this.name.equals(name);
		return super.equals(obj);
	}
	
	@Override
	public String toString() {
		return name != null ? name : super.toString();
	}

	private static byte[] inflate(byte[] packed, int expectedSize) throws IOException {
		ByteArrayOutputStream out = expectedSize > 0 ? new ByteArrayOutputStream(expectedSize) : new ByteArrayOutputStream();
		try (InflaterInputStream inflater = new InflaterInputStream(new ByteArrayInputStream(packed))) {
			byte[] buf = new byte[8192];
			int n;
			while ((n = inflater.read(buf)) >= 0) out.write(buf, 0, n);
		}
		byte[] data = out.toByteArray();
		if (expectedSize > 0 && data.length != expectedSize) throw new IOException("Invalid decompressed L3D size: expected " + expectedSize + ", got " + data.length);
		return data;
	}

	private static EndianDataInputStream stream(byte[] data) {
		EndianDataInputStream str = new EndianDataInputStream(new ByteArrayInputStream(data));
		str.order(ByteOrder.LITTLE_ENDIAN);
		return str;
	}

	private static void readStructAt(byte[] data, int offset, Struct struct, int size) throws IOException {
		checkRange(data, offset, 1, size, struct.getClass().getSimpleName());
		try (EndianDataInputStream str = stream(slice(data, offset, size))) {
			try {
				struct.read(str);
			} catch (IOException e) {
				throw e;
			} catch (Exception e) {
				throw new IOException(e);
			}
		}
	}

	private static int readIntAt(byte[] data, int offset) throws IOException {
		checkRange(data, offset, 1, 4, "int");
		try (EndianDataInputStream str = stream(slice(data, offset, 4))) {
			return str.readInt();
		}
	}

	private static byte[] slice(byte[] data, int offset, int length) {
		byte[] out = new byte[length];
		System.arraycopy(data, offset, out, 0, length);
		return out;
	}

	private static int unsignedCount(int value, String label) throws IOException {
		if (value < 0) throw new IOException("Invalid L3D " + label + ": " + value);
		return value;
	}

	private static int checkedAdd(int a, int b, String label) throws IOException {
		long v = (long)a + (long)b;
		if (v > Integer.MAX_VALUE) throw new IOException("L3D " + label + " overflow.");
		return (int)v;
	}

	private static void checkRange(byte[] data, int offset, int count, int itemSize, String label) throws IOException {
		if (count < 0) throw new IOException("Invalid L3D " + label + " count: " + count);
		long length = (long)count * (long)itemSize;
		if (length > Integer.MAX_VALUE) throw new IOException("Invalid L3D " + label + " range length: " + length);
		checkRangeBytes(data, offset, (int)length, label);
	}

	private static void checkRangeBytes(byte[] data, int offset, int length, String label) throws IOException {
		if (offset < 0 || length < 0 || (long)offset + (long)length > data.length) {
			throw new IOException("Invalid L3D " + label + " range: offset=" + offset + ", length=" + length + ", fileSize=" + data.length);
		}
	}

	private static byte[] readAll(EndianDataInputStream str) throws IOException {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		byte[] buf = new byte[8192];
		int n;
		while ((n = str.read(buf)) >= 0) out.write(buf, 0, n);
		return out.toByteArray();
	}

	public static L3DFile load(File file) throws IOException {
		L3DFile res = new L3DFile();
		res.read(file);
		return res;
	}
}
