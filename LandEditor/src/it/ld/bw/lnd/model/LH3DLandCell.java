/* Copyright (c) 2024-2026 Daniele Lombardi / Daniels118
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

import it.ld.utils.EndianDataInputStream;
import it.ld.utils.EndianDataOutputStream;
import it.ld.utils.MathUtils;
import it.ld.utils.Struct;

public class LH3DLandCell extends Struct {
	public enum Property {R, G, B, LUMINOSITY, ALTITUDE, SAVECOLOR, PROPERTIES, FLAGS}
	
	public static final float BRIGHT = 254f / 255f;
	public static final float DARK = 48f / 255f;
	
	public static final float HEIGHT_UNIT = 0.67f;
	public static final float CELL_SIZE = 10f;
	
	public static final int DEEP_WATER_ALTITUDE = 0;
	public static final int SHALLOW_WATER_ALTITUDE = 1;
	public static final int COASTLINE_ALTITUDE = 2;
	public static final int WET_ALTITUDE = 3;
	public static final int DRY_ALTITUDE = 4;
	
	public static final float DEEP_WATER_HEIGHT = -10f;
	public static final float DRY_HEIGHT = DRY_ALTITUDE * HEIGHT_UNIT;
	
	public static final byte HAS_WATER = (byte)0x10;
	public static final byte COASTLINE = (byte)0x20;
	public static final byte FULLWATER = (byte)0x40;
	public static final byte SPLITDIR  = (byte)0x80;
	
	public enum Sound {
		NONE(0, false),			//silence
		SPLASH(2, true),		//transition between deep and shallow water
		OCEAN(3, true),			//open sea
		SLOW_WAVES(4, false),	//lake boundaries
		LAKE(5, true),			//lake water
		COAST(6, false),		//
		FAST_WAVES(7, true),	//fjord
		JUNGLE(8, false),		//
		WIND(10, false),		//snowy zones
		DESERT(12, false),
		BIRDS(14, false),		//most zones
		FOREST(16, false),
		RIVER(18, false);
		
		public final int code;
		public final boolean water;
		
		private Sound(int code, boolean water) {
			this.code = code;
			this.water = water;
		}
		
		public static Sound valueOf(int code) {
			if (code == 1 || code > 8 && ((code & 1) == 1)) code--;
			for (Sound sound : values()) {
				if (sound.code == code) return sound;
			}
			throw new IllegalArgumentException("Invalid sound code: " + code);
		}
	}
	
	public static final LH3DLandCell EMPTY = new ImmutableWaterCell();
	
	
	private byte r = 0;
	private byte g = 0;
	private byte b = 0;
	private byte luminosity = (byte)254;
	
	private byte altitude;
	private byte savecolor;
	
    private byte propertiesLow = 0b00010000;	//Bit flags defining cell properties and country
	private byte propertiesHigh = 6;			//sound properties
	
	//Fake fields
	private byte altitudeBits = 8;
	private float height = DEEP_WATER_HEIGHT;	//This is used internally to hold a more precise elevation while editing
	
	public LH3DLandCell(int altitudeBits) {
		this.altitudeBits = (byte)altitudeBits;
		setAltitude(0);
		setSavecolor(luminosity);
	}
	
	@Override
	public void read(EndianDataInputStream str) throws Exception {
		try {
			//str.order(ByteOrder.LITTLE_ENDIAN);
			r = str.readByte();
			g = str.readByte();
			b = str.readByte();
			luminosity = str.readByte();
			altitude = str.readByte();
			savecolor = str.readByte();
			propertiesLow = str.readByte();
			propertiesHigh = str.readByte();
			this.height = getHeight(getAltitude());
		} finally {
			
		}
	}
	
	@Override
	public void write(EndianDataOutputStream str) throws Exception {
		try {
			//str.order(ByteOrder.LITTLE_ENDIAN);
			str.writeByte(r);
			str.writeByte(g);
			str.writeByte(b);
			str.writeByte(luminosity);
			str.writeByte(altitude);
			str.writeByte(savecolor);
			str.writeByte(propertiesLow);
			str.writeByte(propertiesHigh);
		} finally {
			
		}
	}
	
	public void set(LH3DLandCell ref) {
		this.altitudeBits = ref.altitudeBits;
		this.r = ref.r;
		this.g = ref.g;
		this.b = ref.b;
		this.luminosity = ref.luminosity;
		this.setAltitude(ref.getAltitude());
		this.setSavecolor(ref.getSavecolor());
		this.propertiesLow = ref.propertiesLow;
		this.propertiesHigh = ref.propertiesHigh;
	}

	public byte getR() {
		return r;
	}

	public void setR(byte r) {
		this.r = r;
	}

	public byte getG() {
		return g;
	}

	public void setG(byte g) {
		this.g = g;
	}

	public byte getB() {
		return b;
	}

	public void setB(byte b) {
		this.b = b;
	}

	public byte getLuminosity() {
		return luminosity;
	}
	
	public void setLuminosity(byte l) {
		this.luminosity = l;
	}
	
	public float getLightLevel() {
		return (luminosity & 0xFF) / 255f;
	}
	
	public void setLightLevel(float l) {
		this.luminosity = (byte)(int)(l * 255f);
		this.setSavecolor(luminosity);
	}
	
	public int getAltitude() {
		int mask = 0xFF >> (16 - altitudeBits);
		return ((int)savecolor & mask) << 8 | (altitude & 0xFF);
	}
	
	public void setAltitude(int altitude) {
		altitude = MathUtils.clamp(altitude, 0, getMaxAltitude());
		int mask = 0xFF >> (16 - altitudeBits);
		this.altitude = (byte)(altitude & 0xFF);
		this.savecolor = (byte)((this.savecolor & ~mask) | ((altitude >> 8) & mask));
		this.height = getHeight(altitude);
	}
	
	public float getHeight() {
		return this.height;
	}
	
	public void setHeight(float height) {
		height = MathUtils.clamp(height, DEEP_WATER_HEIGHT, getMaxHeight());
		this.height = height;
		float a;
		if (height < -3.5f * HEIGHT_UNIT) {
			a = 0;
		} else if (height < 3.5f * HEIGHT_UNIT) {
			a = (height + 3f) / HEIGHT_UNIT;
		} else {
			a = height / HEIGHT_UNIT;
		}
		int altitude = Math.round(MathUtils.clamp(a, 0f, getMaxAltitude()));
		int mask = 0xFF >> (16 - altitudeBits);
		this.altitude = (byte)(altitude & 0xFF);
		this.savecolor = (byte)((this.savecolor & ~mask) | ((altitude >> 8) & mask));
	}
	
	/**Fit the real elevation to the discrete value that can be stored.
	 * @return
	 */
	public boolean fitHeight() {
		float newHeight = getHeight(getAltitude());
		if (newHeight == this.height) return false;
		this.height = newHeight;
		return true;
	}
	
	public byte getSavecolor() {
		int scMask = 0xFF << (altitudeBits - 8);
		return (byte)(savecolor & scMask);
	}

	public void setSavecolor(byte savecolor) {
		int altMask = 0xFF >> (16 - altitudeBits);
		int scMask = ~altMask;
		this.savecolor = (byte)((savecolor & scMask) | (this.savecolor & altMask));
	}
	
	public int getProperties() {
		return (propertiesHigh & 0xFF) << 8 | (propertiesLow & 0xFF);
	}
	
	public byte getPropertiesLow() {
		return propertiesLow;
	}

	public void setPropertiesLow(byte propertiesLow) {
		this.propertiesLow = propertiesLow;
	}
	
	public byte getPropertiesHigh() {
		return propertiesHigh;
	}

	public void setPropertiesHigh(byte propertiesHigh) {
		this.propertiesHigh = propertiesHigh;
	}

	public int getSound() {
		return (int)propertiesHigh >> 1;
	}

	public void setSound(int sound) {
		this.propertiesHigh = (byte)((sound << 1) & 0xFF);
	}
	
	public Sound getSoundEnum() {
		return Sound.valueOf(getSound());
	}
	
	public void setSound(Sound sound) {
		this.propertiesHigh = (byte) ((propertiesHigh & 0x01) | (sound.code << 1));
	}
	
	public boolean isTransparent() {
		return (propertiesHigh & 1) != 0;
	}
	
	public void setTransparent(boolean transparent) {
		if (transparent) {
			propertiesHigh |= 0b00000001;
		} else {
			propertiesHigh &= 0b11111110;
		}
	}
	
	public int getCountry() {
		return propertiesLow & 0x0F;
	}
	
	public void setCountry(int country) {
		if (country < 0 || country > 0x0F) throw new RuntimeException("Invalid country");
		propertiesLow = (byte) ((propertiesLow & 0xF0) | (country & 0x0F));
	}
	
	public boolean hasWater() {
		return (propertiesLow & 0b00010000) != 0;
	}
	
	public void setWater(boolean water) {
		if (water) {
			propertiesLow |= 0b00010000;
		} else {
			propertiesLow &= 0b11101111;
		}
	}
	
	public boolean isCoastLine() {
		return (propertiesLow & 0b00100000) != 0;
	}
	
	public void setCoastLine(boolean coast) {
		if (coast) {
			propertiesLow |= 0b00100000;
		} else {
			propertiesLow &= 0b11011111;
		}
	}
	
	public boolean isFullWater() {
		return (propertiesLow & 0b01000000) != 0;
	}
	
	public void setFullWater(boolean water) {
		if (water) {
			propertiesLow |= 0b01000000;
		} else {
			propertiesLow &= 0b10111111;
		}
	}
	
	/**Tells the direction of the diagonal which splits the cell in 2 triangles.
	 * 
	 * true if diagonal is bottom-right -> top-left
	 * false if diagonal is top-right -> bottom-left
	 * 
	 * @return
	 */
	public boolean hasSplit() {
		return (propertiesLow & 0b10000000) != 0;
	}
	
	/**Set the direction of the diagonal which splits the cell in 2 triangles.
	 * @param split true if diagonal is bottom-right -> top-left, false if diagonal is top-right -> bottom-left
	 */
	public void setSplit(boolean split) {
		if (split) {
			propertiesLow |= 0b10000000;
		} else {
			propertiesLow &= 0b01111111;
		}
	}
	
	public float getAlpha() {
		return MathUtils.clamp(((float)getAltitude() - 0.5f) * 0.5f, 0f, 1f);
	}
	
	void setAltitudeBits(int bits) {
		if (bits < 8 || bits > 16) throw new IllegalArgumentException("Altitude bits must be in range [8, 16]");
		int tmpAltitude = Math.min(getAltitude(), getMaxAltitude(bits));
		this.altitudeBits = (byte)bits;
		setAltitude(tmpAltitude);
		setSavecolor(luminosity);
	}
	
	int getAltitudeBits() {
		return altitudeBits;
	}
	
	public int getMaxAltitude() {
		return getMaxAltitude(altitudeBits);
	}
	
	public float getMaxHeight() {
		return HEIGHT_UNIT * getMaxAltitude(altitudeBits);
	}
	
	@Override
	public String toString() {
		return String.valueOf(getAltitude());
		//return String.valueOf(getCountry());
	}
	
	public static float getHeight(int altitude) {
		if (altitude == DEEP_WATER_ALTITUDE) {
			return DEEP_WATER_HEIGHT;
		} else if (altitude < DRY_ALTITUDE) {
			return 0f;
		} else {
			return HEIGHT_UNIT * altitude;
		}
	}
	
	public static int getMaxAltitude(int altitudeBits) {
		return 0xFFFF >> (16 - altitudeBits);
	}
	
	public static float getMaxHeight(int altitudeBits) {
		return HEIGHT_UNIT * getMaxAltitude(altitudeBits);
	}
	
	
	private static class ImmutableWaterCell extends LH3DLandCell {
		private boolean immutable = false;
		
		public ImmutableWaterCell() {
			super(8);
			immutable = true;
		}
		
		@Override
		public void set(LH3DLandCell ref) {
			throw new IllegalStateException("This cell is immutable");
		}
		
		@Override
		public void setAltitude(int altitude) {
			if (immutable) throw new IllegalStateException("This cell is immutable");
			super.setAltitude(altitude);
		}
		
		@Override
		void setAltitudeBits(int bits) {
			throw new IllegalStateException("This cell is immutable");
		}
		
		@Override
		public void setB(byte b) {
			throw new IllegalStateException("This cell is immutable");
		}
		
		@Override
		public void setCoastLine(boolean coast) {
			throw new IllegalStateException("This cell is immutable");
		}
		
		@Override
		public void setCountry(int country) {
			throw new IllegalStateException("This cell is immutable");
		}
		
		@Override
		public void setFullWater(boolean water) {
			throw new IllegalStateException("This cell is immutable");
		}
		
		@Override
		public void setG(byte g) {
			throw new IllegalStateException("This cell is immutable");
		}
		
		@Override
		public void setHeight(float height) {
			throw new IllegalStateException("This cell is immutable");
		}
		
		@Override
		public void setLightLevel(float l) {
			throw new IllegalStateException("This cell is immutable");
		}
		
		@Override
		public void setLuminosity(byte l) {
			throw new IllegalStateException("This cell is immutable");
		}
		
		@Override
		public void setPropertiesHigh(byte propertiesHigh) {
			throw new IllegalStateException("This cell is immutable");
		}
		
		@Override
		public void setPropertiesLow(byte propertiesLow) {
			throw new IllegalStateException("This cell is immutable");
		}
		
		@Override
		public void setR(byte r) {
			throw new IllegalStateException("This cell is immutable");
		}
		
		@Override
		public void setSavecolor(byte savecolor) {
			if (immutable) throw new IllegalStateException("This cell is immutable");
			super.setSavecolor(savecolor);
		}
		
		@Override
		public void setSound(int sound) {
			throw new IllegalStateException("This cell is immutable");
		}
		
		@Override
		public void setSound(Sound sound) {
			throw new IllegalStateException("This cell is immutable");
		}
		
		@Override
		public void setSplit(boolean split) {
			throw new IllegalStateException("This cell is immutable");
		}
		
		@Override
		public void setTransparent(boolean transparent) {
			throw new IllegalStateException("This cell is immutable");
		}
		
		@Override
		public void setWater(boolean water) {
			throw new IllegalStateException("This cell is immutable");
		}
	}
}
