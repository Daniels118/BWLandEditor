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

import java.nio.ByteOrder;

import it.ld.utils.EndianDataInputStream;
import it.ld.utils.EndianDataOutputStream;
import it.ld.utils.Struct;

public class AudioBankSampleHeader extends Struct {
	private static final int NAME_SIZE = 0x100;
	private static final int DESCRIPTION_SIZE = 0x100;
	
	private String name;
	@SuppressWarnings("unused")
	private int unknown0;
	private int id;
	private int isBank;
	private int size;
	private int offset;
	private int isClone;
	private short group;
	private short atmosGroup;
	@SuppressWarnings("unused")
	private int unknown4;
	@SuppressWarnings("unused")
	private int unknown5;
	@SuppressWarnings("unused")
	private short unknown6a;
	@SuppressWarnings("unused")
	private short unknown6b;
	private int sampleRate;
	@SuppressWarnings("unused")
	private short unknownOthera;
	@SuppressWarnings("unused")
	private short unknownOtherb;
	@SuppressWarnings("unused")
	private short unknown7a;
	@SuppressWarnings("unused")
	private short unknown7b;
	@SuppressWarnings("unused")
	private int unknown8;
	private int lStart;
	private int lEnd;
	private String description;
	private short priority;
	@SuppressWarnings("unused")
	private short unknown9;
	@SuppressWarnings("unused")
	private short unknown10;
	@SuppressWarnings("unused")
	private short unknown11;
	private short loop;
	private short start;
	private byte pan;
	@SuppressWarnings("unused")
	private short unknown12;
	private float posX;
	private float posY;
	private float posZ;
	private byte volume;
	private short userParam;
	private short pitch;
	@SuppressWarnings("unused")
	private short unknown18;
	private short pitchDeviation;
	@SuppressWarnings("unused")
	private short unknown20;
	private float minDist;
	private float maxDist;
	private float scale;
	private AudioBankLoop loopType;	//short
	@SuppressWarnings("unused")
	private short unknown21;
	@SuppressWarnings("unused")
	private short unknown22;
	@SuppressWarnings("unused")
	private short unknown23;
	private short atmos;
	
	@Override
	public void read(EndianDataInputStream str) throws Exception {
		str.order(ByteOrder.LITTLE_ENDIAN);
		name = readFixedString(str, NAME_SIZE, true);
		unknown0 = str.readInt();
		id = str.readInt();
		isBank = str.readInt();
		size = str.readInt();
		offset = str.readInt();
		isClone = str.readInt();
		group = str.readShort();
		atmosGroup = str.readShort();
		unknown4 = str.readInt();
		unknown5 = str.readInt();
		unknown6a = str.readShort();
		unknown6b = str.readShort();
		sampleRate = str.readInt();
		unknownOthera = str.readShort();
		unknownOtherb = str.readShort();
		unknown7a = str.readShort();
		unknown7b = str.readShort();
		unknown8 = str.readInt();
		lStart = str.readInt();
		lEnd = str.readInt();
		description = readFixedString(str, DESCRIPTION_SIZE, true);
		priority = str.readShort();
		unknown9 = str.readShort();
		unknown10 = str.readShort();
		unknown11 = str.readShort();
		loop = str.readShort();
		start = str.readShort();
		pan = str.readByte();
		unknown12 = str.readShort();
		posX = str.readFloat();
		posY = str.readFloat();
		posZ = str.readFloat();
		volume = str.readByte();
		userParam = str.readShort();
		pitch = str.readShort();
		unknown18 = str.readShort();
		pitchDeviation = str.readShort();
		unknown20 = str.readShort();
		minDist = str.readFloat();
		maxDist = str.readFloat();
		scale = str.readFloat();
		loopType = AudioBankLoop.values()[str.readShort()];
		unknown21 = str.readShort();
		unknown22 = str.readShort();
		unknown23 = str.readShort();
		atmos = str.readShort();
	}
	
	@Override
	public void write(EndianDataOutputStream str) throws Exception {
		throw new RuntimeException("Method not implemented");
	}
	
	public String getName() {
		return name;
	}

	public int getId() {
		return id;
	}

	public int getIsBank() {
		return isBank;
	}

	public int getSize() {
		return size;
	}

	public int getOffset() {
		return offset;
	}

	public int getIsClone() {
		return isClone;
	}

	public short getGroup() {
		return group;
	}

	public short getAtmosGroup() {
		return atmosGroup;
	}

	public int getSampleRate() {
		return sampleRate;
	}

	public int getlStart() {
		return lStart;
	}

	public int getlEnd() {
		return lEnd;
	}

	public String getDescription() {
		return description;
	}

	public short getPriority() {
		return priority;
	}

	public short getLoop() {
		return loop;
	}

	public short getStart() {
		return start;
	}

	public byte getPan() {
		return pan;
	}

	public float getPosX() {
		return posX;
	}

	public float getPosY() {
		return posY;
	}

	public float getPosZ() {
		return posZ;
	}

	public byte getVolume() {
		return volume;
	}

	public short getUserParam() {
		return userParam;
	}

	public short getPitch() {
		return pitch;
	}

	public short getPitchDeviation() {
		return pitchDeviation;
	}

	public float getMinDist() {
		return minDist;
	}

	public float getMaxDist() {
		return maxDist;
	}

	public float getScale() {
		return scale;
	}

	public AudioBankLoop getLoopType() {
		return loopType;
	}

	public short getAtmos() {
		return atmos;
	}
}
