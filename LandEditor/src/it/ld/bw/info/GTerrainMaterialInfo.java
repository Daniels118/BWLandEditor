package it.ld.bw.info;

import it.ld.utils.EndianDataInputStream;
import it.ld.utils.EndianDataOutputStream;
import it.ld.utils.Struct;

public class GTerrainMaterialInfo extends Struct {
	public int surfaceSound;
	public int immersion;
	public float surfaceFriction;
	public String debugString;
	public int helpStartEnum;
	public int helpEndEnum;
	public int[] tornadoDustColorRGB = new int[3];
	public int[] magicTreeTypes = new int[4];
	
	@Override
	public void read(EndianDataInputStream str) throws Exception {
		little(str);
		surfaceSound = str.readInt();
		immersion = str.readInt();
		surfaceFriction = str.readFloat();
		debugString = readFixedString(str, 0x30, true);
		helpStartEnum = str.readInt();
		helpEndEnum = str.readInt();
		tornadoDustColorRGB = readIntArray(str, tornadoDustColorRGB);
		magicTreeTypes = readIntArray(str, magicTreeTypes);
	}

	@Override
	public void write(EndianDataOutputStream str) throws Exception {
		throw new RuntimeException("Method not implemented");
	}
	
	@Override
	public String toString() {
		return debugString != null ? debugString : super.toString();
	}
}
