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
package it.ld.bw.g3d;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import it.ld.dds.DDSHeader;
import it.ld.dds.DDSPixelFormat;
import it.ld.utils.EndianDataInputStream;
import it.ld.utils.EndianDataOutputStream;
import it.ld.utils.Struct;

/** Reads and writes Lionhead pack files (.G3D/.sad/.snd style packs). */
public class PackFile extends Struct {
	private static final byte[] MAGIC = new byte[] {'L', 'i', 'O', 'n', 'H', 'e', 'A', 'd'};
	private static final byte[] BLOCK_MAGIC = new byte[] {'M', 'K', 'J', 'C'};
	private static final int BLOCK_NAME_SIZE = 0x20;
	private static final int ANIMATION_HEADER_SIZE = 0x54;

	private boolean loaded;
	private final LinkedHashMap<String, byte[]> blocks = new LinkedHashMap<String, byte[]>();
	private final ArrayList<InfoBlockLookup> infoBlockLookup = new ArrayList<InfoBlockLookup>();
	private final ArrayList<BodyBlockLookup> bodyBlockLookup = new ArrayList<BodyBlockLookup>();
	private final LinkedHashMap<String, G3DTexture> textures = new LinkedHashMap<String, G3DTexture>();
	private final ArrayList<byte[]> meshes = new ArrayList<byte[]>();
	private final ArrayList<byte[]> animations = new ArrayList<byte[]>();
	private final ArrayList<AudioBankSampleHeader> audioSampleHeaders = new ArrayList<AudioBankSampleHeader>();
	private final ArrayList<byte[]> audioSampleData = new ArrayList<byte[]>();

	public void read(File file) throws IOException {
		try (EndianDataInputStream str = new EndianDataInputStream(new FileInputStream(file))) {
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
		clear();
		readBlocks(str);

		if (hasBlock("INFO")) {
			resolveInfoBlock();
			extractTexturesFromBlock();
			resolveMeshBlock();
		}

		if (hasBlock("Body")) {
			resolveBodyBlock();
			extractAnimationsFromBlock();
		}

		if (hasBlock("LHAudioBankSampleTable")) {
			resolveAudioBankSampleTableBlock();
			extractSoundsFromBlock();
		}

		loaded = true;
	}

	@Override
	public void write(EndianDataOutputStream str) throws IOException {
		str.order(ByteOrder.LITTLE_ENDIAN);
		str.write(MAGIC);
		for (Map.Entry<String, byte[]> entry : blocks.entrySet()) {
			writeBlockHeader(str, entry.getKey(), entry.getValue().length);
			str.write(entry.getValue());
		}
	}

	private void readBlocks(EndianDataInputStream str) throws IOException {
		str.order(ByteOrder.LITTLE_ENDIAN);
		byte[] magic = new byte[MAGIC.length];
		str.readFully(magic);
		if (!Arrays.equals(magic, MAGIC)) throw new IOException("Unrecognized file header.");

		while (true) {
			int first = str.read();
			if (first < 0) break;
			String name = readBlockName(str, first);
			int blockSize = str.readInt();

			if (blocks.containsKey(name)) throw new IOException("Duplicate block name: " + name);
			if (blockSize < 0) throw new IOException("Invalid negative block size for block: " + name);
			byte[] data = new byte[blockSize];
			str.readFully(data);
			blocks.put(name, data);
		}
	}

	private void resolveMeshBlock() throws IOException {
		if (!hasBlock("MESHES")) throw new IOException("No MESHES block in pack.");
		byte[] data = getBlock("MESHES");
		try (EndianDataInputStream str = stream(data)) {
			byte[] magic = new byte[BLOCK_MAGIC.length];
			str.readFully(magic);
			if (!Arrays.equals(magic, BLOCK_MAGIC)) throw new IOException("Unrecognized Mesh Block header.");
			int meshCount = str.readInt();
			if (meshCount < 0) throw new IOException("Invalid negative mesh count.");
			int[] meshOffsets = new int[meshCount];
			for (int i = 0; i < meshCount; i++) {
				meshOffsets[i] = str.readInt();
				if (meshOffsets[i] < 0) throw new IOException("Invalid negative mesh offset.");
			}
			meshes.clear();
			for (int i = 0; i < meshCount; i++) {
				int start = meshOffsets[i];
				int end = i == meshCount - 1 ? data.length : meshOffsets[i + 1];
				if (end < start) throw new IOException("Mesh offsets are not ordered.");
				meshes.add(readRange(data, start, end - start));
			}
		}
	}

	private void resolveInfoBlock() throws IOException {
		if (!hasBlock("INFO")) throw new IOException("No INFO block in pack.");
		try (EndianDataInputStream str = stream(getBlock("INFO"))) {
			int totalTextures = str.readInt();
			if (totalTextures < 0) throw new IOException("Invalid negative texture count.");
			infoBlockLookup.clear();
			for (int i = 0; i < totalTextures; i++) {
				InfoBlockLookup item = new InfoBlockLookup();
				readStruct(item, str);
				infoBlockLookup.add(item);
			}
		}
	}

	private void resolveBodyBlock() throws IOException {
		if (!hasBlock("Body")) throw new IOException("No Body block in pack.");
		try (EndianDataInputStream str = stream(getBlock("Body"))) {
			byte[] magic = new byte[BLOCK_MAGIC.length];
			str.readFully(magic);
			if (!Arrays.equals(magic, BLOCK_MAGIC)) throw new IOException("Unrecognized block header.");
			int totalAnimations = str.readInt();
			if (totalAnimations < 0) throw new IOException("Invalid negative animation count.");
			bodyBlockLookup.clear();
			for (int i = 0; i < totalAnimations; i++) {
				BodyBlockLookup item = new BodyBlockLookup();
				readStruct(item, str);
				bodyBlockLookup.add(item);
			}
		}
	}

	private void resolveAudioBankSampleTableBlock() throws IOException {
		if (!hasBlock("LHAudioBankSampleTable")) throw new IOException("No LHAudioBankSampleTable block in pack.");
		try (EndianDataInputStream str = stream(getBlock("LHAudioBankSampleTable"))) {
			int sampleCount = str.readUnsignedShort();
			str.readUnsignedShort();
			if (sampleCount == 0) throw new IOException("No entries found.");
			audioSampleHeaders.clear();
			for (int i = 0; i < sampleCount; i++) {
				AudioBankSampleHeader header = new AudioBankSampleHeader();
				readStruct(header, str);
				audioSampleHeaders.add(header);
			}
		}
	}

	private void extractTexturesFromBlock() throws IOException {
		textures.clear();
		for (InfoBlockLookup item : infoBlockLookup) {
			String blockName = Integer.toHexString(item.getBlockId());
			if (!hasBlock(blockName)) throw new IOException("Required texture block missing: " + blockName);
			try (EndianDataInputStream str = stream(getBlock(blockName))) {
				G3DTexture texture = new G3DTexture();
				readStruct(texture, str);
				if (texture.getHeader().getId() != item.getBlockId()) throw new IOException("Texture block id is not the same as block id: " + blockName);
				DDSHeader ddsHeader = texture.getTexture().header;
				if (ddsHeader.size != DDSHeader.SIZE || ddsHeader.ddspf.size != DDSPixelFormat.SIZE) throw new IOException("Invalid DDS header sizes.");
				if (textures.containsKey(blockName)) throw new IOException("Duplicate texture extracted: " + blockName);
				textures.put(blockName, texture);
			}
		}
	}

	private void extractAnimationsFromBlock() throws IOException {
		byte[] body = getBlock("Body");
		animations.clear();
		for (int i = 0; i < bodyBlockLookup.size(); i++) {
			String blockName = "Julien" + i;
			if (!hasBlock(blockName)) throw new IOException("Required animation block missing: " + blockName);
			int offset = bodyBlockLookup.get(i).getOffset();
			byte[] animationHeader = readRange(body, offset, ANIMATION_HEADER_SIZE);
			byte[] animationData = getBlock(blockName);
			byte[] animation = new byte[animationHeader.length + animationData.length];
			System.arraycopy(animationHeader, 0, animation, 0, animationHeader.length);
			System.arraycopy(animationData, 0, animation, animationHeader.length, animationData.length);
			animations.add(animation);
		}
	}

	private void extractSoundsFromBlock() throws IOException {
		if (!hasBlock("LHAudioWaveData")) throw new IOException("No LHAudioWaveData block in pack.");
		byte[] data = getBlock("LHAudioWaveData");
		audioSampleData.clear();
		for (AudioBankSampleHeader sample : audioSampleHeaders) {
			audioSampleData.add(readRange(data, sample.getOffset(), sample.getSize()));
		}
	}

	public void createRawBlock(String name, byte[] data) throws IOException {
		if (hasBlock(name)) throw new IOException("Duplicate block name: " + name);
		blocks.put(name, data);
	}

	public void insertMesh(byte[] data) {
		meshes.add(data);
	}

	public void createMeshBlock() throws IOException {
		if (hasBlock("MESHES")) throw new IOException("Duplicate block name: MESHES");
		try (ByteArrayOutputStream baos = new ByteArrayOutputStream(); EndianDataOutputStream str = new EndianDataOutputStream(baos)) {
			str.order(ByteOrder.LITTLE_ENDIAN);
			str.write(BLOCK_MAGIC);
			str.writeInt(meshes.size());
			int offset = BLOCK_MAGIC.length + 4 + meshes.size() * 4;
			for (byte[] mesh : meshes) {
				str.writeInt(offset);
				offset += mesh.length;
			}
			for (byte[] mesh : meshes) str.write(mesh);
			str.flush();
			blocks.put("MESHES", baos.toByteArray());
		}
	}

	public void createInfoBlock() throws IOException {
		if (hasBlock("INFO")) throw new IOException("Duplicate block name: INFO");
		try (ByteArrayOutputStream baos = new ByteArrayOutputStream(); EndianDataOutputStream str = new EndianDataOutputStream(baos)) {
			str.order(ByteOrder.LITTLE_ENDIAN);
			str.writeInt(infoBlockLookup.size());
			for (InfoBlockLookup item : infoBlockLookup) writeStruct(item, str);
			str.flush();
			blocks.put("INFO", baos.toByteArray());
		}
	}

	public void createBodyBlock() throws IOException {
		if (hasBlock("Body")) throw new IOException("Duplicate block name: Body");
		blocks.put("Body", new byte[0]);
	}

	private void clear() {
		loaded = false;
		blocks.clear();
		infoBlockLookup.clear();
		bodyBlockLookup.clear();
		textures.clear();
		meshes.clear();
		animations.clear();
		audioSampleHeaders.clear();
		audioSampleData.clear();
	}

	private static void writeBlockHeader(EndianDataOutputStream str, String name, int size) throws IOException {
		byte[] rawName = new byte[BLOCK_NAME_SIZE];
		byte[] nameBytes = name.getBytes(ASCII);
		System.arraycopy(nameBytes, 0, rawName, 0, Math.min(rawName.length - 1, nameBytes.length));
		str.write(rawName);
		str.writeInt(size);
	}

	private static String readBlockName(EndianDataInputStream str, int first) throws IOException {
		byte[] rawName = new byte[BLOCK_NAME_SIZE];
		rawName[0] = (byte)first;
		str.readFully(rawName, 1, rawName.length - 1);
		return trimNulls(new String(rawName, ASCII));
	}

	private static byte[] readRange(byte[] data, int offset, int size) throws IOException {
		if (offset < 0 || size < 0) throw new IOException("Invalid negative offset or size.");
		try (EndianDataInputStream str = stream(data)) {
			long skipped = str.skip(offset);
			while (skipped < offset) {
				long n = str.skip(offset - skipped);
				if (n <= 0) throw new EOFException();
				skipped += n;
			}
			byte[] res = new byte[size];
			str.readFully(res);
			return res;
		}
	}

	private static void readStruct(Struct struct, EndianDataInputStream str) throws IOException {
		try {
			struct.read(str);
		} catch (IOException e) {
			throw e;
		} catch (Exception e) {
			throw new IOException(e);
		}
	}

	private static void writeStruct(Struct struct, EndianDataOutputStream str) throws IOException {
		try {
			struct.write(str);
		} catch (IOException e) {
			throw e;
		} catch (Exception e) {
			throw new IOException(e);
		}
	}

	private static EndianDataInputStream stream(byte[] data) {
		EndianDataInputStream str = new EndianDataInputStream(new ByteArrayInputStream(data));
		str.order(ByteOrder.LITTLE_ENDIAN);
		return str;
	}

	private static String trimNulls(String s) {
		int end = s.indexOf(' ');
		return end >= 0 ? s.substring(0, end) : s.trim();
	}

	public boolean isLoaded() {
		return loaded;
	}
	
	public Map<String, byte[]> getBlocks() {
		return Collections.unmodifiableMap(blocks);
	}
	
	public boolean hasBlock(String name) {
		return blocks.containsKey(name);
	}
	
	public byte[] getBlock(String name) {
		return blocks.get(name);
	}
	
	public List<InfoBlockLookup> getInfoBlockLookup() {
		return Collections.unmodifiableList(infoBlockLookup);
	}
	
	public List<BodyBlockLookup> getBodyBlockLookup() {
		return Collections.unmodifiableList(bodyBlockLookup);
	}
	
	public Map<String, G3DTexture> getTextures() {
		return Collections.unmodifiableMap(textures);
	}
	
	public G3DTexture getTexture(String name) {
		return textures.get(name);
	}
	
	public List<byte[]> getMeshes() {
		return Collections.unmodifiableList(meshes);
	}
	
	public byte[] getMesh(int index) {
		return meshes.get(index);
	}
	
	public List<byte[]> getAnimations() {
		return Collections.unmodifiableList(animations);
	}
	
	public byte[] getAnimation(int index) {
		return animations.get(index);
	}
	
	public List<AudioBankSampleHeader> getAudioSampleHeaders() {
		return Collections.unmodifiableList(audioSampleHeaders);
	}
	
	public AudioBankSampleHeader getAudioSampleHeader(int index) {
		return audioSampleHeaders.get(index);
	}
	
	public List<byte[]> getAudioSampleData() {
		return Collections.unmodifiableList(audioSampleData);
	}
	
	public byte[] getAudioSampleData(int index) {
		return audioSampleData.get(index);
	}
	
	
	public static PackFile load(File file) throws IOException {
		PackFile res = new PackFile();
		res.read(file);
		return res;
	}
}
