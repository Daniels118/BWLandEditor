package it.ld.bw.info;

import java.io.IOException;

import it.ld.utils.EndianDataInputStream;
import it.ld.utils.EndianDataOutputStream;

public class GCitadelHeartInfo extends GCitadelPartInfo {
	public int startGoodness;
	public int startFollowers;
	public int maxFlockCount;
	public float[] storyInfluence = new float[5];
	public float transferedDamageMultiplier;
	
	@Override
	public void read(EndianDataInputStream str) throws IOException {
		little(str);
		super.read(str);
		startGoodness = str.readInt();
		startFollowers = str.readInt();
		maxFlockCount = str.readInt();
		storyInfluence = readFloatArray(str, storyInfluence);
		transferedDamageMultiplier = str.readFloat();
	}
	
	@Override
	public void write(EndianDataOutputStream str) throws IOException {
		throw new RuntimeException("Method not implemented");
	}
}
