package it.ld.bw.info;

import java.io.IOException;

import it.ld.utils.EndianDataInputStream;
import it.ld.utils.EndianDataOutputStream;

public class GMagicForestInfo extends GMagicInfo {
	public int finalNoTrees;
	public float startLife;
	public float growSpeed;
	public float decaySpeed;
	public float woodValueMultiplier;
	
	@Override
	public void read(EndianDataInputStream str) throws IOException {
		little(str);
		super.read(str);
		finalNoTrees = str.readInt();
		startLife = str.readFloat();
		growSpeed = str.readFloat();
		decaySpeed = str.readFloat();
		woodValueMultiplier = str.readFloat();
	}
	
	@Override
	public void write(EndianDataOutputStream str) throws IOException {
		throw new RuntimeException("Method not implemented");
	}
}
